package com.example.job_radar.scraper;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.scraper.rest.SnowflakeScraper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SnowflakeScraperTest {

    /**
     * Live smoke test against Snowflake's Phenom search API (type=Intern). Intern intake is
     * seasonal so this can legitimately return 0 — we only assert the scrape runs cleanly and
     * any returned rows have a title.
     */
    @Test
    void testScrape() {
        SnowflakeScraper scraper = new SnowflakeScraper();
        List<JobPosting> jobs = scraper.scrape();

        assertNotNull(jobs);
        System.out.println("=== Scraped " + jobs.size() + " Snowflake jobs ===");
        for (JobPosting job : jobs) {
            System.out.printf("Title: %s | Location: %s | Date: %s%n",
                    job.getTitle(), job.getLocation(), job.getDate());
            assertFalse(job.getTitle().isEmpty(), "Title should be present");
        }
    }
}
