package com.example.job_radar.scraper;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.scraper.selenium.CohereScraper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CohereScraperTest {

    @Test
    void testScrape() {
        CohereScraper scraper = new CohereScraper();
        List<JobPosting> jobs = scraper.scrape();

        System.out.println("=== Scraped " + jobs.size() + " Cohere jobs ===");
        for (JobPosting job : jobs) {
            System.out.printf("Title: %s | Location: %s | Date: %s%n",
                    job.getTitle(), job.getLocation(), job.getDate());
        }

        for (JobPosting job : jobs) {
            assertFalse(job.getTitle().isEmpty(), "Title should be present");
            String loc = job.getLocation().toLowerCase();
            assertFalse(loc.isEmpty(), "Location should be present");
            boolean canada = loc.contains("canada")
                    || loc.contains("toronto")
                    || loc.contains("montreal")
                    || loc.contains("vancouver")
                    || loc.contains("ottawa");
            assertTrue(canada, "Location should be a Canadian city: " + job.getLocation());
        }
    }
}
