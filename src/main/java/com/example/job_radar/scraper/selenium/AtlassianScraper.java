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
public class AtlassianScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(AtlassianScraper.class);

    private static final String BASE_URL =
            "https://www.atlassian.com/company/careers/all-jobs?team=Interns&location=&search=";

    private static final String COMPANY = "Atlassian";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(25));
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("a[href*='/careers/details/']")));
            Thread.sleep(2000);

            List<WebElement> rows = driver.findElements(
                    By.xpath("//a[contains(@href, '/careers/details/')]/ancestor::tr[1]"));

            for (WebElement row : rows) {
                try {
                    List<WebElement> tds = row.findElements(By.cssSelector("td"));
                    if (tds.size() < 2) continue;

                    String title = tds.get(0).getText().trim();
                    String location = tds.get(1).getText().trim();
                    if (title.isEmpty()) continue;

                    JobPosting job = new JobPosting();
                    job.setCompany(COMPANY);
                    job.setTitle(title);
                    job.setLocation(location);
                    job.setDate(LocalDate.now().toString());
                    jobs.add(job);
                } catch (Exception e) {
                    log.warn("[Atlassian] Failed to parse a row, skipping: {}", e.getMessage());
                }
            }

            log.info("[Atlassian] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Atlassian] Error scraping Atlassian jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
