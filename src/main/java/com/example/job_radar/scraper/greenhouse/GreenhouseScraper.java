package com.example.job_radar.scraper.greenhouse;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.scraper.JobScraper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Base scraper for any Greenhouse-hosted job board.
 *
 * <p>Rather than driving headless Chrome against a company's marketing careers page (whose
 * markup changes constantly), this hits Greenhouse's public board API that every such page
 * is ultimately backed by: {@code GET /v1/boards/{token}/jobs}. Subclasses only provide the
 * board token via {@link #boardToken()} and a display name via {@link #company()}.
 *
 * <p>The default {@link #accept(JobPosting)} keeps only intern / co-op roles, which is the
 * intent of every company board wired up here. No location filter is applied by default —
 * both Canada and US roles are in scope — but subclasses may override to narrow.
 */
public abstract class GreenhouseScraper implements JobScraper {

    private static final Logger log = LoggerFactory.getLogger(GreenhouseScraper.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36";

    /** Greenhouse board token, e.g. {@code cloudflare} in boards.greenhouse.io/cloudflare. */
    protected abstract String boardToken();

    protected abstract String company();

    /** Keep only roles this radar cares about. Defaults to intern / co-op postings. */
    protected boolean accept(JobPosting job) {
        return isInternOrCoop(job.getTitle());
    }

    // Whole-word match so "Internal" / "International" don't count as "intern".
    private static final Pattern INTERN_COOP = Pattern.compile(
            "(?i)\\b(intern(s|ship|ships)?|co[ -]?op)\\b");

    protected static boolean isInternOrCoop(String title) {
        return title != null && INTERN_COOP.matcher(title).find();
    }

    @Override
    public List<JobPosting> scrape() {
        String company = company();
        List<JobPosting> jobs = new ArrayList<>();
        try {
            String endpoint = "https://boards-api.greenhouse.io/v1/boards/"
                    + boardToken() + "/jobs";

            HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(20))
                    .header("Accept", "application/json")
                    .header("User-Agent", USER_AGENT)
                    .GET()
                    .build();

            HttpResponse<String> response =
                    HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.error("[{}] Greenhouse API returned HTTP {} for {}",
                        company, response.statusCode(), endpoint);
                return jobs;
            }

            JsonNode postings = MAPPER.readTree(response.body()).path("jobs");
            for (JsonNode posting : postings) {
                JobPosting job = new JobPosting();
                job.setCompany(company);
                job.setTitle(posting.path("title").asText("").trim());
                job.setLocation(posting.path("location").path("name").asText("").trim());
                String firstPublished = posting.path("first_published").asText("");
                job.setDate(firstPublished.length() >= 10
                        ? firstPublished.substring(0, 10)
                        : java.time.LocalDate.now().toString());

                if (!job.getTitle().isEmpty() && accept(job)) {
                    jobs.add(job);
                }
            }

            log.info("[{}] Scraping complete. Total jobs: {}", company, jobs.size());
        } catch (Exception e) {
            log.error("[{}] Error scraping Greenhouse jobs", company, e);
        }
        return jobs;
    }
}
