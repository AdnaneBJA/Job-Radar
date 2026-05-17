package com.example.job_radar.scraper;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.scraper.selenium.SpotifyScraper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class SpotifyScraperTest {

    @Test
    void testScrape() {
        SpotifyScraper scraper = new SpotifyScraper();
        List<JobPosting> jobs = scraper.scrape();

        System.out.println("=== Scraped " + jobs.size() + " Spotify notifications ===");
        for (JobPosting job : jobs) {
            System.out.printf("Title: %s | Location: %s | Date: %s%n",
                    job.getTitle(), job.getLocation(), job.getDate());
        }

        // Watcher-style scraper: empty while internship count is 0,
        // emits a single notification when it becomes positive.
        assertNotNull(jobs);
    }
}
