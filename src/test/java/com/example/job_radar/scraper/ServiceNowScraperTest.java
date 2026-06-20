package com.example.job_radar.scraper;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.scraper.selenium.ServiceNowScraper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ServiceNowScraperTest {

    @Test
    void testScrape() {
        ServiceNowScraper scraper = new ServiceNowScraper();
        List<JobPosting> jobs = scraper.scrape();

        System.out.println("=== Scraped " + jobs.size() + " ServiceNow notifications ===");
        for (JobPosting job : jobs) {
            System.out.printf("Title: %s | Location: %s | Date: %s%n",
                    job.getTitle(), job.getLocation(), job.getDate());
        }

        // Watcher-style scraper: jobs is empty while ServiceNow shows "no results",
        // and emits a single notification posting when the tracked message disappears.
        assertNotNull(jobs);
    }
}
