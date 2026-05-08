package com.example.job_radar.scraper.workday;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.scraper.JobScraper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public abstract class WorkdayScraper implements JobScraper {

    private static final Logger log = LoggerFactory.getLogger(WorkdayScraper.class);

    protected abstract String getBaseUrl();

    protected abstract String getCompanyName();

    protected boolean shouldInclude(JobPosting job) {
        return true;
    }

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(getBaseUrl());
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

            // Wait for the job list to load, retry once with page refresh if needed
            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("[data-automation-id='jobResults']")));
            } catch (Exception e) {
                log.warn("[{}] First load attempt failed, refreshing page...", getCompanyName());
                driver.navigate().refresh();
                wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("[data-automation-id='jobResults']")));
            }

            // Allow extra time for all items to render
            Thread.sleep(2000);

            while (true) {
                List<JobPosting> pageJobs = scrapeCurrentPage(driver);
                jobs.addAll(pageJobs);
                log.info("[{}] Scraped {} jobs from current page (total so far: {})",
                        getCompanyName(), pageJobs.size(), jobs.size());

                if (!goToNextPage(driver, wait)) {
                    break;
                }
            }

            log.info("[{}] Scraping complete. Total jobs: {}", getCompanyName(), jobs.size());
        } catch (Exception e) {
            log.error("[{}] Error scraping Workday jobs", getCompanyName(), e);
        } finally {
            driver.quit();
        }

        return jobs;
    }

    private List<JobPosting> scrapeCurrentPage(WebDriver driver) {
        List<JobPosting> jobs = new ArrayList<>();

        // Get all the job list items (parent containers)
        List<WebElement> jobListItems = driver.findElements(
                By.cssSelector("li[class*='css-1q2dra3']"));

        // Fallback: get list items from the job results section
        if (jobListItems.isEmpty()) {
            jobListItems = driver.findElements(
                    By.cssSelector("[data-automation-id='jobResults'] > ul > li"));
        }

        for (WebElement listItem : jobListItems) {
            try {
                JobPosting job = new JobPosting();
                job.setCompany(getCompanyName());

                // Extract job title
                WebElement titleElement = listItem.findElement(
                        By.cssSelector("a[data-automation-id='jobTitle']"));
                job.setTitle(titleElement.getText().trim());

                // Extract location and posted date from dd elements
                List<WebElement> ddElements = listItem.findElements(By.cssSelector("dd"));

                if (ddElements.size() >= 2) {
                    job.setLocation(ddElements.get(0).getText().trim());
                    job.setDate(ddElements.get(1).getText().trim());
                } else if (ddElements.size() == 1) {
                    job.setLocation(ddElements.get(0).getText().trim());
                    job.setDate("");
                } else {
                    List<WebElement> dlItems = listItem.findElements(By.cssSelector("dl"));
                    String fullText = dlItems.isEmpty() ? listItem.getText() : dlItems.get(0).getText();
                    job.setLocation(fullText);
                    job.setDate("");
                }

                if (!job.getTitle().isEmpty() && shouldInclude(job)) {
                    jobs.add(job);
                }
            } catch (Exception e) {
                log.warn("[{}] Failed to parse a job card, skipping: {}", getCompanyName(), e.getMessage());
            }
        }

        return jobs;
    }

    private boolean goToNextPage(WebDriver driver, WebDriverWait wait) {
        try {
            WebElement nextButton = driver.findElement(
                    By.cssSelector("button[aria-label='next']"));

            if (!nextButton.isEnabled() || "true".equals(nextButton.getAttribute("aria-disabled"))) {
                return false;
            }

            nextButton.click();

            Thread.sleep(3000);
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("a[data-automation-id='jobTitle']")));

            return true;
        } catch (Exception e) {
            log.info("[{}] No more pages to navigate", getCompanyName());
            return false;
        }
    }

    private WebDriver createDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");
        return new ChromeDriver(options);
    }
}
