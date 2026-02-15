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
public class SapScraper extends SeleniumScraper{
    private static final Logger log = LoggerFactory.getLogger(SapScraper.class);

    private static final String BASE_URL =
            "https://jobs.sap.com/search/?createNewAlert=false"
                    + "&q=&locationsearch="
                    + "&optionsFacetsDD_department="
                    + "&optionsFacetsDD_customfield3=Student"
                    + "&optionsFacetsDD_country=CA";

    private static final String COMPANY = "SAP";



    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".data-row")));
            Thread.sleep(2000);


            // Lever loads all jobs on a single page (no pagination)
            List<WebElement> postings = driver.findElements(By.cssSelector(".data-row"));

            for (WebElement posting: postings) {
                try {
                    WebElement titleEl = posting.findElement(By.cssSelector("a.jobTitle-link"));
                    String title = titleEl.getText().trim();

                    WebElement locationEl = posting.findElement(
                            By.cssSelector("td.colLocation span.jobLocation")
                    );

                    String location = locationEl.getText().trim();


                    JobPosting job = new JobPosting();
                    job.setCompany(COMPANY);
                    job.setTitle(title);
                    job.setDate(LocalDate.now().toString());
                    job.setLocation(location);

                    if (!job.getTitle().isEmpty()) {
                        jobs.add(job);
                    }

                } catch (Exception e) {
                    log.error("[SAP] Failed to parse a posting (inside nested try block), skipping: {}" , e.getMessage());
                }
            }
            log.info("[SAP] Scraping complete. Total jobs: {}", jobs.size());

        } catch (Exception e) {
            log.warn("[SAP] Failed to parse a posting, skipping: {}", e.getMessage());
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
