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
public class JaneStreetScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(JaneStreetScraper.class);

    private static final String BASE_URL =
            "https://www.janestreet.com/join-jane-street/open-roles/?type=co-op&location=all-locations";

    private static final String COMPANY = "Jane Street";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(25));
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".students-and-new-grads.job")));
            Thread.sleep(2000);

            List<WebElement> rows = driver.findElements(
                    By.cssSelector(".students-and-new-grads.job.open, .students-and-new-grads.job.closed"));

            for (WebElement row : rows) {
                try {
                    if (!row.isDisplayed()) continue;

                    String text = row.getText().trim();
                    if (text.isEmpty()) continue;

                    // The visible row is rendered as columns: Title | Type | Location | Department | Season
                    // Selenium .getText() returns these separated by newlines.
                    String[] parts = text.split("\\r?\\n");

                    String title = parts.length > 0 ? parts[0].trim() : "";
                    String location = "";

                    // Find the column matching a known JS location code (NYC/HKG/LDN/SGP/CHI/MUM names)
                    for (int i = 1; i < parts.length; i++) {
                        String p = parts[i].trim();
                        if (p.equalsIgnoreCase("New York") || p.equalsIgnoreCase("London")
                                || p.equalsIgnoreCase("Hong Kong") || p.equalsIgnoreCase("Singapore")
                                || p.equalsIgnoreCase("Chicago") || p.equalsIgnoreCase("Mumbai")) {
                            location = p;
                            break;
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
                    log.warn("[Jane Street] Failed to parse a row, skipping: {}", e.getMessage());
                }
            }

            log.info("[Jane Street] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Jane Street] Error scraping Jane Street jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
