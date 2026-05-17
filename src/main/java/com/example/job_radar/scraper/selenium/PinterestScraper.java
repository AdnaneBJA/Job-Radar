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
public class PinterestScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(PinterestScraper.class);

    private static final String BASE_URL =
            "https://www.pinterestcareers.com/jobs/?search=&type=Intern&pagesize=20#results";

    private static final String COMPANY = "Pinterest";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".card.card-job")));
            Thread.sleep(2000);

            List<WebElement> cards = driver.findElements(By.cssSelector(".card.card-job"));

            for (WebElement card : cards) {
                try {
                    String title = card.findElement(By.cssSelector(".card-title")).getText().trim();
                    if (title.isEmpty()) continue;

                    String location = "";
                    // Pinterest renders location and team inline so getText() merges them.
                    // Prefer a dedicated location element when present; otherwise fall back to
                    // splitting the card body and stripping the trailing team name.
                    List<WebElement> locEls = card.findElements(
                            By.cssSelector("[class*='location'], [class*='Location']"));
                    if (!locEls.isEmpty()) {
                        location = locEls.get(0).getText().trim();
                    } else {
                        String[] lines = card.getText().split("\\r?\\n");
                        for (int i = 1; i < lines.length; i++) {
                            String l = lines[i].trim();
                            if (l.isEmpty() || l.equalsIgnoreCase("Save")) continue;
                            location = l;
                            break;
                        }
                    }
                    // Strip the trailing team/department name that gets concatenated on
                    // Pinterest's inline-rendered card body (e.g. "... University").
                    location = location.replaceAll(
                            "\\s+(University|Engineering|Design|Finance|HR|IT|Legal|Marketing|Sales|Operations|Product|Research|Security|Data)$",
                            "").trim();

                    JobPosting job = new JobPosting();
                    job.setCompany(COMPANY);
                    job.setTitle(title);
                    job.setLocation(location);
                    job.setDate(LocalDate.now().toString());
                    jobs.add(job);
                } catch (Exception e) {
                    log.warn("[Pinterest] Failed to parse a card, skipping: {}", e.getMessage());
                }
            }

            log.info("[Pinterest] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Pinterest] Error scraping Pinterest jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
