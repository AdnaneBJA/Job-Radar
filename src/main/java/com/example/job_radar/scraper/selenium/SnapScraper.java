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
public class SnapScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(SnapScraper.class);

    private static final String BASE_URL =
            "https://careers.snap.com/jobs?type=Intern&role=Engineering";

    private static final String COMPANY = "Snap";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(25));
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("a[href*='/job?id=']")));
            Thread.sleep(3000);

            List<WebElement> rows = driver.findElements(
                    By.xpath("//a[contains(@href, '/job?id=')]/ancestor::tr[1]"));

            for (WebElement row : rows) {
                try {
                    List<WebElement> tds = row.findElements(By.cssSelector("td"));
                    if (tds.isEmpty()) continue;

                    String title = tds.get(0).getText().trim();
                    String location = "";
                    if (tds.size() >= 3) {
                        location = tds.get(tds.size() - 1).getText().trim();
                    }

                    if (title.isEmpty()) continue;

                    JobPosting job = new JobPosting();
                    job.setCompany(COMPANY);
                    job.setTitle(title);
                    job.setLocation(location);
                    job.setDate(LocalDate.now().toString());
                    jobs.add(job);
                } catch (Exception e) {
                    log.warn("[Snap] Failed to parse a row, skipping: {}", e.getMessage());
                }
            }

            log.info("[Snap] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Snap] Error scraping Snap jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
