package com.example.job_radar.scraper.selenium;

import com.example.job_radar.model.JobPosting;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class FigmaScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(FigmaScraper.class);

    private static final String BASE_URL = "https://www.figma.com/careers/#job-openings";
    private static final String COMPANY = "Figma";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(25));
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("a[href*='greenhouse.io/figma']")));
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
            Thread.sleep(2000);

            List<WebElement> links = driver.findElements(
                    By.cssSelector("a[href*='greenhouse.io/figma']"));

            Set<String> seenHrefs = new HashSet<>();

            for (WebElement link : links) {
                try {
                    String href = link.getAttribute("href");
                    if (href == null || !seenHrefs.add(href)) continue;

                    String title = link.getText().trim();
                    if (title.isEmpty()) {
                        title = link.getAttribute("textContent");
                        if (title != null) title = title.trim();
                    }
                    if (title == null || title.isEmpty()) continue;

                    // Figma's listing page renders only titles; only keep titles relevant to SWE/intern.
                    if (!title.toLowerCase().contains("software") && !title.toLowerCase().contains("intern")) {
                        continue;
                    }

                    JobPosting job = new JobPosting();
                    job.setCompany(COMPANY);
                    job.setTitle(title);
                    job.setLocation("");
                    job.setDate(LocalDate.now().toString());
                    jobs.add(job);
                } catch (Exception e) {
                    log.warn("[Figma] Failed to parse a link, skipping: {}", e.getMessage());
                }
            }

            log.info("[Figma] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Figma] Error scraping Figma jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
