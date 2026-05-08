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
public class CohereScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(CohereScraper.class);

    private static final String BASE_URL = "https://jobs.ashbyhq.com/cohere?employmentType=Intern";
    private static final String COMPANY = "Cohere";

    private static boolean isCanada(String location) {
        if (location == null) return false;
        String l = location.toLowerCase();
        return l.contains("canada")
                || l.contains("toronto")
                || l.contains("montreal")
                || l.contains("vancouver")
                || l.contains("ottawa");
    }

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".ashby-job-posting-brief")));
            Thread.sleep(2000);

            List<WebElement> postings = driver.findElements(
                    By.cssSelector(".ashby-job-posting-brief"));

            for (WebElement posting : postings) {
                try {
                    WebElement titleEl = posting.findElement(
                            By.cssSelector(".ashby-job-posting-brief-title"));
                    String title = titleEl.getText().trim();

                    String location = "";
                    List<WebElement> detailEls = posting.findElements(
                            By.cssSelector(".ashby-job-posting-brief-details p"));
                    if (!detailEls.isEmpty()) {
                        String[] parts = detailEls.get(0).getText().split("•");
                        if (parts.length > 1) location = parts[1].trim();
                    }

                    if (title.isEmpty() || !isCanada(location)) {
                        continue;
                    }

                    JobPosting job = new JobPosting();
                    job.setCompany(COMPANY);
                    job.setTitle(title);
                    job.setLocation(location);
                    job.setDate(LocalDate.now().toString());
                    jobs.add(job);
                } catch (Exception e) {
                    log.warn("[Cohere] Failed to parse a posting, skipping: {}", e.getMessage());
                }
            }

            log.info("[Cohere] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Cohere] Error scraping Ashby jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
