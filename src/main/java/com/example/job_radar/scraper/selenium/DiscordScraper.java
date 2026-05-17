package com.example.job_radar.scraper.selenium;

import com.example.job_radar.model.JobPosting;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class DiscordScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(DiscordScraper.class);

    private static final String BASE_URL = "https://discord.com/careers#all-jobs";
    private static final String COMPANY = "Discord";
    private static final String TARGET_CATEGORY = "Data Science & Engineering";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            Thread.sleep(8000);
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
            Thread.sleep(2000);

            // Discord renders the full job list in the DOM grouped by department,
            // and the filter dropdown only narrows the visible subset. Read the
            // visible body text and pull out rows whose category matches our target.
            String body = driver.findElement(By.tagName("body")).getText();
            String[] lines = body.split("\\r?\\n");

            for (int i = 0; i + 2 < lines.length; i++) {
                String maybeCategory = lines[i].trim();
                if (!maybeCategory.equals(TARGET_CATEGORY)) continue;

                // Following line is the title, then a blank line, then the location.
                String title = lines[i + 1].trim();
                String location = "";
                for (int j = i + 2; j < Math.min(lines.length, i + 5); j++) {
                    String l = lines[j].trim();
                    if (l.isEmpty()) continue;
                    if (l.equals(TARGET_CATEGORY) || l.matches("[A-Z][A-Za-z0-9 &/.,'\\-]+")) {
                        // Skip a category boundary; first non-empty line is location
                    }
                    location = l;
                    break;
                }

                if (title.isEmpty() || title.equals(TARGET_CATEGORY)) continue;

                JobPosting job = new JobPosting();
                job.setCompany(COMPANY);
                job.setTitle(title);
                job.setLocation(location);
                job.setDate(LocalDate.now().toString());
                jobs.add(job);
            }

            log.info("[Discord] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Discord] Error scraping Discord jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
