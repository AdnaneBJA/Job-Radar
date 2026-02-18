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
public class AdobeScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(AdobeScraper.class);

    private static final String BASE_URL =
            "https://careers.adobe.com/us/en/intern-and-graduate";

    private static final String COMPANY = "Adobe";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            Thread.sleep(5000);

            // Expand Country accordion if collapsed
            expandAccordion(driver, "CountryAccordion");
            Thread.sleep(1000);

            // Check the Canada checkbox (country_phs_0 based on debug)
            clickCanadaCheckbox(driver);
            Thread.sleep(3000);

            // Wait for job cards to load after filtering
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("a[data-ph-at-id='job-link']")));

            // Scrape all pages
            while (true) {
                List<JobPosting> pageJobs = scrapeCurrentPage(driver);
                jobs.addAll(pageJobs);
                log.info("[Adobe] Scraped {} jobs from current page (total so far: {})",
                        pageJobs.size(), jobs.size());

                if (!goToNextPage(driver, wait)) {
                    break;
                }
            }

            log.info("[Adobe] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Adobe] Error scraping Adobe jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }

    private void expandAccordion(WebDriver driver, String accordionId) {
        try {
            WebElement accordion = driver.findElement(By.id(accordionId));
            String expanded = accordion.getAttribute("aria-expanded");
            if (!"true".equals(expanded)) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", accordion);
                log.info("[Adobe] Expanded accordion '{}'", accordionId);
            }
        } catch (Exception e) {
            log.warn("[Adobe] Could not expand accordion '{}': {}", accordionId, e.getMessage());
        }
    }

    private void clickCanadaCheckbox(WebDriver driver) {
        try {
            // Find the Canada checkbox by its aria-label
            List<WebElement> checkboxes = driver.findElements(By.cssSelector("input[id^='country_phs_']"));
            for (WebElement cb : checkboxes) {
                String ariaLabel = cb.getAttribute("aria-label");
                if (ariaLabel != null && ariaLabel.startsWith("Canada")) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cb);
                    log.info("[Adobe] Checked Canada checkbox");
                    return;
                }
            }
            log.warn("[Adobe] Could not find Canada checkbox");
        } catch (Exception e) {
            log.warn("[Adobe] Could not click Canada checkbox: {}", e.getMessage());
        }
    }

    private List<JobPosting> scrapeCurrentPage(WebDriver driver) {
        List<JobPosting> jobs = new ArrayList<>();

        List<WebElement> jobLinks = driver.findElements(
                By.cssSelector("a[data-ph-at-id='job-link']"));
        List<WebElement> dateSpans = driver.findElements(By.cssSelector("span.job-postdate"));

        for (int i = 0; i < jobLinks.size(); i++) {
            try {
                String title = jobLinks.get(i).getAttribute("data-ph-at-job-title-text");
                if (title == null || title.isEmpty()) {
                    title = jobLinks.get(i).getText().trim();
                }

                if (title.isEmpty()) continue;

                // Get the posted date from the corresponding span.job-postdate
                String date = LocalDate.now().toString();
                if (i < dateSpans.size()) {
                    String dateText = dateSpans.get(i).getText().trim();
                    // The text includes "Posted Date" label and the date
                    // Extract just the date part (MM/DD/YYYY)
                    dateText = dateText.replace("Posted Date", "").trim();
                    if (!dateText.isEmpty()) {
                        date = dateText;
                    }
                }

                JobPosting job = new JobPosting();
                job.setCompany(COMPANY);
                job.setTitle(title);
                job.setLocation("Canada");
                job.setDate(date);
                jobs.add(job);
            } catch (Exception e) {
                log.warn("[Adobe] Failed to parse a job card, skipping: {}", e.getMessage());
            }
        }

        return jobs;
    }

    private boolean goToNextPage(WebDriver driver, WebDriverWait wait) {
        try {
            WebElement nextLabel = driver.findElement(
                    By.cssSelector("span[data-ph-at-id='pagination-next-text']"));
            WebElement nextLink = nextLabel.findElement(By.xpath("./ancestor::a"));

            String disabled = nextLink.getAttribute("aria-disabled");
            if ("true".equals(disabled)) {
                return false;
            }

            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", nextLink);
            Thread.sleep(3000);

            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("a[data-ph-at-id='job-link']")));

            return true;
        } catch (Exception e) {
            log.info("[Adobe] No more pages to navigate");
            return false;
        }
    }
}
