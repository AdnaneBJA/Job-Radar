package com.example.job_radar.scraper.selenium;

import com.example.job_radar.model.JobPosting;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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
public class OracleScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(OracleScraper.class);

    private static final String BASE_URL =
            "https://careers.oracle.com/en/sites/jobsearch/jobs?lastSelectedFacet=AttributeChar6"
            + "&selectedFlexFieldsFacets=%22AttributeChar6%7C0+to+2%2B+years%22"
            + "&selectedLocationsFacet=300000000106749";

    private static final String COMPANY = "Oracle";

    @Override
    public WebDriver createDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        return new ChromeDriver(options);
    }

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            Thread.sleep(8000);

            // Wait for job tiles to load
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("span.job-tile__title")));

            // Scrape all pages
            while (true) {
                List<JobPosting> pageJobs = scrapeCurrentPage(driver);
                jobs.addAll(pageJobs);
                log.info("[Oracle] Scraped {} jobs from current page (total so far: {})",
                        pageJobs.size(), jobs.size());

                if (!goToNextPage(driver, wait)) {
                    break;
                }
            }

            log.info("[Oracle] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Oracle] Error scraping Oracle jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }

    private List<JobPosting> scrapeCurrentPage(WebDriver driver) {
        List<JobPosting> jobs = new ArrayList<>();

        List<WebElement> titleSpans = driver.findElements(By.cssSelector("span.job-tile__title"));
        List<WebElement> locationSpans = driver.findElements(
                By.cssSelector("span[data-bind='html: primaryLocation']"));

        for (int i = 0; i < titleSpans.size(); i++) {
            try {
                String title = titleSpans.get(i).getText().trim();
                if (title.isEmpty()) continue;

                String location = "";
                if (i < locationSpans.size()) {
                    location = locationSpans.get(i).getText().trim();
                }

                JobPosting job = new JobPosting();
                job.setCompany(COMPANY);
                job.setTitle(title);
                job.setLocation(location);
                job.setDate(LocalDate.now().toString());
                jobs.add(job);
            } catch (Exception e) {
                log.warn("[Oracle] Failed to parse a job card, skipping: {}", e.getMessage());
            }
        }

        return jobs;
    }

    private boolean goToNextPage(WebDriver driver, WebDriverWait wait) {
        try {
            WebElement nextBtn = driver.findElement(
                    By.cssSelector("button[aria-label='Next'], a[aria-label='Next'], [class*='next-page']"));

            String disabled = nextBtn.getAttribute("disabled");
            String ariaDisabled = nextBtn.getAttribute("aria-disabled");
            if ("true".equals(disabled) || "true".equals(ariaDisabled)) {
                return false;
            }

            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", nextBtn);
            Thread.sleep(5000);

            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("span.job-tile__title")));

            return true;
        } catch (Exception e) {
            log.info("[Oracle] No more pages to navigate");
            return false;
        }
    }
}
