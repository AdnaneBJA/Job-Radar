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
public class PalantirScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(PalantirScraper.class);

    private static final String BASE_URL =
            "https://www.palantir.com/careers/open-positions/?commitment=Internship";

    private static final String COMPANY = "Palantir";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("a[href*='lever.co/palantir']")));
            Thread.sleep(2000);

            List<WebElement> items = driver.findElements(
                    By.cssSelector("li[class*='ptcom-design__listItem']"));

            for (WebElement item : items) {
                try {
                    List<WebElement> leverLinks = item.findElements(
                            By.cssSelector("a[href*='lever.co/palantir']"));
                    if (leverLinks.isEmpty()) continue;

                    WebElement btn = item.findElement(By.cssSelector("button"));
                    String title = btn.getText().trim();
                    // Strip trailing arrow/symbol artifacts
                    title = title.replaceAll("\\s+[\\u2192\\u2190\\u00bb→←»?]+\\s*$", "").trim();
                    if (title.isEmpty()) continue;

                    List<String> locations = new ArrayList<>();
                    for (WebElement a : leverLinks) {
                        // The sublist is collapsed until the parent button is expanded, so
                        // getText() returns "" — read textContent directly instead.
                        String locText = a.getAttribute("textContent");
                        if (locText == null) continue;
                        // Strip any non-letter prefix (the CSS-rendered icon character is a
                        // private-use codepoint that surfaces as a stray '?' in text).
                        locText = locText.replaceAll("^[^\\p{L}\\p{N}]+", "").trim();
                        if (!locText.isEmpty()) locations.add(locText);
                    }
                    String location = String.join("; ", locations);

                    JobPosting job = new JobPosting();
                    job.setCompany(COMPANY);
                    job.setTitle(title);
                    job.setLocation(location);
                    job.setDate(LocalDate.now().toString());
                    jobs.add(job);
                } catch (Exception e) {
                    log.warn("[Palantir] Failed to parse a list item, skipping: {}", e.getMessage());
                }
            }

            log.info("[Palantir] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Palantir] Error scraping Palantir jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
