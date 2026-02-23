package com.example.job_radar.scraper.selenium;

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
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class WealthsimpleScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(WealthsimpleScraper.class);

    private static final String BASE_URL = "https://jobs.ashbyhq.com/wealthsimple?employmentType=Intern";
    private static final String COMPANY = "Wealthsimple";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".ashby-job-posting-brief")));
            Thread.sleep(2000);

            List<WebElement> postings = driver.findElements(By.cssSelector(".ashby-job-posting-brief"));

            for (WebElement posting : postings) {
                try {
                    WebElement titleEl = posting.findElement(By.cssSelector(".ashby-job-posting-brief-title"));
                    String title = titleEl.getText().trim();

                    JobPosting job = new JobPosting();
                    job.setCompany(COMPANY);
                    job.setTitle(title);

                    // Details paragraph format: "Department • Location • Type • Mode"
                    List<WebElement> detailEls = posting.findElements(
                            By.cssSelector(".ashby-job-posting-brief-details p"));
                    if (!detailEls.isEmpty()) {
                        String[] parts = detailEls.get(0).getText().split("•");
                        job.setLocation(parts.length > 1 ? parts[1].trim() : "");
                    } else {
                        job.setLocation("");
                    }

                    // Ashby doesn't show posted date on listings
                    job.setDate(LocalDate.now().toString());

                    if (!title.isEmpty()) {
                        jobs.add(job);
                    }
                } catch (Exception e) {
                    log.warn("[Wealthsimple] Failed to parse a posting, skipping: {}", e.getMessage());
                }
            }

            log.info("[Wealthsimple] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Wealthsimple] Error scraping Ashby jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }

}
