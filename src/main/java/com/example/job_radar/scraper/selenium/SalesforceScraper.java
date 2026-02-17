package com.example.job_radar.scraper.selenium;

import com.example.job_radar.model.JobPosting;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class SalesforceScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(SalesforceScraper.class);

    private static final String BASE_URL =
            "https://careers.salesforce.com/en/jobs/";

    private static final String COMPANY = "Salesforce";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            Thread.sleep(3000);

            // Select Country -> Canada using Tom Select
            selectTomSelectOption(driver, "l-country", "Canada");
            Thread.sleep(2000);

            // Select Employee Type -> Intern using Tom Select
            selectTomSelectOption(driver, "l-jobtype", "Intern");
            Thread.sleep(2000);

            // Click the Search button to apply filters
            WebElement searchBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("button.js-search-jobs, button[type='submit']")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", searchBtn);
            Thread.sleep(5000);

            // Wait for job cards to load
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("div.card")));

            // Scrape all pages
            while (true) {
                List<JobPosting> pageJobs = scrapeCurrentPage(driver);
                jobs.addAll(pageJobs);
                log.info("[Salesforce] Scraped {} jobs from current page (total so far: {})",
                        pageJobs.size(), jobs.size());

                if (!goToNextPage(driver, wait)) {
                    break;
                }
            }

            log.info("[Salesforce] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Salesforce] Error scraping Salesforce jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }

    private void selectTomSelectOption(WebDriver driver, String selectId, String value) {
        try {
            // Tom Select wraps the native <select> — use JS to set the value directly
            ((JavascriptExecutor) driver).executeScript(
                    "var el = document.getElementById('" + selectId + "');"
                    + "var ts = el.tomselect;"
                    + "ts.setValue('" + value + "');");
            log.info("[Salesforce] Selected '{}' for '{}'", value, selectId);
        } catch (Exception e) {
            log.warn("[Salesforce] Could not select '{}' for '{}': {}", value, selectId, e.getMessage());
        }
    }

    private List<JobPosting> scrapeCurrentPage(WebDriver driver) {
        List<JobPosting> jobs = new ArrayList<>();

        List<WebElement> jobCards = driver.findElements(By.cssSelector("div.card"));

        for (WebElement card : jobCards) {
            try {
                // Title is in h3.card-title > a.js-view-job
                String title = "";
                List<WebElement> titleLinks = card.findElements(
                        By.cssSelector("h3.card-title a.js-view-job"));
                if (!titleLinks.isEmpty()) {
                    title = titleLinks.get(0).getText().trim();
                }

                if (title.isEmpty()) continue;

                // Location is in ul.locations
                String location = "";
                List<WebElement> locationEls = card.findElements(By.cssSelector("ul.locations"));
                if (!locationEls.isEmpty()) {
                    location = locationEls.get(0).getText().trim();
                }

                JobPosting job = new JobPosting();
                job.setCompany(COMPANY);
                job.setTitle(title);
                job.setLocation(location);
                job.setDate(LocalDate.now().toString());
                jobs.add(job);
            } catch (Exception e) {
                log.warn("[Salesforce] Failed to parse a job card, skipping: {}", e.getMessage());
            }
        }

        return jobs;
    }

    private boolean goToNextPage(WebDriver driver, WebDriverWait wait) {
        try {
            // Find the active page in pagination, then get the next sibling
            WebElement activePage = driver.findElement(
                    By.cssSelector("ul.pagination li.active"));
            WebElement nextLi = activePage.findElement(By.xpath("following-sibling::li[1]"));

            List<WebElement> nextLink = nextLi.findElements(By.cssSelector("a.page-link"));
            if (nextLink.isEmpty()) {
                return false;
            }

            String text = nextLink.get(0).getText().trim();
            if (text.isEmpty()) {
                return false;
            }

            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", nextLink.get(0));
            Thread.sleep(3000);

            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("div.card")));

            return true;
        } catch (Exception e) {
            log.info("[Salesforce] No more pages to navigate");
            return false;
        }
    }
}
