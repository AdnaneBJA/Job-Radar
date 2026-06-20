package com.example.job_radar.scraper.rest;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.scraper.JobScraper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Atlassian careers scraper.
 *
 * <p>atlassian.com/company/careers/all-jobs is a SPA whose intern table is rendered client-side
 * (driving headless Chrome against it timed out the moment the {@code team=Interns} filter had no
 * results). Instead this hits the public listings endpoint the page is backed by:
 * {@code GET /endpoint/careers/listings}, which returns every requisition as JSON. Intern / co-op
 * filtering is done on the title with word boundaries so "Internal" / "International" don't match.
 */
@Component
public class AtlassianScraper implements JobScraper {

    private static final Logger log = LoggerFactory.getLogger(AtlassianScraper.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    private static final String COMPANY = "Atlassian";
    private static final String ENDPOINT = "https://www.atlassian.com/endpoint/careers/listings";
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36";

    // Whole-word match so "Internal" / "International" don't count as "intern".
    private static final Pattern INTERN_COOP = Pattern.compile(
            "(?i)\\b(intern(s|ship|ships)?|co[ -]?op)\\b");

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(ENDPOINT))
                    .timeout(Duration.ofSeconds(20))
                    .header("Accept", "application/json")
                    .header("User-Agent", USER_AGENT)
                    .GET()
                    .build();

            HttpResponse<String> response =
                    HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.error("[Atlassian] Listings API returned HTTP {}", response.statusCode());
                return jobs;
            }

            JsonNode postings = MAPPER.readTree(response.body());
            for (JsonNode posting : postings) {
                String title = posting.path("title").asText("").trim();
                if (title.isEmpty() || !INTERN_COOP.matcher(title).find()) {
                    continue;
                }

                JobPosting job = new JobPosting();
                job.setCompany(COMPANY);
                job.setTitle(title);
                job.setLocation(joinLocations(posting.path("locations")));
                job.setDate(postedDate(posting));
                jobs.add(job);
            }

            log.info("[Atlassian] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Atlassian] Error scraping Atlassian jobs", e);
        }
        return jobs;
    }

    private static String joinLocations(JsonNode locations) {
        if (!locations.isArray() || locations.isEmpty()) {
            return "";
        }
        return StreamSupport.stream(locations.spliterator(), false)
                .map(loc -> loc.asText("").trim())
                .filter(loc -> !loc.isEmpty())
                .collect(Collectors.joining(" / "));
    }

    // updatedDate is "YYYY-MM-DD HH:MM AM/PM"; keep just the date.
    private static String postedDate(JsonNode posting) {
        String updated = posting.path("portalJobPost").path("updatedDate").asText("");
        return updated.length() >= 10 ? updated.substring(0, 10) : LocalDate.now().toString();
    }
}
