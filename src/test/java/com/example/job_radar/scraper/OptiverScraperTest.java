package com.example.job_radar.scraper;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.scraper.selenium.OptiverScraper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OptiverScraperTest {

    @Test
    void testScrape() {
        OptiverScraper scraper = new OptiverScraper();
        List<JobPosting> jobs = scraper.scrape();

        System.out.println("=== Scraped " + jobs.size() + " Optiver Technology Internships ===");
        for (JobPosting job : jobs) {
            System.out.printf("Title: %s | Location: %s | Date: %s%n",
                    job.getTitle(), job.getLocation(), job.getDate());
        }

        assertNotNull(jobs);
        assertFalse(jobs.isEmpty(), "Expected at least one Optiver Technology Internship");
        for (JobPosting job : jobs) {
            assertFalse(job.getTitle().isEmpty(), "Title should be present");
        }
    }
}
