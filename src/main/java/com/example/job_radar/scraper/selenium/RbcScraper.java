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
public class RbcScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(RbcScraper.class);

    private static final String BASE_URL =
            "https://jobs.rbc.com/ca/en/search-results?s=1";

    private static final String COMPANY = "RBC";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            Thread.sleep(5000);

            JavascriptExecutor js = (JavascriptExecutor) driver;

            // 1. Expand Country accordion and check "Canada"
            expandAccordion(driver, js, "CountryAccordion");
            Thread.sleep(1000);
            boolean canadaOk = clickCheckboxByAriaLabel(driver, js, wait, "Canada");
            Thread.sleep(3000);

            // 2. Expand Job Type accordion and check "Student/Coop (Fixed Term)"
            expandAccordion(driver, js, "JobTypeAccordion");
            Thread.sleep(1000);
            boolean coopOk = clickCheckboxByAriaLabel(driver, js, wait, "Student/Coop");
            Thread.sleep(3000);

            if (!canadaOk || !coopOk) {
                log.error("[RBC] Filters did not apply (Canada={}, Student/Coop={}), aborting scrape to avoid flooding DB with unfiltered jobs",
                        canadaOk, coopOk);
                return jobs;
            }

            // Safety cap — Canada + Student/Coop should yield ~4 pages (~40 jobs)
            int maxPages = 15;
            int currentPage = 1;
            while (currentPage <= maxPages) {
                List<JobPosting> pageJobs = scrapeCurrentPage(driver);
                jobs.addAll(pageJobs);
                log.info("[RBC] Scraped {} jobs from current page (total so far: {})",
                        pageJobs.size(), jobs.size());

                currentPage++;
                if (!goToNextPage(driver, js, wait, currentPage)) {
                    break;
                }
            }
            if (currentPage > maxPages) {
                log.warn("[RBC] Hit max page cap ({}); filters may have silently failed", maxPages);
            }

            log.info("[RBC] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[RBC] Error scraping RBC jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }

    private void expandAccordion(WebDriver driver, JavascriptExecutor js, String accordionId) {
        try {
            WebElement accordion = driver.findElement(By.id(accordionId));
            List<WebElement> plusIcons = accordion.findElements(By.cssSelector("use[href='#phw-cns-icon-plus']"));
            if (!plusIcons.isEmpty()) {
                js.executeScript("arguments[0].click();", accordion);
                log.info("[RBC] Expanded accordion '{}'", accordionId);
            }
        } catch (Exception e) {
            log.warn("[RBC] Could not expand accordion '{}': {}", accordionId, e.getMessage());
        }
    }

    private boolean clickCheckboxByAriaLabel(WebDriver driver, JavascriptExecutor js,
                                             WebDriverWait wait, String labelPrefix) {
        By locator = By.xpath("//input[@type='checkbox' and starts-with(@aria-label, '"
                + labelPrefix + "')]");
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                WebElement checkbox = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
                js.executeScript("arguments[0].scrollIntoView({block:'center'});", checkbox);
                Thread.sleep(500);
                if (checkbox.isSelected()) {
                    log.info("[RBC] '{}' already selected", labelPrefix);
                    return true;
                }
                js.executeScript("arguments[0].click();", checkbox);
                Thread.sleep(2000);
                WebElement recheck = driver.findElement(locator);
                if (recheck.isSelected()) {
                    log.info("[RBC] Checked '{}' (attempt {})", labelPrefix, attempt);
                    return true;
                }
                log.warn("[RBC] '{}' click did not register (attempt {})", labelPrefix, attempt);
            } catch (Exception e) {
                log.warn("[RBC] Error clicking '{}' (attempt {}): {}", labelPrefix, attempt, e.getMessage());
            }
        }
        return false;
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

                // Location: navigate up to the job card container and find location
                String location = "";
                try {
                    // Go up to the nearest list item or card container
                    WebElement card = link.findElement(By.xpath("./ancestor::*[@data-ph-at-id='job-link' or contains(@class,'job')]/.."));
                    List<WebElement> singleLoc = card.findElements(By.cssSelector("span.job-location"));
                    if (!singleLoc.isEmpty()) {
                        String text = singleLoc.get(0).getText().replace("Location", "").trim();
                        if (text.startsWith("|")) text = text.substring(1).trim();
                        location = text;
                    }
                    if (location.isEmpty()) {
                        List<WebElement> multiLoc = card.findElements(By.cssSelector("ul.job-multi-locations"));
                        if (!multiLoc.isEmpty()) {
                            location = multiLoc.get(0).getText().trim();
                        }
                    }
                } catch (Exception ex) {
                    // Fallback: search globally by job seqno
                    try {
                        String seqno = link.getAttribute("data-ph-at-job-seqno-text");
                        if (seqno != null) {
                            List<WebElement> locSpans = driver.findElements(
                                    By.xpath("//span[contains(@class,'" + seqno + "') and contains(@class,'location')]"));
                            if (!locSpans.isEmpty()) {
                                location = locSpans.get(0).getText().replace("Location", "").trim();
                                if (location.startsWith("|")) location = location.substring(1).trim();
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }

                JobPosting job = new JobPosting();
                job.setCompany(COMPANY);
                job.setTitle(title);
                job.setLocation(location);
                job.setDate(LocalDate.now().toString());
                jobs.add(job);
            } catch (Exception e) {
                log.warn("[RBC] Failed to parse a job card, skipping: {}", e.getMessage());
            }
        }

        return jobs;
    }

    private boolean goToNextPage(WebDriver driver, JavascriptExecutor js,
                                 WebDriverWait wait, int nextPage) {
        try {
            String nextPageStr = String.valueOf(nextPage);

            // Find the page link with the next page number
            List<WebElement> pageLinks = driver.findElements(
                    By.cssSelector("[class*='paginat'] a.au-target"));
            for (WebElement pageLink : pageLinks) {
                if (pageLink.getText().trim().equals(nextPageStr) && pageLink.isDisplayed()) {
                    js.executeScript("arguments[0].click();", pageLink);
                    Thread.sleep(3000);
                    wait.until(ExpectedConditions.presenceOfElementLocated(
                            By.cssSelector("a[data-ph-at-id='job-link']")));
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            log.info("[RBC] No more pages to navigate");
            return false;
        }
    }
}
