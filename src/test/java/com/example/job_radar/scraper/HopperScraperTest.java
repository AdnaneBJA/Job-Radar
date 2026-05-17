package com.example.job_radar.scraper;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.scraper.ashby.HopperScraper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HopperScraperTest {

    @Test
    void testScrape() {
        HopperScraper scraper = new HopperScraper();
        List<JobPosting> jobs = scraper.scrape();

        System.out.println("=== Scraped " + jobs.size() + " Hopper jobs ===");
        for (JobPosting job : jobs) {
            System.out.printf("Title: %s | Location: %s | Date: %s%n",
                    job.getTitle(), job.getLocation(), job.getDate());
        }

        assertEquals(15, jobs.size(),
                "Expected 15 Hopper Canada (Remote) postings, got " + jobs.size());

        for (JobPosting job : jobs) {
            assertFalse(job.getTitle().isEmpty(), "Title should be present");
            String loc = job.getLocation().toLowerCase();
            assertFalse(loc.isEmpty(), "Location should be present");
            assertTrue(loc.contains("canada") || loc.contains("toronto")
                            || loc.contains("montreal") || loc.contains("vancouver")
                            || loc.contains("ottawa"),
                    "Location should be Canadian: " + job.getLocation());
        }
    }
}
