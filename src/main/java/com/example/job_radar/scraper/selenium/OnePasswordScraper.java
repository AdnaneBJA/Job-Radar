package com.example.job_radar.scraper.selenium;

import com.example.job_radar.model.JobPosting;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class OnePasswordScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(OnePasswordScraper.class);

    private static final String BASE_URL = "https://jobs.ashbyhq.com/1password";
    private static final String COMPANY = "1Password";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("select[name='departmentId']")));
            Thread.sleep(1500);

            // Pick the Technology option from the Department dropdown. The visible label
            // includes the count ("Technology (33)") so we match by prefix instead.
            WebElement deptSelect = driver.findElement(By.cssSelector("select[name='departmentId']"));
            Select sel = new Select(deptSelect);
            String matchedValue = null;
            for (WebElement opt : sel.getOptions()) {
                String text = opt.getText().trim();
                if (text.startsWith("Technology")) {
                    matchedValue = opt.getAttribute("value");
                    break;
                }
            }
            if (matchedValue != null) {
                sel.selectByValue(matchedValue);
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].dispatchEvent(new Event('change', {bubbles:true}));", deptSelect);
            }
            Thread.sleep(3000);

            List<WebElement> postings = driver.findElements(By.cssSelector(".ashby-job-posting-brief"));

            for (WebElement posting : postings) {
                try {
                    String title = posting.findElement(
                            By.cssSelector(".ashby-job-posting-brief-title")).getText().trim();
                    String location = "";
                    List<WebElement> detailEls = posting.findElements(
                            By.cssSelector(".ashby-job-posting-brief-details p"));
                    if (!detailEls.isEmpty()) {
                        String[] parts = detailEls.get(0).getText().split("•");
                        if (parts.length > 1) location = parts[1].trim();
                        else location = detailEls.get(0).getText().trim();
                    }

                    if (title.isEmpty()) continue;

                    JobPosting job = new JobPosting();
                    job.setCompany(COMPANY);
                    job.setTitle(title);
                    job.setLocation(location);
                    job.setDate(LocalDate.now().toString());
                    jobs.add(job);
                } catch (Exception e) {
                    log.warn("[1Password] Failed to parse a posting, skipping: {}", e.getMessage());
                }
            }

            log.info("[1Password] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[1Password] Error scraping 1Password jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
