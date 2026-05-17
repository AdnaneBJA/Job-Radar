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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class TwilioScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(TwilioScraper.class);

    private static final String BASE_URL =
            "https://jobs.twilio.com/careers?start=0&pid=1099553142342&sort_by=timestamp&filter_seniority=0";

    private static final String COMPANY = "Twilio";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(25));
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("a[href*='/careers/job/']")));
            Thread.sleep(2000);

            List<WebElement> links = driver.findElements(
                    By.cssSelector("a[href*='/careers/job/']"));

            Set<String> seenHrefs = new HashSet<>();
            for (WebElement link : links) {
                try {
                    String href = link.getAttribute("href");
                    if (href == null || !seenHrefs.add(href)) continue;

                    String text = link.getText().trim();
                    if (text.isEmpty()) continue;

                    // Each link's textContent is the full card: title, department,
                    // location, "Remote", "Posted X ago" — all newline-separated.
                    String[] lines = text.split("\\r?\\n");
                    String title = lines[0].trim();
                    String department = lines.length > 1 ? lines[1].trim() : "";

                    // The page filters to a single PID but also returns "you might like"
                    // suggestions in the same DOM. Keep only the intern set (department
                    // == "Students") which is what the filtered URL is meant to surface.
                    if (!"Students".equalsIgnoreCase(department)) continue;

                    String location = "";
                    for (int i = 1; i < lines.length; i++) {
                        String l = lines[i].trim();
                        if (l.isEmpty()) continue;
                        if (l.toLowerCase().startsWith("posted ")) break;
                        if (l.equalsIgnoreCase("Remote") || l.equalsIgnoreCase("Students")) continue;
                        location = l;
                        break;
                    }

                    JobPosting job = new JobPosting();
                    job.setCompany(COMPANY);
                    job.setTitle(title);
                    job.setLocation(location);
                    job.setDate(LocalDate.now().toString());
                    jobs.add(job);
                } catch (Exception e) {
                    log.warn("[Twilio] Failed to parse a link, skipping: {}", e.getMessage());
                }
            }

            log.info("[Twilio] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Twilio] Error scraping Twilio jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
