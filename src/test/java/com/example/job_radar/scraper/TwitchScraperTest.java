package com.example.job_radar.scraper;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.scraper.selenium.TwitchScraper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class TwitchScraperTest {

    @Test
    void testScrape() {
        TwitchScraper scraper = new TwitchScraper();
        List<JobPosting> jobs = scraper.scrape();

        System.out.println("=== Scraped " + jobs.size() + " Twitch notifications ===");
        for (JobPosting job : jobs) {
            System.out.printf("Title: %s | Location: %s | Date: %s%n",
                    job.getTitle(), job.getLocation(), job.getDate());
        }

        // Watcher-style scraper: jobs is empty while Twitch's "applications closed"
        // marker is present, and emits a single notification when it disappears.
        assertNotNull(jobs);
    }
}
