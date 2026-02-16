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
public class CiscoScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(CiscoScraper.class);

    private static final String BASE_URL =
            "https://careers.cisco.com/global/en/search-results";

    private static final String COMPANY = "Cisco";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            Thread.sleep(5000);

            // 1. Expand Country or Region accordion and check "Canada"
            expandAccordion(driver, "CountryorRegionAccordion");
            Thread.sleep(1000);
            clickCheckbox(driver, wait, "country_phs_Canada");
            Thread.sleep(3000);

            // 2. Expand Experience Level accordion and check "Co-Op" and "Intern"
            expandAccordion(driver, "ExperienceLevelAccordion");
            Thread.sleep(1000);
            clickCheckbox(driver, wait, "raasJobRequisitionType_phs_Co-Op");
            Thread.sleep(3000);
            clickCheckbox(driver, wait, "raasJobRequisitionType_phs_Intern");
            Thread.sleep(3000);

            // Scrape all pages
            while (true) {
                List<JobPosting> pageJobs = scrapeCurrentPage(driver);
                jobs.addAll(pageJobs);
                log.info("[Cisco] Scraped {} jobs from current page (total so far: {})",
                        pageJobs.size(), jobs.size());

                if (!goToNextPage(driver, wait)) {
                    break;
                }
            }

            log.info("[Cisco] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Cisco] Error scraping Cisco jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }

    private void expandAccordion(WebDriver driver, String accordionId) {
        try {
            WebElement accordion = driver.findElement(By.id(accordionId));
            // Check if the accordion contains a "plus" icon (collapsed)
            List<WebElement> plusIcons = accordion.findElements(By.cssSelector("use[href='#phw-cns-icon-plus']"));
            if (!plusIcons.isEmpty()) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", accordion);
                log.info("[Cisco] Expanded accordion '{}'", accordionId);
            }
        } catch (Exception e) {
            log.warn("[Cisco] Could not expand accordion '{}': {}", accordionId, e.getMessage());
        }
    }

    private void clickCheckbox(WebDriver driver, WebDriverWait wait, String idPrefix) {
        try {
            WebElement checkbox = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("input[id^='" + idPrefix + "']")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", checkbox);
            log.info("[Cisco] Checked '{}'", idPrefix);
        } catch (Exception e) {
            log.warn("[Cisco] Could not click checkbox '{}': {}", idPrefix, e.getMessage());
        }
    }

    private List<JobPosting> scrapeCurrentPage(WebDriver driver) {
        List<JobPosting> jobs = new ArrayList<>();

        List<WebElement> jobLinks = driver.findElements(
                By.cssSelector("a[data-ph-at-id='job-link']"));

        for (WebElement link : jobLinks) {
            try {
                String title = link.getAttribute("data-ph-at-job-title-text");
                if (title == null || title.isEmpty()) {
                    title = link.getText().trim();
                }

                if (title.isEmpty()) {
                    continue;
                }

                // Location: navigate up to the card container and find the location element
                String location = "";
                try {
                    WebElement card = link.findElement(By.xpath("./ancestor::div[contains(@class,'phw-posn-relative')]"));
                    // Try single location first (div.job-location)
                    List<WebElement> singleLoc = card.findElements(By.cssSelector("div.job-location"));
                    if (!singleLoc.isEmpty()) {
                        String text = singleLoc.get(0).getText().replace("Location :", "").trim();
                        location = text.split("\\|")[0].trim();
                        if (location.startsWith("|")) location = location.substring(1).trim();
                    }
                    // Fallback to multi-location button
                    if (location.isEmpty()) {
                        List<WebElement> multiLoc = card.findElements(
                                By.cssSelector("button[data-ph-at-id='job-multi_location']"));
                        if (!multiLoc.isEmpty()) {
                            location = multiLoc.get(0).getText().split("\n")[0].trim();
                        }
                    }
                } catch (Exception ignored) {
                }

                JobPosting job = new JobPosting();
                job.setCompany(COMPANY);
                job.setTitle(title);
                job.setLocation(location);
                job.setDate(LocalDate.now().toString());
                jobs.add(job);
            } catch (Exception e) {
                log.warn("[Cisco] Failed to parse a job card, skipping: {}", e.getMessage());
            }
        }

        return jobs;
    }

    private boolean goToNextPage(WebDriver driver, WebDriverWait wait) {
        try {
            WebElement nextLabel = driver.findElement(
                    By.cssSelector("span[data-ph-at-id='pagination-next-text']"));
            WebElement nextLink = nextLabel.findElement(By.xpath("./ancestor::a"));

            String disabled = nextLink.getAttribute("aria-disabled");
            if ("true".equals(disabled)) {
                return false;
            }

            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", nextLink);
            Thread.sleep(3000);

            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("a[data-ph-at-id='job-link']")));

            return true;
        } catch (Exception e) {
            log.info("[Cisco] No more pages to navigate");
            return false;
        }
    }
}
