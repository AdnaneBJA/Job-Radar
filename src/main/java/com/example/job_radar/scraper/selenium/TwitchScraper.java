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
public class TwitchScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(TwitchScraper.class);

    private static final String BASE_URL = "https://careers.twitch.com/en/early-career/";
    private static final String COMPANY = "Twitch";
    private static final String CLOSED_MARKER =
            "Applications for Summer 2026 are closed.";

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
                log.warn("[Twitch] Could not read page body: {}", ex.getMessage());
                return jobs;
            }

            if (body.length() < 200
                    || body.contains("Access Denied") || body.contains("Forbidden")
                    || body.contains("502 Bad Gateway") || body.contains("503 Service Unavailable")) {
                log.warn("[Twitch] Page returned an error or empty body; skipping");
                return jobs;
            }

            boolean onCareersPage = body.contains("Twitch")
                    && (body.contains("Early Career") || body.contains("early career")
                        || body.contains("Internship") || body.contains("Careers"));
            if (!onCareersPage) {
                log.warn("[Twitch] Body does not look like the Twitch careers page; skipping");
                return jobs;
            }

            if (body.contains(CLOSED_MARKER)) {
                log.info("[Twitch] Tracked 'applications closed' marker still present — Summer 2026 still closed");
                return jobs;
            }

            log.info("[Twitch] Tracked 'applications closed' marker is GONE — Twitch may have opened applications!");
            JobPosting job = new JobPosting();
            job.setCompany(COMPANY);
            job.setTitle("Twitch early-career page changed — check if Summer 2026 applications opened");
            job.setLocation("");
            job.setDate(LocalDate.now().toString());
            jobs.add(job);
        } catch (Exception e) {
            log.error("[Twitch] Error checking Twitch early-career page", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
