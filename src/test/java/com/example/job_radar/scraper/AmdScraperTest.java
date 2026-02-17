package com.example.job_radar.scraper;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.scraper.selenium.AmdScraper;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.List;

class AmdScraperTest {

    @Test
    void debugPageStructure() throws Exception {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-gpu", "--window-size=1920,1080");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get("https://careers.amd.com/careers-home/jobs");
            Thread.sleep(10000);

            // Find all mat-select elements (Angular Material dropdowns)
            List<WebElement> matSelects = driver.findElements(By.cssSelector("mat-select"));
            System.out.println("=== mat-select elements: " + matSelects.size() + " ===");
            for (int i = 0; i < matSelects.size(); i++) {
                String id = matSelects.get(i).getAttribute("id");
                String cls = matSelects.get(i).getAttribute("class");
                System.out.printf("  mat-select[%d] id='%s' class='%s'%n", i, id, cls);
            }

            // Click Region/Location dropdown (index 1) and list options
            if (matSelects.size() > 1) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", matSelects.get(1));
                Thread.sleep(2000);

                List<WebElement> matOptions = driver.findElements(By.cssSelector("mat-option"));
                System.out.println("\n=== Region/Location options: " + matOptions.size() + " ===");
                for (int i = 0; i < Math.min(10, matOptions.size()); i++) {
                    System.out.printf("  option[%d] text='%s'%n", i, matOptions.get(i).getText().trim());
                }

                List<WebElement> backdrop = driver.findElements(By.cssSelector(".cdk-overlay-backdrop"));
                if (!backdrop.isEmpty()) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", backdrop.get(0));
                    Thread.sleep(1000);
                }
            }

            // Click Categories dropdown (index 3) and list options
            matSelects = driver.findElements(By.cssSelector("mat-select"));
            if (matSelects.size() > 3) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", matSelects.get(3));
                Thread.sleep(2000);

                List<WebElement> matOptions = driver.findElements(By.cssSelector("mat-option"));
                System.out.println("\n=== Categories options: " + matOptions.size() + " ===");
                for (int i = 0; i < matOptions.size(); i++) {
                    System.out.printf("  option[%d] text='%s'%n", i, matOptions.get(i).getText().trim());
                }

                List<WebElement> backdrop = driver.findElements(By.cssSelector(".cdk-overlay-backdrop"));
                if (!backdrop.isEmpty()) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", backdrop.get(0));
                    Thread.sleep(1000);
                }
            }

            // Check job cards
            List<WebElement> jobLinks = driver.findElements(By.cssSelector("a.job-title-link"));
            System.out.println("\n=== Job title links: " + jobLinks.size() + " ===");
            for (int i = 0; i < Math.min(3, jobLinks.size()); i++) {
                String text = jobLinks.get(i).getText().trim();
                System.out.printf("  a[%d] text='%s'%n", i, text);
            }

            List<WebElement> locationSpans = driver.findElements(By.cssSelector("span.label-value.location"));
            System.out.println("\n=== Location spans: " + locationSpans.size() + " ===");
            for (int i = 0; i < Math.min(3, locationSpans.size()); i++) {
                String text = locationSpans.get(i).getText().trim().replaceAll("\\s+", " ");
                System.out.printf("  span[%d] text='%s'%n", i, text);
            }

        } finally {
            driver.quit();
        }
    }

    @Test
    void testScrape() {
        AmdScraper scraper = new AmdScraper();
        List<JobPosting> jobs = scraper.scrape();

        System.out.println("=== Scraped " + jobs.size() + " AMD jobs ===");
        for (JobPosting job : jobs) {
            System.out.printf("Title: %s | Location: %s | Date: %s%n",
                    job.getTitle(), job.getLocation(), job.getDate());
        }
    }
}
