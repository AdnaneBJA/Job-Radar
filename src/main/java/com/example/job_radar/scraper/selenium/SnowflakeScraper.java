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
public class SnowflakeScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(SnowflakeScraper.class);

    private static final String BASE_URL = "https://careers.snowflake.com/us/en/search-results";
    private static final String COMPANY = "Snowflake";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("a[data-ph-at-id='job-link']")));
            Thread.sleep(2000);

            // Click the "Intern" checkbox under Job Level (id='Job Level-1')
            JavascriptExecutor js = (JavascriptExecutor) driver;
            List<WebElement> internCb = driver.findElements(
                    By.cssSelector("input[id='Job Level-1']"));
            if (internCb.isEmpty()) {
                log.error("[Snowflake] Could not find Job Level=Intern checkbox");
                return jobs;
            }
            js.executeScript("arguments[0].click();", internCb.get(0));
            Thread.sleep(4000);

            List<WebElement> jobLinks = driver.findElements(
                    By.cssSelector("a[data-ph-at-id='job-link']"));

            for (WebElement link : jobLinks) {
                try {
                    String title = link.getAttribute("data-ph-at-job-title-text");
                    String location = link.getAttribute("data-ph-at-job-location-text");
                    if (title == null || title.isEmpty()) {
                        continue;
                    }

                    JobPosting job = new JobPosting();
                    job.setCompany(COMPANY);
                    job.setTitle(title.trim());
                    job.setLocation(location == null ? "" : location.trim());
                    job.setDate(LocalDate.now().toString());
                    jobs.add(job);
                } catch (Exception e) {
                    log.warn("[Snowflake] Failed to parse a posting, skipping: {}", e.getMessage());
                }
            }

            log.info("[Snowflake] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Snowflake] Error scraping Snowflake careers", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
