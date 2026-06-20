package com.example.job_radar.scraper;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.scraper.greenhouse.CloudflareScraper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CloudflareScraperTest {

    /**
     * Live smoke test against Cloudflare's Greenhouse board. Intern intake is seasonal so this
     * can legitimately return 0 — we only assert the scrape runs cleanly and any returned rows
     * have a title.
     */
    @Test
    void testScrape() {
        CloudflareScraper scraper = new CloudflareScraper();
        List<JobPosting> jobs = scraper.scrape();

        assertNotNull(jobs);
        System.out.println("=== Scraped " + jobs.size() + " Cloudflare jobs ===");
        for (JobPosting job : jobs) {
            System.out.printf("Title: %s | Location: %s | Date: %s%n",
                    job.getTitle(), job.getLocation(), job.getDate());
            assertFalse(job.getTitle().isEmpty(), "Title should be present");
        }
    }
}
