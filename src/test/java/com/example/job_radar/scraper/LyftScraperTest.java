package com.example.job_radar.scraper;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.scraper.selenium.LyftScraper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class LyftScraperTest {

    @Test
    void testScrape() {
        LyftScraper scraper = new LyftScraper();
        List<JobPosting> jobs = scraper.scrape();

        System.out.println("=== Scraped " + jobs.size() + " Lyft notifications ===");
        for (JobPosting job : jobs) {
            System.out.printf("Title: %s | Location: %s | Date: %s%n",
                    job.getTitle(), job.getLocation(), job.getDate());
        }

        // Watcher-style scraper: jobs is empty while Lyft has no postings,
        // and emits a single notification posting when the tracked message disappears.
        assertNotNull(jobs);
    }
}
