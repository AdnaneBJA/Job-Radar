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
public class MercuryScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(MercuryScraper.class);

    private static final String BASE_URL = "https://mercury.com/jobs#job-listings";
    private static final String COMPANY = "Mercury";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(25));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table caption")));
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
            Thread.sleep(2500);

            // Mercury renders each department as its own <table> with a <caption>.
            WebElement engineeringTable = (WebElement) ((JavascriptExecutor) driver).executeScript(
                    "var caps=document.querySelectorAll('table caption');" +
                    "for (var i=0;i<caps.length;i++){if(caps[i].textContent.trim()==='Engineering') return caps[i].closest('table');}" +
                    "return null;");
            if (engineeringTable == null) {
                log.warn("[Mercury] Engineering table not found");
                return jobs;
            }

            List<WebElement> rows = engineeringTable.findElements(By.cssSelector("tbody tr"));
            for (WebElement row : rows) {
                try {
                    List<WebElement> tds = row.findElements(By.cssSelector("td"));
                    if (tds.size() < 2) continue;

                    String title = tds.get(0).getAttribute("textContent").trim();
                    String location = tds.get(1).getAttribute("textContent").trim();
                    if (title.isEmpty()) continue;

                    String t = title.toLowerCase();
                    if (!t.contains("software") && !t.contains("intern") && !t.contains("engineer")) {
                        continue;
                    }

                    JobPosting job = new JobPosting();
                    job.setCompany(COMPANY);
                    job.setTitle(title);
                    job.setLocation(location);
                    job.setDate(LocalDate.now().toString());
                    jobs.add(job);
                } catch (Exception e) {
                    log.warn("[Mercury] Failed to parse a row, skipping: {}", e.getMessage());
                }
            }

            log.info("[Mercury] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Mercury] Error scraping Mercury jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
