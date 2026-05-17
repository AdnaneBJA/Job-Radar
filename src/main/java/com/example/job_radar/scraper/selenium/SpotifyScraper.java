package com.example.job_radar.scraper.selenium;

import com.example.job_radar.model.JobPosting;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SpotifyScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(SpotifyScraper.class);

    private static final String BASE_URL = "https://www.lifeatspotify.com/jobs";
    private static final String COMPANY = "Spotify";

    private static final Pattern INTERNSHIP_COUNT = Pattern.compile("Internship[^0-9]{0,30}(\\d+)");

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            Thread.sleep(8000);

            // Expand the "Job type" filter section so internship count becomes visible
            List<WebElement> filterToggles = driver.findElements(By.xpath(
                    "//*[translate(normalize-space(text()), 'JOBTYPE', 'jobtype')='job type']"));
            for (WebElement el : filterToggles) {
                try {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
                    Thread.sleep(1500);
                    break;
                } catch (Exception ignored) { }
            }

            int internshipCount = -1;

            // Try to find a labeled element pairing "Internship" with a count
            List<WebElement> internEls = driver.findElements(
                    By.xpath("//*[contains(text(), 'Internship')]"));
            for (WebElement el : internEls) {
                String text = el.getText();
                if (text == null) continue;
                Matcher m = INTERNSHIP_COUNT.matcher(text);
                if (m.find()) {
                    internshipCount = Integer.parseInt(m.group(1));
                    break;
                }
            }

            // Fallback: scan whole body text once
            if (internshipCount < 0) {
                String body = driver.findElement(By.tagName("body")).getText();
                Matcher m = INTERNSHIP_COUNT.matcher(body);
                if (m.find()) {
                    internshipCount = Integer.parseInt(m.group(1));
                }
            }

            if (internshipCount < 0) {
                log.warn("[Spotify] Could not locate internship count on page");
                return jobs;
            }

            log.info("[Spotify] Internship count: {}", internshipCount);

            if (internshipCount > 0) {
                JobPosting job = new JobPosting();
                job.setCompany(COMPANY);
                job.setTitle("Spotify Internship count is now " + internshipCount + " — check postings");
                job.setLocation("");
                job.setDate(LocalDate.now().toString());
                jobs.add(job);
            }
        } catch (Exception e) {
            log.error("[Spotify] Error checking Spotify jobs page", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
