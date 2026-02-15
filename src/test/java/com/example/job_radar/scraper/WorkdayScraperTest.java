package com.example.job_radar.scraper;

import com.example.job_radar.model.JobPosting;
import org.junit.jupiter.api.Test;

import java.util.List;

class WorkdayScraperTest {

    @Test
    void testAutodeskScrape() {
        AutodeskScraper scraper = new AutodeskScraper();
        List<JobPosting> jobs = scraper.scrape();

        System.out.println("=== Scraped " + jobs.size() + " Autodesk jobs ===");
        for (JobPosting job : jobs) {
            System.out.printf("Title: %s | Location: %s | Date: %s%n",
                    job.getTitle(), job.getLocation(), job.getDate());
        }
    }

    @Test
    void testBmoScrape() {
        BmoScraper scraper = new BmoScraper();
        List<JobPosting> jobs = scraper.scrape();

        System.out.println("=== Scraped " + jobs.size() + " BMO jobs ===");
        for (JobPosting job : jobs) {
            System.out.printf("Title: %s | Location: %s | Date: %s%n",
                    job.getTitle(), job.getLocation(), job.getDate());
        }
    }

    @Test
    void testNvidiaScrape() {
        NvidiaScraper scraper = new NvidiaScraper();
        List<JobPosting> jobs = scraper.scrape();

        System.out.println("=== Scraped " + jobs.size() + " NVIDIA jobs (PhD excluded) ===");
        for (JobPosting job : jobs) {
            System.out.printf("Title: %s | Location: %s | Date: %s%n",
                    job.getTitle(), job.getLocation(), job.getDate());
        }
    }
}
