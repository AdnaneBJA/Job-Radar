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
public class IntuitScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(IntuitScraper.class);

    private static final String BASE_URL =
            "https://jobs.intuit.com/search-jobs?glat=45.5285&glon=-73.6104";

    private static final String COMPANY = "Intuit";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            // Wait for filter section to load
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.id("search-filters")));
            Thread.sleep(2000);

            // 1. Expand Country filter and check "Canada"
            clickFilterCheckbox(driver, wait, "Country", "Canada");
            Thread.sleep(3000);

            // 2. Expand Job Category filter and check "Internship"
            clickFilterCheckbox(driver, wait, "Job Category", "Internship");
            Thread.sleep(3000);

            // Wait for results to load after filtering
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.id("search-results-list")));

            // Scrape all pages
            while (true) {
                List<JobPosting> pageJobs = scrapeCurrentPage(driver);
                jobs.addAll(pageJobs);
                log.info("[Intuit] Scraped {} jobs from current page (total so far: {})",
                        pageJobs.size(), jobs.size());

                if (!goToNextPage(driver, wait)) {
                    break;
                }
            }

            log.info("[Intuit] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Intuit] Error scraping Intuit jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }

    private void clickFilterCheckbox(WebDriver driver, WebDriverWait wait,
                                     String filterName, String optionName) {
        try {
            // Find the expandable section button that contains the filter name
            WebElement filterButton = driver.findElement(
                    By.xpath("//button[contains(@class,'expandable-parent') and contains(.,'" + filterName + "')]"));

            // Click to expand the filter section if it's collapsed
            String expanded = filterButton.getAttribute("aria-expanded");
            if (!"true".equals(expanded)) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", filterButton);
                Thread.sleep(1000);
            }

            // Find and click the checkbox by its data-display attribute
            WebElement checkbox = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("input.filter-checkbox[data-display='" + optionName + "']")));

            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", checkbox);
            log.info("[Intuit] Checked '{}' under '{}'", optionName, filterName);
        } catch (Exception e) {
            log.warn("[Intuit] Could not click filter '{}' > '{}': {}", filterName, optionName, e.getMessage());
        }
    }

    private List<JobPosting> scrapeCurrentPage(WebDriver driver) {
        List<JobPosting> jobs = new ArrayList<>();

        List<WebElement> jobItems = driver.findElements(
                By.cssSelector("#search-results-list li"));

        for (WebElement item : jobItems) {
            try {
                WebElement titleEl = item.findElement(By.cssSelector("a.sr-item h2"));
                String title = titleEl.getText().trim();

                if (title.isEmpty()) {
                    continue;
                }

                // Location is typically in a span with job-location class or second text element
                String location = "";
                List<WebElement> locationEls = item.findElements(By.cssSelector("span.job-location"));
                if (!locationEls.isEmpty()) {
                    location = locationEls.get(0).getText().trim();
                } else {
                    // Fallback: try getting location from the item text minus the title
                    String fullText = item.getText();
                    String[] lines = fullText.split("\n");
                    for (String line : lines) {
                        String trimmed = line.trim();
                        if (!trimmed.equals(title) && !trimmed.equalsIgnoreCase("Save")
                                && !trimmed.isEmpty()) {
                            location = trimmed;
                            break;
                        }
                    }
                }

                JobPosting job = new JobPosting();
                job.setCompany(COMPANY);
                job.setTitle(title);
                job.setLocation(location);
                job.setDate(LocalDate.now().toString());
                jobs.add(job);
            } catch (Exception e) {
                log.warn("[Intuit] Failed to parse a job card, skipping: {}", e.getMessage());
            }
        }

        return jobs;
    }

    private boolean goToNextPage(WebDriver driver, WebDriverWait wait) {
        try {
            WebElement nextLink = driver.findElement(
                    By.cssSelector("a.next"));

            // If the next link has class "disabled", we're on the last page
            String classes = nextLink.getAttribute("class");
            if (classes != null && classes.contains("disabled")) {
                return false;
            }

            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", nextLink);
            Thread.sleep(3000);

            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.id("search-results-list")));

            return true;
        } catch (Exception e) {
            log.info("[Intuit] No more pages to navigate");
            return false;
        }
    }
}
