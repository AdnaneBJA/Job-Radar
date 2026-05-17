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
public class AirbnbScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(AirbnbScraper.class);

    private static final String BASE_URL =
            "https://careers.airbnb.com/positions/?_departments=early-career-program-intern";

    private static final String COMPANY = "Airbnb";

    private static final Pattern POSITION_HREF = Pattern.compile(".*/positions/\\d+/?$");

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("a[href*='/positions/']")));
            Thread.sleep(3000);

            List<WebElement> links = driver.findElements(By.cssSelector("a[href*='/positions/']"));

            for (WebElement link : links) {
                try {
                    String href = link.getAttribute("href");
                    if (href == null || !POSITION_HREF.matcher(href).matches()) continue;

                    String text = link.getText().trim();
                    if (text.isEmpty()) continue;

                    String title = text;
                    String location = "";

                    // Airbnb's listing puts title and location in the same anchor text,
                    // separated by commas (e.g. "Business Development Intern, Experiences, Paris").
                    // Treat the final comma-segment as the location.
                    int lastComma = text.lastIndexOf(',');
                    if (lastComma > 0 && lastComma < text.length() - 1) {
                        title = text.substring(0, lastComma).trim();
                        location = text.substring(lastComma + 1).trim();
                    }

                    JobPosting job = new JobPosting();
                    job.setCompany(COMPANY);
                    job.setTitle(title);
                    job.setLocation(location);
                    job.setDate(LocalDate.now().toString());
                    jobs.add(job);
                } catch (Exception e) {
                    log.warn("[Airbnb] Failed to parse a link, skipping: {}", e.getMessage());
                }
            }

            log.info("[Airbnb] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Airbnb] Error scraping Airbnb jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
