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
public class RobinHoodScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(RobinHoodScraper.class);

    private static final String BASE_URL = "https://careers.robinhood.com/";
    private static final String COMPANY = "RobinHood";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            Thread.sleep(5000);

            JavascriptExecutor js = (JavascriptExecutor) driver;

            // 1. Select "EARLY TALENT AND INTERNSHIPS" category
            WebElement earlyTalent = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("li[data-filter='EARLYTALENTANDINTERNSHIPS']")));
            js.executeScript("arguments[0].click();", earlyTalent);
            log.info("[RobinHood] Selected 'EARLY TALENT AND INTERNSHIPS' category");
            Thread.sleep(2000);

            // 2. Select "Toronto, Canada" location
            WebElement toronto = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("li[data-location='Toronto, Canada']")));
            js.executeScript("arguments[0].click();", toronto);
            log.info("[RobinHood] Selected 'Toronto, Canada' location");
            Thread.sleep(2000);

            // 3. Scrape visible job entries (those without the "hide" class)
            List<WebElement> allJobPs = driver.findElements(By.cssSelector("p.job"));
            for (WebElement jobP : allJobPs) {
                if (!jobP.isDisplayed()) {
                    continue;
                }

                try {
                    WebElement titleLink = jobP.findElement(By.cssSelector("a"));
                    String title = titleLink.getText().trim();

                    String location = "";
                    List<WebElement> spans = jobP.findElements(By.cssSelector("span"));
                    if (!spans.isEmpty()) {
                        location = spans.get(0).getText().trim();
                    }

                    if (title.isEmpty()) {
                        continue;
                    }

                    JobPosting job = new JobPosting();
                    job.setCompany(COMPANY);
                    job.setTitle(title);
                    job.setLocation(location);
                    job.setDate(LocalDate.now().toString());
                    jobs.add(job);
                } catch (Exception e) {
                    log.warn("[RobinHood] Failed to parse a job card, skipping: {}", e.getMessage());
                }
            }

            log.info("[RobinHood] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[RobinHood] Error scraping RobinHood jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
