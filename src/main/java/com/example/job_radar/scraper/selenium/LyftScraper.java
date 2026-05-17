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
public class LyftScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(LyftScraper.class);

    private static final String BASE_URL = "https://www.lyft.com/careers/early-talent";
    private static final String COMPANY = "Lyft";
    private static final String[] NO_JOBS_MARKERS = {
            "Sorry, no jobs were found for that criteria",
            "no jobs were found",
            "aucun emploi n'a",
            "Showing 0 of",
            "Afficher 0 des"
    };

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
                log.warn("[Lyft] Could not read page body: {}", ex.getMessage());
                return jobs;
            }

            if (body.contains("upstream connect error") || body.contains("connection timeout")
                    || body.contains("502 Bad Gateway") || body.contains("503 Service Unavailable")
                    || body.contains("Access Denied") || body.contains("Forbidden")
                    || body.length() < 200) {
                log.warn("[Lyft] Page returned an error or empty body in this environment; skipping");
                return jobs;
            }

            // Sanity-check we actually loaded the Lyft careers page before declaring a change.
            boolean onCareersPage = body.contains("Lyft") || body.contains("Early Career")
                    || body.contains("Early Talent") || body.contains("Careers");
            if (!onCareersPage) {
                log.warn("[Lyft] Body does not look like the Lyft careers page; skipping");
                return jobs;
            }

            for (String marker : NO_JOBS_MARKERS) {
                if (body.contains(marker)) {
                    log.info("[Lyft] Tracked 'no jobs' marker still present — no postings yet ('{}')", marker);
                    return jobs;
                }
            }

            log.info("[Lyft] Tracked 'no jobs' message is GONE — Lyft may have added postings!");
            JobPosting job = new JobPosting();
            job.setCompany(COMPANY);
            job.setTitle("Lyft early-talent page changed — check for new postings");
            job.setLocation("");
            job.setDate(LocalDate.now().toString());
            jobs.add(job);
        } catch (Exception e) {
            log.error("[Lyft] Error checking Lyft early-talent page", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
