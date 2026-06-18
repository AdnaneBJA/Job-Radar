package com.example.job_radar.scraper.workday;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.scraper.JobScraper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Base scraper for any Workday-hosted careers site (*.myworkdayjobs.com).
 *
 * <p>Rather than driving headless Chrome against the JS-rendered page (fragile emotion-hashed
 * CSS classes, one Chrome process per company), this hits Workday's public CXS JSON endpoint
 * that the page itself calls: {@code POST /wday/cxs/{tenant}/{site}/jobs}. Subclasses only
 * provide the normal public careers URL via {@link #getBaseUrl()}; the tenant, site, and
 * applied facets are derived from it.
 */
public abstract class WorkdayScraper implements JobScraper {

    private static final Logger log = LoggerFactory.getLogger(WorkdayScraper.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    // Workday caps page size at 20.
    private static final int PAGE_SIZE = 20;
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36";

    protected abstract String getBaseUrl();

    protected abstract String getCompanyName();

    protected boolean shouldInclude(JobPosting job) {
        return true;
    }

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        try {
            URI base = URI.create(getBaseUrl());
            String endpoint = cxsEndpoint(base);
            String facets = facetsJson(base.getRawQuery());

            int offset = 0;
            int total = Integer.MAX_VALUE;
            while (offset < total) {
                String body = "{\"appliedFacets\":" + facets
                        + ",\"limit\":" + PAGE_SIZE
                        + ",\"offset\":" + offset
                        + ",\"searchText\":\"\"}";

                HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                        .timeout(Duration.ofSeconds(20))
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .header("User-Agent", USER_AGENT)
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();

                HttpResponse<String> response =
                        HTTP.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    log.error("[{}] Workday API returned HTTP {} for {}",
                            getCompanyName(), response.statusCode(), endpoint);
                    break;
                }

                JsonNode root = MAPPER.readTree(response.body());
                total = root.path("total").asInt(0);

                List<JobPosting> pageJobs = mapPostings(root, getCompanyName());
                if (pageJobs.isEmpty()) {
                    break;
                }
                for (JobPosting job : pageJobs) {
                    if (!job.getTitle().isEmpty() && shouldInclude(job)) {
                        jobs.add(job);
                    }
                }
                offset += PAGE_SIZE;
            }

            log.info("[{}] Scraping complete. Total jobs: {}", getCompanyName(), jobs.size());
        } catch (Exception e) {
            log.error("[{}] Error scraping Workday jobs", getCompanyName(), e);
        }
        return jobs;
    }

    /** Builds the Workday CXS jobs endpoint from a public careers URL. */
    static String cxsEndpoint(URI careersUrl) {
        String host = careersUrl.getHost();
        String tenant = host.substring(0, host.indexOf('.'));
        String site = lastPathSegment(careersUrl.getPath());
        return "https://" + host + "/wday/cxs/" + tenant + "/" + site + "/jobs";
    }

    private static String lastPathSegment(String path) {
        if (path == null) {
            return "";
        }
        String[] parts = path.split("/");
        for (int i = parts.length - 1; i >= 0; i--) {
            if (!parts[i].isEmpty()) {
                return parts[i];
            }
        }
        return "";
    }

    /**
     * Converts a careers-URL query string into a Workday {@code appliedFacets} JSON object,
     * grouping repeated keys into arrays (e.g. {@code jobFamilyGroup=A&jobFamilyGroup=B}).
     */
    static String facetsJson(String rawQuery) {
        Map<String, List<String>> facets = new LinkedHashMap<>();
        if (rawQuery != null && !rawQuery.isBlank()) {
            for (String pair : rawQuery.split("&")) {
                int eq = pair.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String key = URLDecoder.decode(pair.substring(0, eq), StandardCharsets.UTF_8);
                String value = URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8);
                facets.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
            }
        }
        try {
            return MAPPER.writeValueAsString(facets);
        } catch (Exception e) {
            return "{}";
        }
    }

    /** Maps a Workday CXS response into job postings (without per-company filtering). */
    static List<JobPosting> mapPostings(JsonNode root, String company) {
        List<JobPosting> jobs = new ArrayList<>();
        JsonNode postings = root.path("jobPostings");
        if (!postings.isArray()) {
            return jobs;
        }
        for (JsonNode posting : postings) {
            JobPosting job = new JobPosting();
            job.setCompany(company);
            job.setTitle(posting.path("title").asText("").trim());
            job.setLocation(posting.path("locationsText").asText("").trim());
            job.setDate(posting.path("postedOn").asText("").trim());
            jobs.add(job);
        }
        return jobs;
    }
}
