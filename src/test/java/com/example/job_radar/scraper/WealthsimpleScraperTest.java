package com.example.job_radar.scraper;

import com.example.job_radar.model.JobPosting;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.List;

class WealthsimpleScraperTest {

    @Test
    void debugPageStructure() throws Exception {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-gpu", "--window-size=1920,1080");
        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get("https://jobs.lever.co/wealthsimple?location=Canada");
            Thread.sleep(5000);

            System.out.println("Page title: " + driver.getTitle());

            // Try common Lever selectors
            String[] selectors = {
                    ".posting", "a.posting-title", ".posting-name",
                    "h5", ".sort-by-team", ".posting-category",
                    "[data-qa]", ".postings-group", ".posting-apply",
                    "a[href*='/wealthsimple/']"
            };

            for (String sel : selectors) {
                List<WebElement> els = driver.findElements(By.cssSelector(sel));
                if (!els.isEmpty()) {
                    System.out.println("\n--- Found " + els.size() + " elements with: " + sel);
                    for (int i = 0; i < Math.min(3, els.size()); i++) {
                        String text = els.get(i).getText().replace("\n", " | ");
                        if (text.length() > 200) text = text.substring(0, 200) + "...";
                        System.out.println("  [" + i + "] tag=" + els.get(i).getTagName()
                                + " class=" + els.get(i).getAttribute("class")
                                + " text=" + text);
                    }
                }
            }

            // Print first posting innerHTML if found
            List<WebElement> postings = driver.findElements(By.cssSelector(".posting"));
            if (!postings.isEmpty()) {
                String html = postings.get(0).getAttribute("innerHTML");
                System.out.println("\n=== First .posting innerHTML ===");
                System.out.println(html.substring(0, Math.min(1500, html.length())));
            }
        } finally {
            driver.quit();
        }
    }

    @Test
    void testScrape() {
        WealthsimpleScraper scraper = new WealthsimpleScraper();
        List<JobPosting> jobs = scraper.scrape();

        System.out.println("=== Scraped " + jobs.size() + " Wealthsimple jobs ===");
        for (JobPosting job : jobs) {
            System.out.printf("Title: %s | Location: %s | Date: %s%n",
                    job.getTitle(), job.getLocation(), job.getDate());
        }
    }
}
