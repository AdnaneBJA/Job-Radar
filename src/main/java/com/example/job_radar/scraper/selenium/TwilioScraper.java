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
            // Twilio migrated to the Eightfold AI platform. It loads the search shell but
            // renders zero job cards under headless Chrome (results come from a bot-gated XHR;
            // the public /api/apply/v2/jobs endpoint hard-403s server-side too). Wait briefly
            // for any position card and degrade quietly instead of hanging the batch / spewing
            // a 25s timeout stack trace.
            // TODO: revisit via the Eightfold API with proper anti-bot handling, or a non-headless run.
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(12));
            List<WebElement> links;
            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("a.position-card, [data-test-id='position-card'], a[href*='/careers/job/']")));
                Thread.sleep(2000);
                links = driver.findElements(
                        By.cssSelector("a.position-card, [data-test-id='position-card'], a[href*='/careers/job/']"));
            } catch (org.openqa.selenium.TimeoutException te) {
                log.info("[Twilio] No job cards rendered (Eightfold gates headless results); returning 0.");
                return jobs;
            }

            Set<String> seenHrefs = new HashSet<>();
            for (WebElement link : links) {
                try {
                    String href = link.getAttribute("href");
                    if (href == null || !seenHrefs.add(href)) continue;

                    String text = link.getText().trim();
                    if (text.isEmpty()) continue;

                    String[] lines = text.split("\\r?\\n");
                    String title = lines[0].trim();
                    if (title.isEmpty()) continue;

                    String location = lines.length > 1 ? lines[1].trim() : "";

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
