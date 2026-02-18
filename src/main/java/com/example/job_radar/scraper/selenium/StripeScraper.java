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
public class StripeScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(StripeScraper.class);

    private static final String BASE_URL =
            "https://stripe.com/jobs/search?query=intern"
            + "&remote_locations=North+America--Canada+Remote"
            + "&office_locations=North+America--Toronto";

    private static final String COMPANY = "Stripe";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            Thread.sleep(5000);

            // Wait for job listing links to load
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("a.JobsListings__link")));

            // Scrape all job rows from the table
            List<WebElement> jobLinks = driver.findElements(By.cssSelector("a.JobsListings__link"));

            for (WebElement link : jobLinks) {
                try {
                    String title = link.getText().trim();
                    if (title.isEmpty()) continue;

                    // Navigate to the parent tr to get the location from td[2]
                    WebElement tr = link.findElement(By.xpath("./ancestor::tr"));
                    List<WebElement> tds = tr.findElements(By.cssSelector("td"));

                    String location = "";
                    if (tds.size() > 2) {
                        location = tds.get(2).getText().trim();
                    }

                    JobPosting job = new JobPosting();
                    job.setCompany(COMPANY);
                    job.setTitle(title);
                    job.setLocation(location);
                    job.setDate(LocalDate.now().toString());
                    jobs.add(job);
                } catch (Exception e) {
                    log.warn("[Stripe] Failed to parse a job row, skipping: {}", e.getMessage());
                }
            }

            log.info("[Stripe] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Stripe] Error scraping Stripe jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
