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
public class AmdScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(AmdScraper.class);

    private static final String BASE_URL =
            "https://careers.amd.com/careers-home/jobs";

    private static final String COMPANY = "AMD";

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
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            Thread.sleep(8000);

            // 1. Click Region/Location dropdown (index 1) and select "Canada"
            selectMatOption(driver, wait, 1, "Canada");
            Thread.sleep(3000);

            // 2. Click Categories dropdown (index 3) and select "Student / Intern / Temp"
            selectMatOption(driver, wait, 3, "Student / Intern / Temp");
            Thread.sleep(3000);

            // Wait for job cards to load after filtering
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("a.job-title-link")));
            Thread.sleep(2000);

            // Scrape all pages
            while (true) {
                List<JobPosting> pageJobs = scrapeCurrentPage(driver);
                jobs.addAll(pageJobs);
                log.info("[AMD] Scraped {} jobs from current page (total so far: {})",
                        pageJobs.size(), jobs.size());

                if (!goToNextPage(driver, wait)) {
                    break;
                }
            }

            log.info("[AMD] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[AMD] Error scraping AMD jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }

    private void selectMatOption(WebDriver driver, WebDriverWait wait,
                                  int selectIndex, String optionTextPrefix) {
        try {
            List<WebElement> matSelects = driver.findElements(By.cssSelector("mat-select"));
            if (selectIndex >= matSelects.size()) {
                log.warn("[AMD] mat-select index {} out of range (found {})", selectIndex, matSelects.size());
                return;
            }

            // Click to open the dropdown
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", matSelects.get(selectIndex));
            Thread.sleep(1000);

            // Wait for options to appear and find the matching one
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("mat-option")));
            List<WebElement> options = driver.findElements(By.cssSelector("mat-option"));

            for (WebElement option : options) {
                String text = option.getText().trim();
                if (text.startsWith(optionTextPrefix)) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", option);
                    log.info("[AMD] Selected '{}' from dropdown {}", text, selectIndex);

                    // Close the dropdown by clicking the backdrop
                    Thread.sleep(500);
                    List<WebElement> backdrop = driver.findElements(By.cssSelector(".cdk-overlay-backdrop"));
                    if (!backdrop.isEmpty()) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", backdrop.get(0));
                    }
                    return;
                }
            }

            log.warn("[AMD] Could not find option starting with '{}' in dropdown {}", optionTextPrefix, selectIndex);
            // Close the dropdown
            List<WebElement> backdrop = driver.findElements(By.cssSelector(".cdk-overlay-backdrop"));
            if (!backdrop.isEmpty()) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", backdrop.get(0));
            }
        } catch (Exception e) {
            log.warn("[AMD] Could not select '{}' from dropdown {}: {}", optionTextPrefix, selectIndex, e.getMessage());
        }
    }

    private List<JobPosting> scrapeCurrentPage(WebDriver driver) {
        List<JobPosting> jobs = new ArrayList<>();

        List<WebElement> titleLinks = driver.findElements(By.cssSelector("a.job-title-link"));
        List<WebElement> locationSpans = driver.findElements(By.cssSelector("span.label-value.location"));

        for (int i = 0; i < titleLinks.size(); i++) {
            try {
                // Title from span[itemprop='title'] inside the link
                String title = "";
                List<WebElement> titleSpans = titleLinks.get(i).findElements(By.cssSelector("span[itemprop='title']"));
                if (!titleSpans.isEmpty()) {
                    title = titleSpans.get(0).getText().trim();
                } else {
                    title = titleLinks.get(i).getText().trim();
                }

                if (title.isEmpty()) continue;

                // Location from the corresponding span.label-value.location
                String location = "";
                if (i < locationSpans.size()) {
                    location = locationSpans.get(i).getText().trim()
                            .replaceAll("\\s+", " ");
                }

                JobPosting job = new JobPosting();
                job.setCompany(COMPANY);
                job.setTitle(title);
                job.setLocation(location);
                job.setDate(LocalDate.now().toString());
                jobs.add(job);
            } catch (Exception e) {
                log.warn("[AMD] Failed to parse a job card, skipping: {}", e.getMessage());
            }
        }

        return jobs;
    }

    private boolean goToNextPage(WebDriver driver, WebDriverWait wait) {
        try {
            WebElement nextBtn = driver.findElement(
                    By.cssSelector("button.mat-paginator-navigation-next, button[aria-label='Next page']"));

            String disabled = nextBtn.getAttribute("disabled");
            if ("true".equals(disabled)) {
                return false;
            }

            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", nextBtn);
            Thread.sleep(3000);

            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("a.job-title-link")));

            return true;
        } catch (Exception e) {
            log.info("[AMD] No more pages to navigate");
            return false;
        }
    }
}
