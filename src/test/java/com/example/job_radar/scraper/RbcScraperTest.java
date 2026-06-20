package com.example.job_radar.scraper;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.scraper.workday.RbcScraper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class RbcScraperTest {

    /**
     * Live smoke test against RBC's Workday backend. RBC's Canadian Student/Coop intake is
     * seasonal, so this can legitimately return 0 — we only assert the scrape runs cleanly.
     */
    @Test
    void testScrape() {
        RbcScraper scraper = new RbcScraper();
        List<JobPosting> jobs = scraper.scrape();

        assertNotNull(jobs);
        System.out.println("=== Scraped " + jobs.size() + " RBC jobs ===");
        for (JobPosting job : jobs) {
            System.out.printf("Title: %s | Location: %s | Date: %s%n",
                    job.getTitle(), job.getLocation(), job.getDate());
        }
    }
}
