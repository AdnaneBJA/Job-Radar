package com.example.job_radar.scraper;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.scraper.selenium.StripeScraper;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.List;

class StripeScraperTest {

    @Test
    void debugPageStructure() throws Exception {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-gpu", "--window-size=1920,1080");
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");
        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get("https://stripe.com/jobs/search?query=intern&remote_locations=North+America--Canada+Remote&office_locations=North+America--Toronto");
            Thread.sleep(8000);

            System.out.println("=== Page title: " + driver.getTitle() + " ===");

            // Check for job listing links
            List<WebElement> jobLinks = driver.findElements(By.cssSelector("a.JobsListings__link"));
            System.out.println("\n=== Job listings: " + jobLinks.size() + " ===");

            for (int i = 0; i < jobLinks.size(); i++) {
                String text = jobLinks.get(i).getText().trim();
                String href = jobLinks.get(i).getAttribute("href");

                // Get the parent tr to inspect all tds
                WebElement tr = jobLinks.get(i).findElement(By.xpath("./ancestor::tr"));
                List<WebElement> tds = tr.findElements(By.cssSelector("td"));
                System.out.printf("  [%d] href='%s'%n", i, href);
                for (int j = 0; j < tds.size(); j++) {
                    System.out.printf("      td[%d] text='%s'%n", j, tds.get(j).getText().trim());
                }
            }

        } finally {
            driver.quit();
        }
    }

    @Test
    void testScrape() {
        StripeScraper scraper = new StripeScraper();
        List<JobPosting> jobs = scraper.scrape();

        System.out.println("=== Scraped " + jobs.size() + " Stripe jobs ===");
        for (JobPosting job : jobs) {
            System.out.printf("Title: %s | Location: %s | Date: %s%n",
                    job.getTitle(), job.getLocation(), job.getDate());
        }
    }
}
