package com.example.job_radar.scraper.ashby;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.scraper.selenium.SeleniumScraper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public abstract class AshbyScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(AshbyScraper.class);

    protected abstract String company();

    protected abstract String baseUrl();

    protected boolean accept(String title, String location) {
        return true;
    }

    protected static boolean isCanada(String location) {
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
        String company = company();
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(baseUrl());
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".ashby-job-posting-brief")));
            Thread.sleep(2000);

            List<WebElement> postings = driver.findElements(
                    By.cssSelector(".ashby-job-posting-brief"));

            for (WebElement posting : postings) {
                try {
                    String title = posting.findElement(
                            By.cssSelector(".ashby-job-posting-brief-title")).getText().trim();

                    String location = "";
                    List<WebElement> detailEls = posting.findElements(
                            By.cssSelector(".ashby-job-posting-brief-details p"));
                    if (!detailEls.isEmpty()) {
                        String[] parts = detailEls.get(0).getText().split("•");
                        if (parts.length > 1) location = parts[1].trim();
                    }

                    if (title.isEmpty() || !accept(title, location)) {
                        continue;
                    }

                    JobPosting job = new JobPosting();
                    job.setCompany(company);
                    job.setTitle(title);
                    job.setLocation(location);
                    job.setDate(LocalDate.now().toString());
                    jobs.add(job);
                } catch (Exception e) {
                    log.warn("[{}] Failed to parse a posting, skipping: {}", company, e.getMessage());
                }
            }

            log.info("[{}] Scraping complete. Total jobs: {}", company, jobs.size());
        } catch (Exception e) {
            log.error("[{}] Error scraping Ashby jobs", company, e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
