package com.example.job_radar.scraper;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.scraper.rest.ShopifyScraper;
import org.junit.jupiter.api.Test;

import java.util.List;

class ShopifyScraperTest {

    @Test
    void testScrape() {
        ShopifyScraper scraper = new ShopifyScraper();
        List<JobPosting> jobs = scraper.scrape();

        if (jobs.isEmpty()) {
            System.out.println("=== Shopify: No changes detected — tracked message still present ===");
        } else {
            System.out.println("=== Shopify: Website has changed! " + jobs.size() + " alert(s) ===");
            for (JobPosting job : jobs) {
                System.out.printf("Title: %s | Company: %s | Date: %s%n",
                        job.getTitle(), job.getCompany(), job.getDate());
            }
        }
    }
}
