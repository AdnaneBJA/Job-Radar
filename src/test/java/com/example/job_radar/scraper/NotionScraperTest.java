package com.example.job_radar.scraper;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.scraper.selenium.NotionScraper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NotionScraperTest {

    @Test
    void testScrape() {
        NotionScraper scraper = new NotionScraper();
        List<JobPosting> jobs = scraper.scrape();

        System.out.println("=== Scraped " + jobs.size() + " Notion University jobs ===");
        for (JobPosting job : jobs) {
            System.out.printf("Title: %s | Location: %s | Date: %s%n",
                    job.getTitle(), job.getLocation(), job.getDate());
        }

        assertNotNull(jobs);
        assertFalse(jobs.isEmpty(), "Expected at least one Notion University posting");
        for (JobPosting job : jobs) {
            assertFalse(job.getTitle().isEmpty(), "Title should be present");
        }
    }
}
