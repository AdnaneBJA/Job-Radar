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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class ImcScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(ImcScraper.class);

    private static final String BASE_URL =
            "https://www.imc.com/us/search-careers"
            + "?jobOffices=Chicago%2CNew+York&jobTypes=Intern&page=1";

    private static final String COMPANY = "IMC Trading";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(25));
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("a[href*='/careers/jobs/']")));
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
            Thread.sleep(2000);

            List<WebElement> links = driver.findElements(
                    By.cssSelector("a[href*='/careers/jobs/']"));

            Set<String> seenHrefs = new HashSet<>();
            for (WebElement link : links) {
                try {
                    String href = link.getAttribute("href");
                    if (href == null || !seenHrefs.add(href)) continue;
                    if (href.contains("/apply")) continue;

                    String text = link.getText().trim();
                    if (text.isEmpty()) continue;

                    // Link text is laid out as: Title | Intern | <Department> | <Location>
                    String[] lines = text.split("\\r?\\n");
                    String title = lines[0].trim();
                    String location = lines.length > 0 ? lines[lines.length - 1].trim() : "";

                    JobPosting job = new JobPosting();
                    job.setCompany(COMPANY);
                    job.setTitle(title);
                    job.setLocation(location);
                    job.setDate(LocalDate.now().toString());
                    jobs.add(job);
                } catch (Exception e) {
                    log.warn("[IMC] Failed to parse a link, skipping: {}", e.getMessage());
                }
            }

            log.info("[IMC] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[IMC] Error scraping IMC jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
