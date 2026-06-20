package com.example.job_radar.scraper;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.scraper.greenhouse.RobinHoodScraper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RobinHoodScraperTest {

    /**
     * Live smoke test against RobinHood's Greenhouse board. Intern intake is seasonal so this can
     * legitimately return 0 — we only assert the scrape runs cleanly and rows have a title.
     */
    @Test
    void testScrape() {
        RobinHoodScraper scraper = new RobinHoodScraper();
        List<JobPosting> jobs = scraper.scrape();

        assertNotNull(jobs);
        System.out.println("=== Scraped " + jobs.size() + " RobinHood jobs ===");
        for (JobPosting job : jobs) {
            System.out.printf("Title: %s | Location: %s | Date: %s%n",
                    job.getTitle(), job.getLocation(), job.getDate());
            assertFalse(job.getTitle().isEmpty(), "Title should be present");
        }
    }
}
