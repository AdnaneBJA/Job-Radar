package com.example.job_radar.scraper;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.scraper.selenium.RbcScraper;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.List;

class RbcScraperTest {

    @Test
    void debugPageStructure() throws Exception {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-gpu", "--window-size=1920,1080");
        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get("https://jobs.rbc.com/ca/en/search-results?s=1");
            Thread.sleep(8000);

            JavascriptExecutor js = (JavascriptExecutor) driver;

            // Expand Job Type accordion
            List<WebElement> jobTypeAcc = driver.findElements(By.id("JobTypeAccordion"));
            if (!jobTypeAcc.isEmpty()) {
                js.executeScript("arguments[0].click();", jobTypeAcc.get(0));
                Thread.sleep(1000);
            }
            // Expand Country accordion
            List<WebElement> countryAcc = driver.findElements(By.id("CountryAccordion"));
            if (!countryAcc.isEmpty()) {
                js.executeScript("arguments[0].click();", countryAcc.get(0));
                Thread.sleep(1000);
            }

            // Dump first 80 checkboxes (to see Job Type and Country filters)
            List<WebElement> checkboxes = driver.findElements(By.cssSelector("input[type='checkbox']"));
            System.out.println("=== First 80 Checkboxes (total=" + checkboxes.size() + ") ===");
            for (int i = 0; i < Math.min(80, checkboxes.size()); i++) {
                String ariaLabel = checkboxes.get(i).getAttribute("aria-label");
                if (ariaLabel != null && !ariaLabel.isEmpty()) {
                    System.out.printf("  [%d] aria-label='%s'%n", i, ariaLabel);
                }
            }

            // Also check pagination alternatives
            List<WebElement> showMore = driver.findElements(By.cssSelector("button[data-ph-at-id*='load-more'], button[class*='show-more'], [data-ph-at-id*='more']"));
            System.out.println("\n=== Show more / load more buttons: " + showMore.size() + " ===");
            for (int i = 0; i < showMore.size(); i++) {
                String text = showMore.get(i).getText().trim();
                String cls = showMore.get(i).getAttribute("class");
                System.out.printf("  [%d] class='%s' text='%s'%n", i, cls, text);
            }

            // Check for pagination links
            List<WebElement> pagLinks = driver.findElements(By.cssSelector("a[class*='pagination'], [class*='paginat'] a"));
            System.out.println("\n=== Pagination links: " + pagLinks.size() + " ===");
            for (int i = 0; i < Math.min(5, pagLinks.size()); i++) {
                String text = pagLinks.get(i).getText().trim();
                String cls = pagLinks.get(i).getAttribute("class");
                System.out.printf("  [%d] class='%s' text='%s'%n", i, cls, text);
            }

            // Check accordion elements
            List<WebElement> accordions = driver.findElements(By.cssSelector("[id*='Accordion']"));
            System.out.println("\n=== Accordion elements: " + accordions.size() + " ===");
            for (int i = 0; i < accordions.size(); i++) {
                String id = accordions.get(i).getAttribute("id");
                String text = accordions.get(i).getText().replace("\n", " ").trim();
                if (text.length() > 80) text = text.substring(0, 80);
                System.out.printf("  [%d] id='%s' text='%s'%n", i, id, text);
            }

            // Check job links
            List<WebElement> jobLinks = driver.findElements(By.cssSelector("a[data-ph-at-id='job-link']"));
            System.out.println("\n=== Job links: " + jobLinks.size() + " ===");
            for (int i = 0; i < Math.min(3, jobLinks.size()); i++) {
                String title = jobLinks.get(i).getAttribute("data-ph-at-job-title-text");
                String href = jobLinks.get(i).getAttribute("href");
                System.out.printf("  [%d] title='%s' href='%s'%n", i, title, href);
            }

            // Check location elements
            List<WebElement> locEls = driver.findElements(By.xpath("//*[contains(@class, 'location')]"));
            System.out.println("\n=== Elements with 'location' in class: " + locEls.size() + " ===");
            for (int i = 0; i < Math.min(8, locEls.size()); i++) {
                String tag = locEls.get(i).getTagName();
                String cls = locEls.get(i).getAttribute("class");
                String text = locEls.get(i).getText().replace("\n", " | ").trim();
                if (text.length() > 100) text = text.substring(0, 100);
                System.out.printf("  [%d] tag='%s' class='%s' text='%s'%n", i, tag, cls, text);
            }

            // Check pagination
            List<WebElement> pagination = driver.findElements(By.cssSelector("span[data-ph-at-id='pagination-next-text']"));
            System.out.println("\n=== Pagination next: " + pagination.size() + " ===");

        } finally {
            driver.quit();
        }
    }

    @Test
    void testScrape() {
        RbcScraper scraper = new RbcScraper();
        List<JobPosting> jobs = scraper.scrape();

        System.out.println("=== Scraped " + jobs.size() + " RBC jobs ===");
        for (JobPosting job : jobs) {
            System.out.printf("Title: %s | Location: %s | Date: %s%n",
                    job.getTitle(), job.getLocation(), job.getDate());
        }
    }
}
