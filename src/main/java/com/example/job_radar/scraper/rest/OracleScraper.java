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
import java.util.regex.Pattern;

/**
 * Oracle careers scraper.
 *
 * <p>careers.oracle.com is an Oracle Recruiting Cloud (ORC) SPA. Instead of driving headless
 * Chrome against its job tiles (selectors change, page is slow), this calls the ORC REST
 * endpoint the page is backed by: {@code recruitingCEJobRequisitions}. The location facet is
 * carried over from the original careers URL; intern / co-op filtering is done on the title.
 */
@Component
public class OracleScraper implements JobScraper {

    private static final Logger log = LoggerFactory.getLogger(OracleScraper.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    private static final String COMPANY = "Oracle";
    private static final String API_BASE =
            "https://eeho.fa.us2.oraclecloud.com/hcmRestApi/resources/latest/recruitingCEJobRequisitions";
    private static final String SITE_NUMBER = "CX_45001";
    // Location facet carried over from the original careers.oracle.com search URL.
    private static final String LOCATION_FACET = "300000000106749";
    private static final int PAGE_SIZE = 200;
    private static final int MAX_PAGES = 25;
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36";

    private static final Pattern INTERN_COOP = Pattern.compile(
            "(?i)\\b(intern(s|ship|ships)?|co[ -]?op)\\b");

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        try {
            int offset = 0;
            int total = Integer.MAX_VALUE;
            for (int page = 0; page < MAX_PAGES && offset < total; page++) {
                String url = API_BASE + "?onlyData=true"
                        + "&expand=requisitionList.secondaryLocations,flexFieldsFacet.values"
                        + "&finder=findReqs;siteNumber=" + SITE_NUMBER
                        + ",selectedLocationsFacet=" + LOCATION_FACET
                        + ",sortBy=POSTING_DATES_DESC"
                        + "&limit=" + PAGE_SIZE + "&offset=" + offset;

                HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                        .timeout(Duration.ofSeconds(25))
                        .header("Accept", "application/json")
                        .header("REST-Framework-Version", "3")
                        .header("User-Agent", USER_AGENT)
                        .GET()
                        .build();

                HttpResponse<String> response =
                        HTTP.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    log.error("[Oracle] ORC API returned HTTP {}", response.statusCode());
                    break;
                }

                JsonNode item = MAPPER.readTree(response.body()).path("items").path(0);
                total = item.path("TotalJobsCount").asInt(0);
                JsonNode items = item.path("requisitionList").path("items");
                if (!items.isArray() || items.isEmpty()) {
                    break;
                }

                for (JsonNode req : items) {
                    String title = req.path("Title").asText("").trim();
                    if (title.isEmpty() || !INTERN_COOP.matcher(title).find()) {
                        continue;
                    }
                    JobPosting job = new JobPosting();
                    job.setCompany(COMPANY);
                    job.setTitle(title);
                    job.setLocation(req.path("PrimaryLocation").asText("").trim());
                    job.setDate(req.path("PostedDate").asText(
                            java.time.LocalDate.now().toString()).trim());
                    jobs.add(job);
                }
                offset += items.size();
            }

            log.info("[Oracle] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Oracle] Error scraping Oracle jobs", e);
        }
        return jobs;
    }
}
