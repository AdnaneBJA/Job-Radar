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
public class ServiceNowScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(ServiceNowScraper.class);

    private static final String BASE_URL =
            "https://careers.servicenow.com/jobs/?search=&jobPostingType=Intern&pagesize=20#results";
    private static final String COMPANY = "ServiceNow";
    private static final String NO_RESULTS_MARKER =
            "Oops! It seems that there are no results matching your selected criteria";

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
                log.warn("[ServiceNow] Could not read page body: {}", ex.getMessage());
                return jobs;
            }

            if (body.length() < 200
                    || body.contains("Access Denied") || body.contains("Forbidden")
                    || body.contains("502 Bad Gateway") || body.contains("503 Service Unavailable")) {
                log.warn("[ServiceNow] Page returned an error or empty body; skipping");
                return jobs;
            }

            boolean onCareersPage = body.contains("ServiceNow")
                    && (body.contains("Intern") || body.contains("jobs")
                        || body.contains("Careers") || body.contains("search"));
            if (!onCareersPage) {
                log.warn("[ServiceNow] Body does not look like the ServiceNow careers page; skipping");
                return jobs;
            }

            if (body.contains(NO_RESULTS_MARKER)) {
                log.info("[ServiceNow] Tracked 'no results' marker still present — no intern postings yet");
                return jobs;
            }

            log.info("[ServiceNow] Tracked 'no results' marker is GONE — ServiceNow may have added postings!");
            JobPosting job = new JobPosting();
            job.setCompany(COMPANY);
            job.setTitle("ServiceNow intern listings page changed — check for new postings");
            job.setLocation("");
            job.setDate(LocalDate.now().toString());
            jobs.add(job);
        } catch (Exception e) {
            log.error("[ServiceNow] Error checking ServiceNow intern listings page", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
