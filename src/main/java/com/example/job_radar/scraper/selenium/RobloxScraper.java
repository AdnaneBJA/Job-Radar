package com.example.job_radar.scraper.selenium;

import com.example.job_radar.model.JobPosting;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class RobloxScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(RobloxScraper.class);

    private static final String BASE_URL = "https://careers.roblox.com/jobs?type=internship";

    private static final String COMPANY = "Roblox";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            Thread.sleep(10000);

            String body = "";
            try {
                body = driver.findElement(By.tagName("body")).getText();
            } catch (Exception ignored) { }

            if (body.contains("Application error") || body.contains("client-side exception")) {
                log.warn("[Roblox] Page rendered an application error in this environment; skipping");
                return jobs;
            }

            List<WebElement> jobLinks = driver.findElements(
                    By.cssSelector("a[href*='/jobs/'], a[href*='/job/']"));

            for (WebElement link : jobLinks) {
                try {
                    String href = link.getAttribute("href");
                    if (href == null || href.endsWith("/jobs") || href.contains("type=")) continue;
                    String title = link.getText().trim();
                    if (title.isEmpty()) continue;

                    String location = "";
                    try {
                        WebElement card = link.findElement(By.xpath("./ancestor::*[self::li or self::tr or self::article or contains(@class,'card') or contains(@class,'job')][1]"));
                        String[] lines = card.getText().split("\\r?\\n");
                        if (lines.length >= 2) location = lines[1].trim();
                    } catch (Exception ignored) { }

                    JobPosting job = new JobPosting();
                    job.setCompany(COMPANY);
                    job.setTitle(title);
                    job.setLocation(location);
                    job.setDate(LocalDate.now().toString());
                    jobs.add(job);
                } catch (Exception e) {
                    log.warn("[Roblox] Failed to parse a link, skipping: {}", e.getMessage());
                }
            }

            log.info("[Roblox] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Roblox] Error scraping Roblox jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
