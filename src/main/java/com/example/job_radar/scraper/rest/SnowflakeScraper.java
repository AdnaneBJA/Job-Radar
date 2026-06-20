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
import java.util.ArrayList;
import java.util.List;

/**
 * Snowflake careers scraper.
 *
 * <p>careers.snowflake.com is a Phenom People site. Instead of driving headless Chrome and
 * toggling filter checkboxes (whose ids change), this calls the same {@code POST /widgets}
 * JSON endpoint the page uses, searching for "intern" and keeping only {@code type=Intern}
 * postings.
 */
@Component
public class SnowflakeScraper implements JobScraper {

    private static final Logger log = LoggerFactory.getLogger(SnowflakeScraper.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    private static final String ENDPOINT = "https://careers.snowflake.com/widgets";
    private static final String COMPANY = "Snowflake";
    private static final int PAGE_SIZE = 100;
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        try {
            int from = 0;
            int total = Integer.MAX_VALUE;
            while (from < total) {
                String body = "{\"lang\":\"en_us\",\"deviceType\":\"desktop\",\"country\":\"us\","
                        + "\"pageName\":\"search-results\",\"ddoKey\":\"refineSearch\",\"sortBy\":\"\","
                        + "\"subsearch\":\"\",\"from\":" + from + ",\"jobs\":true,\"counts\":true,"
                        + "\"all_fields\":[\"category\",\"country\",\"state\",\"city\"],"
                        + "\"size\":" + PAGE_SIZE + ",\"clearAll\":false,\"jdsource\":\"facets\","
                        + "\"isSliderEnable\":false,\"keywords\":\"intern\",\"global\":true,"
                        + "\"selected_fields\":{},\"locationData\":{}}";

                HttpRequest request = HttpRequest.newBuilder(URI.create(ENDPOINT))
                        .timeout(Duration.ofSeconds(20))
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .header("User-Agent", USER_AGENT)
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();

                HttpResponse<String> response =
                        HTTP.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    log.error("[Snowflake] Phenom API returned HTTP {}", response.statusCode());
                    break;
                }

                JsonNode search = MAPPER.readTree(response.body()).path("refineSearch");
                total = search.path("totalHits").asInt(0);
                JsonNode pageJobs = search.path("data").path("jobs");
                if (!pageJobs.isArray() || pageJobs.isEmpty()) {
                    break;
                }

                for (JsonNode node : pageJobs) {
                    // Keyword search also surfaces roles that merely mention "intern" in the
                    // description; keep only actual intern postings.
                    if (!"Intern".equalsIgnoreCase(node.path("type").asText(""))) {
                        continue;
                    }
                    String title = node.path("title").asText("").trim();
                    if (title.isEmpty()) {
                        continue;
                    }
                    JobPosting job = new JobPosting();
                    job.setCompany(COMPANY);
                    job.setTitle(title);
                    job.setLocation(node.path("location").asText("").trim());
                    String posted = node.path("postedDate").asText("");
                    job.setDate(posted.length() >= 10
                            ? posted.substring(0, 10)
                            : java.time.LocalDate.now().toString());
                    jobs.add(job);
                }
                from += PAGE_SIZE;
            }

            log.info("[Snowflake] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Snowflake] Error scraping Snowflake careers", e);
        }
        return jobs;
    }
}
