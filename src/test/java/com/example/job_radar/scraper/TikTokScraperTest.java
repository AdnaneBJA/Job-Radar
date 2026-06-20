package com.example.job_radar.scraper;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.scraper.selenium.TikTokScraper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TikTokScraperTest {

    @Test
    void testScrape() {
        TikTokScraper scraper = new TikTokScraper();
        List<JobPosting> jobs = scraper.scrape();

        System.out.println("=== Scraped " + jobs.size() + " TikTok jobs ===");
        for (JobPosting job : jobs) {
            System.out.printf("Title: %s | Location: %s | Date: %s%n",
                    job.getTitle(), job.getLocation(), job.getDate());
        }

        assertNotNull(jobs);
        assertFalse(jobs.isEmpty(), "Expected at least one TikTok posting");
        for (JobPosting job : jobs) {
            assertFalse(job.getTitle().isEmpty(), "Title should be present");
        }
    }
}
