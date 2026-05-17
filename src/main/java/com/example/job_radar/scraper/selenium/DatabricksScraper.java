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
public class DatabricksScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(DatabricksScraper.class);

    private static final String BASE_URL =
            "https://www.databricks.com/company/careers/open-positions"
            + "?department=University%20Recruiting&location=all";

    private static final String COMPANY = "Databricks";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("#jobWrap a[href*='/company/careers/']")));
            Thread.sleep(2000);

            List<WebElement> jobLinks = driver.findElements(
                    By.cssSelector("#jobWrap a[href*='/company/careers/']"));

            for (WebElement link : jobLinks) {
                try {
                    List<WebElement> spans = link.findElements(By.cssSelector("span"));
                    String title = "";
                    String location = "";

                    if (spans.size() >= 2) {
                        title = spans.get(0).getText().trim();
                        location = spans.get(1).getText().trim();
                    } else {
                        String aria = link.getAttribute("aria-label");
                        if (aria != null) {
                            title = aria.replaceAll("\\s+\\d+$", "").trim();
                        }
                    }

                    if (title.isEmpty()) continue;

                    JobPosting job = new JobPosting();
                    job.setCompany(COMPANY);
                    job.setTitle(title);
                    job.setLocation(location);
                    job.setDate(LocalDate.now().toString());
                    jobs.add(job);
                } catch (Exception e) {
                    log.warn("[Databricks] Failed to parse a job link, skipping: {}", e.getMessage());
                }
            }

            log.info("[Databricks] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Databricks] Error scraping Databricks jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
