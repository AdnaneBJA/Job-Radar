package com.example.job_radar.scraper.selenium;

import com.example.job_radar.model.JobPosting;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class AppleScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(AppleScraper.class);

    private static final String BASE_URL =
            "https://jobs.apple.com/en-ca/search?location=canada-CANC&team=internships-STDNT-INTRN";
    private static final String COMPANY = "Apple";
    private static final String NO_RESULTS_MARKER =
            "There are no results that match your search.";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            Thread.sleep(8000);

            String body;
            try {
                body = driver.findElement(By.tagName("body")).getText();
            } catch (Exception ex) {
                log.warn("[Apple] Could not read page body: {}", ex.getMessage());
                return jobs;
            }

            if (body.length() < 200
                    || body.contains("Access Denied") || body.contains("Forbidden")
                    || body.contains("502 Bad Gateway") || body.contains("503 Service Unavailable")) {
                log.warn("[Apple] Page returned an error or empty body; skipping");
                return jobs;
            }

            boolean onCareersPage = body.contains("Apple")
                    && (body.contains("internships") || body.contains("Internship")
                        || body.contains("Search Jobs") || body.contains("search results"));
            if (!onCareersPage) {
                log.warn("[Apple] Body does not look like the Apple careers page; skipping");
                return jobs;
            }

            if (body.contains(NO_RESULTS_MARKER)) {
                log.info("[Apple] Tracked 'no results' marker still present — no internships yet");
                return jobs;
            }

            log.info("[Apple] Tracked 'no results' marker is GONE — Apple may have added postings!");
            JobPosting job = new JobPosting();
            job.setCompany(COMPANY);
            job.setTitle("Apple Canada internships page changed — check for new postings");
            job.setLocation("");
            job.setDate(LocalDate.now().toString());
            jobs.add(job);
        } catch (Exception e) {
            log.error("[Apple] Error checking Apple internships page", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
