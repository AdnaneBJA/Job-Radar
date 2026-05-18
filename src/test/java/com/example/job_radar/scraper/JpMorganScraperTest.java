package com.example.job_radar.scraper;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.scraper.selenium.JpMorganScraper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JpMorganScraperTest {

    @Test
    void testScrape() {
        JpMorganScraper scraper = new JpMorganScraper();
        List<JobPosting> jobs = scraper.scrape();

        System.out.println("=== Scraped " + jobs.size() + " JPMorgan jobs ===");
        for (JobPosting job : jobs) {
            System.out.printf("Title: %s | Location: %s | Date: %s%n",
                    job.getTitle(), job.getLocation(), job.getDate());
        }

        assertNotNull(jobs);
        assertFalse(jobs.isEmpty(), "Expected at least one JPMorgan Americas internship");
        for (JobPosting job : jobs) {
            assertFalse(job.getTitle().isEmpty(), "Title should be present");
            assertTrue(job.getLocation().contains("Canada") && job.getLocation().contains("United States"),
                    "Location should include both Canada and US: " + job.getLocation());
        }
    }
}
