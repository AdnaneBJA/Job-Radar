package com.example.job_radar.scraper;

import com.example.job_radar.model.JobPosting;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
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
public class WealthsimpleScraper implements JobScraper {

    private static final Logger log = LoggerFactory.getLogger(WealthsimpleScraper.class);

    private static final String BASE_URL = "https://jobs.lever.co/wealthsimple?location=Canada";
    private static final String COMPANY = "Wealthsimple";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".posting")));
            Thread.sleep(2000);

            // Lever loads all jobs on a single page (no pagination)
            List<WebElement> postings = driver.findElements(By.cssSelector(".posting"));

            for (WebElement posting : postings) {
                try {
                    WebElement titleEl = posting.findElement(By.cssSelector("h5[data-qa='posting-name']"));
                    String title = titleEl.getText().trim();

                    JobPosting job = new JobPosting();
                    job.setCompany(COMPANY);
                    job.setTitle(title);

                    // Location
                    List<WebElement> locationEls = posting.findElements(
                            By.cssSelector("span.sort-by-location"));
                    if (!locationEls.isEmpty()) {
                        job.setLocation(locationEls.get(0).getText().trim());
                    } else {
                        job.setLocation("");
                    }

                    // Lever doesn't show posted date on listings
                    job.setDate(LocalDate.now().toString());

                    if (!job.getTitle().isEmpty() && job.getTitle().toUpperCase().contains("INTERN")) {
                        jobs.add(job);
                    }
                } catch (Exception e) {
                    log.warn("[Wealthsimple] Failed to parse a posting, skipping: {}", e.getMessage());
                }
            }

            log.info("[Wealthsimple] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Wealthsimple] Error scraping Lever jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }

    private WebDriver createDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");
        return new ChromeDriver(options);
    }
}
