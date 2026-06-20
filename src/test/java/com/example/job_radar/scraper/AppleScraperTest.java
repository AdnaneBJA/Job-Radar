package com.example.job_radar.scraper;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.scraper.selenium.AppleScraper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class AppleScraperTest {

    @Test
    void testScrape() {
        AppleScraper scraper = new AppleScraper();
        List<JobPosting> jobs = scraper.scrape();

        System.out.println("=== Scraped " + jobs.size() + " Apple notifications ===");
        for (JobPosting job : jobs) {
            System.out.printf("Title: %s | Location: %s | Date: %s%n",
                    job.getTitle(), job.getLocation(), job.getDate());
        }

        // Watcher-style scraper: jobs is empty while Apple shows "no results",
        // and emits a single notification posting when the tracked message disappears.
        assertNotNull(jobs);
    }
}
