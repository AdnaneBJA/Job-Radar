package com.example.job_radar.scraper.selenium;

import com.example.job_radar.model.JobPosting;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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
public class CloudflareScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(CloudflareScraper.class);

    private static final String BASE_URL =
            "https://www.cloudflare.com/careers/jobs/?department=Engineering";

    private static final String COMPANY = "Cloudflare";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(25));
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("#jobs-list a[href*='greenhouse.io']")));

            // Scroll repeatedly to load all rows (Cloudflare lazy-renders below the fold)
            JavascriptExecutor js = (JavascriptExecutor) driver;
            long lastHeight = 0;
            for (int i = 0; i < 12; i++) {
                js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
                Thread.sleep(1200);
                long newHeight = ((Number) js.executeScript("return document.body.scrollHeight;")).longValue();
                if (newHeight == lastHeight) break;
                lastHeight = newHeight;
            }
            Thread.sleep(1500);

            List<WebElement> links = driver.findElements(
                    By.cssSelector("#jobs-list a[href*='greenhouse.io']"));

            for (WebElement link : links) {
                try {
                    String title = link.getText().trim();
                    if (title.isEmpty()) continue;

                    String location = "";
                    try {
                        // The job row is two levels up from the title link; the location lives
                        // in the last `.flex-column` of the row.
                        WebElement row = (WebElement) js.executeScript(
                                "var n=arguments[0]; while (n && !(n.classList && n.classList.contains('flex') && n.classList.contains('flex-row'))) n = n.parentElement; return n;",
                                link);
                        if (row != null) {
                            List<WebElement> cols = row.findElements(By.cssSelector(".flex-column"));
                            if (!cols.isEmpty()) {
                                WebElement locCol = cols.get(cols.size() - 1);
                                List<String> locParts = new ArrayList<>();
                                for (WebElement span : locCol.findElements(By.tagName("span"))) {
                                    String t = span.getText().trim();
                                    if (!t.isEmpty()) locParts.add(t);
                                }
                                location = String.join("; ", locParts);
                            }
                        }
                    } catch (Exception ignored) { }

                    JobPosting job = new JobPosting();
                    job.setCompany(COMPANY);
                    job.setTitle(title);
                    job.setLocation(location);
                    job.setDate(LocalDate.now().toString());
                    jobs.add(job);
                } catch (Exception e) {
                    log.warn("[Cloudflare] Failed to parse a row, skipping: {}", e.getMessage());
                }
            }

            log.info("[Cloudflare] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Cloudflare] Error scraping Cloudflare jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
