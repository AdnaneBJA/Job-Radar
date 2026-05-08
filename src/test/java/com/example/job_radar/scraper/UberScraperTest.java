package com.example.job_radar.scraper;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.scraper.selenium.UberScraper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UberScraperTest {

    @Test
    void testScrape() {
        UberScraper scraper = new UberScraper();
        List<JobPosting> jobs = scraper.scrape();

        System.out.println("=== Scraped " + jobs.size() + " Uber jobs ===");
        for (JobPosting job : jobs) {
            System.out.printf("Title: %s | Location: %s | Date: %s%n",
                    job.getTitle(), job.getLocation(), job.getDate());
        }

        for (JobPosting job : jobs) {
            assertFalse(job.getTitle().isEmpty(), "Title should be present");
            assertFalse(job.getLocation().isEmpty(), "Location should be present");
        }
    }
}
