package com.example.job_radar.scraper;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.scraper.selenium.SapScraper;
import org.junit.jupiter.api.Test;

import java.util.List;

class SapScraperTest {

    @Test 
    void testScrape() {
        SapScraper scraper = new SapScraper();
        List<JobPosting> jobs = scraper.scrape();

        System.out.println("=== Scraped " + jobs.size() + " SAP jobs ===");
        for (JobPosting job : jobs) {
            System.out.printf("Title: %s | Location: %s | Date: %s%n",
                    job.getTitle(), job.getLocation(), job.getDate());
        }
    }
}
