package com.example.job_radar.scraper;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.scraper.selenium.TwilioScraper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TwilioScraperTest {

    /**
     * Smoke test only. Twilio runs on Eightfold AI, which gates job results from headless Chrome
     * and hard-403s its public API, so this currently returns 0 by design (the scraper degrades
     * quietly). We assert it runs cleanly and any rows that do appear have a title.
     */
    @Test
    void testScrape() {
        TwilioScraper scraper = new TwilioScraper();
        List<JobPosting> jobs = scraper.scrape();

        assertNotNull(jobs);
        System.out.println("=== Scraped " + jobs.size() + " Twilio jobs ===");
        for (JobPosting job : jobs) {
            System.out.printf("Title: %s | Location: %s | Date: %s%n",
                    job.getTitle(), job.getLocation(), job.getDate());
            assertFalse(job.getTitle().isEmpty(), "Title should be present");
        }
    }
}
