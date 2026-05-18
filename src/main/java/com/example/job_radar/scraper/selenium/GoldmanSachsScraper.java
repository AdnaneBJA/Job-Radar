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
public class GoldmanSachsScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(GoldmanSachsScraper.class);

    private static final String BASE_URL =
            "https://www.goldmansachs.com/careers/students/programs-and-internships"
            + "?0=gscom%3Aprogram-type%2Finternship"
            + "&1=gscom%3Aprogram-region-careers%2Famericas";

    private static final String COMPANY = "Goldman Sachs";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("a[href*='/careers/students/programs-and-internships/']")));
            Thread.sleep(2000);

            List<WebElement> links = driver.findElements(
                    By.cssSelector("a[href*='/careers/students/programs-and-internships/']"));

            Set<String> seenHrefs = new HashSet<>();
            for (WebElement link : links) {
                try {
                    String href = link.getAttribute("href");
                    if (href == null) continue;
                    // Skip filter / category anchors — keep only real program detail URLs.
                    if (href.contains("#") || href.contains("?")) continue;
                    if (href.endsWith("/programs-and-internships/")) continue;
                    if (!seenHrefs.add(href)) continue;

                    String title = link.getText().trim();
                    if (title.isEmpty() || title.equalsIgnoreCase("View") || title.equalsIgnoreCase("See eligibility")) continue;

                    JobPosting job = new JobPosting();
                    job.setCompany(COMPANY);
                    job.setTitle(title);
                    job.setLocation("Americas");
                    job.setDate(LocalDate.now().toString());
                    jobs.add(job);
                } catch (Exception e) {
                    log.warn("[Goldman Sachs] Failed to parse a link, skipping: {}", e.getMessage());
                }
            }

            log.info("[Goldman Sachs] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Goldman Sachs] Error scraping Goldman Sachs jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
