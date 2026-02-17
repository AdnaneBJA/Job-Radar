package com.example.job_radar.scraper;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.scraper.selenium.OracleScraper;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.List;

class OracleScraperTest {

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
            driver.get("https://careers.oracle.com/en/sites/jobsearch/jobs?lastSelectedFacet=AttributeChar6&selectedFlexFieldsFacets=%22AttributeChar6%7C0+to+2%2B+years%22&selectedLocationsFacet=300000000106749");
            Thread.sleep(10000);

            System.out.println("=== Page title: " + driver.getTitle() + " ===");

            // Check for job tile titles
            List<WebElement> titles = driver.findElements(By.cssSelector("span.job-tile__title"));
            System.out.println("\n=== span.job-tile__title: " + titles.size() + " ===");
            for (int i = 0; i < Math.min(5, titles.size()); i++) {
                System.out.printf("  [%d] text='%s'%n", i, titles.get(i).getText().trim());
            }

            // Check for location spans
            List<WebElement> locations = driver.findElements(By.cssSelector("span[data-bind='html: primaryLocation']"));
            System.out.println("\n=== span[data-bind='html: primaryLocation']: " + locations.size() + " ===");
            for (int i = 0; i < Math.min(5, locations.size()); i++) {
                System.out.printf("  [%d] text='%s'%n", i, locations.get(i).getText().trim());
            }

            // If the above didn't work, try broader selectors
            if (titles.isEmpty()) {
                // Try finding any job-tile related elements
                List<WebElement> jobTiles = driver.findElements(By.cssSelector("[class*='job-tile']"));
                System.out.println("\n=== Elements with class containing 'job-tile': " + jobTiles.size() + " ===");
                for (int i = 0; i < Math.min(5, jobTiles.size()); i++) {
                    String tag = jobTiles.get(i).getTagName();
                    String cls = jobTiles.get(i).getAttribute("class");
                    String text = jobTiles.get(i).getText().trim().replaceAll("\\s+", " ");
                    if (text.length() > 200) text = text.substring(0, 200);
                    System.out.printf("  %s[%d] class='%s' text='%s'%n", tag, i, cls, text);
                }

                // Try data-bind attributes
                List<WebElement> dataBind = driver.findElements(By.cssSelector("[data-bind*='title']"));
                System.out.println("\n=== Elements with data-bind containing 'title': " + dataBind.size() + " ===");
                for (int i = 0; i < Math.min(10, dataBind.size()); i++) {
                    String tag = dataBind.get(i).getTagName();
                    String bind = dataBind.get(i).getAttribute("data-bind");
                    String text = dataBind.get(i).getText().trim();
                    if (text.length() > 100) text = text.substring(0, 100);
                    System.out.printf("  %s[%d] data-bind='%s' text='%s'%n", tag, i, bind, text);
                }

                // Check if page loaded at all
                String bodyText = driver.findElement(By.tagName("body")).getText();
                if (bodyText.length() > 2000) bodyText = bodyText.substring(0, 2000);
                System.out.println("\n=== Body text (first 2000 chars): ===\n" + bodyText);
            }

            // Check pagination
            List<WebElement> pagination = driver.findElements(
                    By.cssSelector("[class*='paginat'], [class*='Paginat'], [class*='next'], button[aria-label*='Next']"));
            System.out.println("\n=== Pagination elements: " + pagination.size() + " ===");
            for (int i = 0; i < Math.min(5, pagination.size()); i++) {
                String tag = pagination.get(i).getTagName();
                String cls = pagination.get(i).getAttribute("class");
                String text = pagination.get(i).getText().trim();
                System.out.printf("  %s[%d] class='%s' text='%s'%n", tag, i, cls, text);
            }

        } finally {
            driver.quit();
        }
    }

    @Test
    void testScrape() {
        OracleScraper scraper = new OracleScraper();
        List<JobPosting> jobs = scraper.scrape();

        System.out.println("=== Scraped " + jobs.size() + " Oracle jobs ===");
        for (JobPosting job : jobs) {
            System.out.printf("Title: %s | Location: %s | Date: %s%n",
                    job.getTitle(), job.getLocation(), job.getDate());
        }
    }
}
