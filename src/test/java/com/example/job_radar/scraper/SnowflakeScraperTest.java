package com.example.job_radar.scraper;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.scraper.selenium.SnowflakeScraper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnowflakeScraperTest {

    @Test
    void testScrape() {
        SnowflakeScraper scraper = new SnowflakeScraper();
        List<JobPosting> jobs = scraper.scrape();

        System.out.println("=== Scraped " + jobs.size() + " Snowflake jobs ===");
        for (JobPosting job : jobs) {
            System.out.printf("Title: %s | Location: %s | Date: %s%n",
                    job.getTitle(), job.getLocation(), job.getDate());
        }

        assertEquals(6, jobs.size(),
                "Expected 6 Snowflake intern postings (Job Level=Intern), got " + jobs.size());

        for (JobPosting job : jobs) {
            assertFalse(job.getTitle().isEmpty(), "Title should be present");
            assertFalse(job.getLocation().isEmpty(), "Location should be present");
            assertTrue(job.getTitle().toLowerCase().contains("intern"),
                    "Every posting should be an intern role: " + job.getTitle());
        }
    }
}
