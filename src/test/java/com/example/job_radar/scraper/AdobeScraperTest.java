package com.example.job_radar.scraper;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.scraper.selenium.AdobeScraper;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.List;

class AdobeScraperTest {

    @Test
    void debugPageStructure() throws Exception {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-gpu", "--window-size=1920,1080");
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");
        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get("https://careers.adobe.com/us/en/intern-and-graduate");
            Thread.sleep(8000);

            System.out.println("=== Page title: " + driver.getTitle() + " ===");

            // Check Country accordion
            List<WebElement> countryAccordion = driver.findElements(By.id("CountryAccordion"));
            System.out.println("\n=== #CountryAccordion found: " + countryAccordion.size() + " ===");

            // Check country checkboxes
            List<WebElement> checkboxes = driver.findElements(By.cssSelector("input[id^='country_phs_']"));
            System.out.println("\n=== Country checkboxes: " + checkboxes.size() + " ===");
            for (int i = 0; i < checkboxes.size(); i++) {
                String id = checkboxes.get(i).getAttribute("id");
                String ariaLabel = checkboxes.get(i).getAttribute("aria-label");
                System.out.printf("  checkbox[%d] id='%s' aria-label='%s'%n", i, id, ariaLabel);
            }

            // Check for job links
            List<WebElement> jobLinks = driver.findElements(By.cssSelector("a[data-ph-at-id='job-link']"));
            System.out.println("\n=== Job links: " + jobLinks.size() + " ===");
            for (int i = 0; i < Math.min(5, jobLinks.size()); i++) {
                String title = jobLinks.get(i).getAttribute("data-ph-at-job-title-text");
                System.out.printf("  [%d] title='%s'%n", i, title);
            }

            // Check for posted dates
            List<WebElement> dateSpans = driver.findElements(By.cssSelector("span.job-postdate"));
            System.out.println("\n=== Posted dates (span.job-postdate): " + dateSpans.size() + " ===");
            for (int i = 0; i < Math.min(5, dateSpans.size()); i++) {
                String text = dateSpans.get(i).getText().trim().replace("\n", " ");
                System.out.printf("  [%d] text='%s'%n", i, text);
            }

        } finally {
            driver.quit();
        }
    }

    @Test
    void testScrape() {
        AdobeScraper scraper = new AdobeScraper();
        List<JobPosting> jobs = scraper.scrape();

        System.out.println("=== Scraped " + jobs.size() + " Adobe jobs ===");
        for (JobPosting job : jobs) {
            System.out.printf("Title: %s | Location: %s | Date: %s%n",
                    job.getTitle(), job.getLocation(), job.getDate());
        }
    }
}
