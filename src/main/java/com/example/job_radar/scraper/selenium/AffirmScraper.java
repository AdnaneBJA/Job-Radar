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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class AffirmScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(AffirmScraper.class);

    private static final String BASE_URL = "https://www.affirm.com/careers#jobOpenings";
    private static final String COMPANY = "Affirm";

    private static final Pattern EXCLUDE = Pattern.compile(
            "\\b(Director|Manager|Senior|Staff)\\b", Pattern.CASE_INSENSITIVE);

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(25));
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("select#jobsFilterDepartment")));
            Thread.sleep(1500);

            WebElement deptSelect = driver.findElement(By.cssSelector("select#jobsFilterDepartment"));
            new Select(deptSelect).selectByValue("Engineering");
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].dispatchEvent(new Event('change', {bubbles:true}));", deptSelect);
            Thread.sleep(3000);
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
            Thread.sleep(2000);

            // Filtered jobs render as <a href='job-boards.greenhouse.io/affirm/jobs/...'>
            // with textContent "Title\nLocation".
            List<WebElement> links = driver.findElements(
                    By.cssSelector("a[href*='greenhouse.io/affirm']"));

            Set<String> seenHrefs = new HashSet<>();
            for (WebElement link : links) {
                try {
                    String href = link.getAttribute("href");
                    if (href == null || !seenHrefs.add(href)) continue;

                    String text = link.getText().trim();
                    if (text.isEmpty()) continue;
                    String[] lines = text.split("\\r?\\n");
                    String title = lines[0].trim();
                    String location = lines.length > 1 ? lines[1].trim() : "";

                    if (title.isEmpty()) continue;
                    if (EXCLUDE.matcher(title).find()) continue;

                    JobPosting job = new JobPosting();
                    job.setCompany(COMPANY);
                    job.setTitle(title);
                    job.setLocation(location);
                    job.setDate(LocalDate.now().toString());
                    jobs.add(job);
                } catch (Exception e) {
                    log.warn("[Affirm] Failed to parse a link, skipping: {}", e.getMessage());
                }
            }

            log.info("[Affirm] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Affirm] Error scraping Affirm jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
