package com.example.job_radar.scraper.selenium;

import com.example.job_radar.model.JobPosting;
import org.openqa.selenium.By;
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
public class DatadogScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(DatadogScraper.class);

    private static final String BASE_URL =
            "https://careers.datadoghq.com/all-jobs/?location_Americas%5B0%5D=Canada";

    private static final String COMPANY = "Datadog";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            Thread.sleep(5000);

            // Wait for job cards to load
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("button.job-card")));

            List<WebElement> jobCards = driver.findElements(By.cssSelector("button.job-card"));

            for (WebElement card : jobCards) {
                try {
                    // Title is in h3.job-title
                    String title = "";
                    List<WebElement> titleEls = card.findElements(By.cssSelector("h3.job-title"));
                    if (!titleEls.isEmpty()) {
                        title = titleEls.get(0).getText().trim();
                    }

                    if (title.isEmpty()) continue;

                    // Location is the last line of the card text
                    String location = "";
                    String cardText = card.getText().trim();
                    String[] lines = cardText.split("\n");
                    if (lines.length >= 3) {
                        location = lines[lines.length - 1].trim();
                    }

                    JobPosting job = new JobPosting();
                    job.setCompany(COMPANY);
                    job.setTitle(title);
                    job.setLocation(location);
                    job.setDate(LocalDate.now().toString());
                    jobs.add(job);
                } catch (Exception e) {
                    log.warn("[Datadog] Failed to parse a job card, skipping: {}", e.getMessage());
                }
            }

            log.info("[Datadog] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Datadog] Error scraping Datadog jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
