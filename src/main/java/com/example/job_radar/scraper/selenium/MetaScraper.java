package com.example.job_radar.scraper.selenium;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.scraper.JobScraper;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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
public class MetaScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(MetaScraper.class);

    // The old jobsearch URL pinned specific teams[] values that Meta has since renamed, so the
    // filter matched nothing and the wait timed out. roles[0]=Internship is the stable filter;
    // we keep all internship teams and let the radar surface them.
    private static final String BASE_URL =
            "https://www.metacareers.com/jobs?roles[0]=Internship";

    private static final String COMPANY = "Meta";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("a[href*='/profile/job_details/']")));
            Thread.sleep(3000);

            // Scroll down to load all jobs (Meta uses infinite scroll)
            scrollToLoadAll(driver);

            List<WebElement> jobLinks = driver.findElements(
                    By.cssSelector("a[href*='/profile/job_details/']"));

            for (WebElement link : jobLinks) {
                try {
                    String fullText = link.getText();
                    String[] lines = fullText.split("\n");

                    if (lines.length < 2) {
                        continue;
                    }

                    String title = lines[0].trim();
                    String location = lines[1].trim();

                    // Skip PhD positions
                    String upperTitle = title.toUpperCase();
                    if (upperTitle.contains("PHD")) {
                        continue;
                    }

                    JobPosting job = new JobPosting();
                    job.setCompany(COMPANY);
                    job.setTitle(title);
                    job.setLocation(location);
                    job.setDate(LocalDate.now().toString());

                    if (!title.isEmpty()) {
                        jobs.add(job);
                    }
                } catch (Exception e) {
                    log.warn("[Meta] Failed to parse a job card, skipping: {}", e.getMessage());
                }
            }

            log.info("[Meta] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Meta] Error scraping Meta Careers jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }

    private void scrollToLoadAll(WebDriver driver) throws InterruptedException {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        int previousCount = 0;
        int stableRounds = 0;

        while (stableRounds < 3) {
            js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
            Thread.sleep(2000);

            int currentCount = driver.findElements(
                    By.cssSelector("a[href*='/profile/job_details/']")).size();

            if (currentCount == previousCount) {
                stableRounds++;
            } else {
                stableRounds = 0;
            }
            previousCount = currentCount;
            log.info("[Meta] Scrolling... {} jobs loaded so far", currentCount);
        }
    }
}
