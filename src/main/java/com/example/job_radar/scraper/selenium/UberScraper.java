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
import java.util.regex.Pattern;

@Component
public class UberScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(UberScraper.class);

    private static final String BASE_URL =
            "https://www.uber.com/ca/fr-ca/careers/list/?location=CAN-Quebec-Montreal&location=CAN-Ontario-Toronto&department=Engineering";

    private static final String COMPANY = "Uber";

    private static final Pattern JOB_HREF = Pattern.compile(".*/careers/list/\\d+/?$");

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("a[href*='/careers/list/']")));
            Thread.sleep(3000);

            List<WebElement> links = driver.findElements(
                    By.cssSelector("a[href*='/careers/list/']"));

            for (WebElement link : links) {
                try {
                    String href = link.getAttribute("href");
                    if (href == null || !JOB_HREF.matcher(href).matches()) {
                        continue;
                    }

                    String title = link.getText().trim();
                    if (title.isEmpty()) {
                        continue;
                    }

                    JobPosting job = new JobPosting();
                    job.setCompany(COMPANY);
                    job.setTitle(title);
                    job.setLocation("Toronto / Montreal");
                    job.setDate(LocalDate.now().toString());
                    jobs.add(job);
                } catch (Exception e) {
                    log.warn("[Uber] Failed to parse a posting, skipping: {}", e.getMessage());
                }
            }

            log.info("[Uber] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Uber] Error scraping Uber careers", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
