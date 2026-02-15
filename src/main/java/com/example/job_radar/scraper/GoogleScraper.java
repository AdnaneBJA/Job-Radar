package com.example.job_radar.scraper;

import com.example.job_radar.model.JobPosting;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

@Component
public class GoogleScraper implements JobScraper {

    private static final Logger log = LoggerFactory.getLogger(GoogleScraper.class);

    private static final String BASE_URL =
            "https://www.google.com/about/careers/applications/jobs/results"
                    + "?hl=en-US&q=Software&target_level=INTERN_AND_APPRENTICE";

    private static final String COMPANY = "Google";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("li.lLd3Je")));
            Thread.sleep(2000);

            while (true) {
                List<JobPosting> pageJobs = scrapeCurrentPage(driver);
                jobs.addAll(pageJobs);
                log.info("[Google] Scraped {} jobs from current page (total so far: {})",
                        pageJobs.size(), jobs.size());

                if (!goToNextPage(driver, wait)) {
                    break;
                }
            }

            log.info("[Google] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Google] Error scraping Google Careers jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }

    private List<JobPosting> scrapeCurrentPage(WebDriver driver) {
        List<JobPosting> jobs = new ArrayList<>();

        List<WebElement> jobCards = driver.findElements(By.cssSelector("li.lLd3Je"));

        for (WebElement card : jobCards) {
            try {
                // Job title
                WebElement titleEl = card.findElement(By.cssSelector("h3.QJPWVe"));
                String title = titleEl.getText().trim();

                // Skip positions that contain these words
                String upperTitle = title.toUpperCase();
                if (upperTitle.contains("PHD") || upperTitle.contains("CAPITALG")
                        || upperTitle.contains("RESEARCHER")) {
                    continue;
                }

                JobPosting job = new JobPosting();
                job.setCompany(COMPANY);
                job.setTitle(title);

                // Location is in span.r0wTof
                List<WebElement> locationSpans = card.findElements(By.cssSelector("span.r0wTof"));
                if (!locationSpans.isEmpty()) {
                    job.setLocation(locationSpans.get(0).getText().trim());
                } else {
                    job.setLocation("");
                }

                // Google Careers cards don't show a posted date
                job.setDate(LocalDate.now().toString());

                jobs.add(job);
            } catch (Exception e) {
                log.warn("[Google] Failed to parse a job card, skipping: {}", e.getMessage());
            }
        }

        return jobs;
    }

    private boolean goToNextPage(WebDriver driver, WebDriverWait wait) {
        try {
            WebElement nextLink = driver.findElement(
                    By.cssSelector("a[aria-label='Go to next page']"));

            String href = nextLink.getAttribute("href");
            if (href == null || href.isEmpty()) {
                return false;
            }

            nextLink.click();

            Thread.sleep(3000);
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("li.lLd3Je")));

            return true;
        } catch (Exception e) {
            log.info("[Google] No more pages to navigate");
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
