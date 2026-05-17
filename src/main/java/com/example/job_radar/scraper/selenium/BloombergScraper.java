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
public class BloombergScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(BloombergScraper.class);

    private static final String BASE_URL =
            "https://bloomberg.avature.net/careers/SearchJobs/?1686=%5B55479%5D"
            + "&1686_format=2312&listFilterMode=1&jobRecordsPerPage=12&";

    private static final String COMPANY = "Bloomberg";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            Thread.sleep(4000);

            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("a[href*='JobDetail']")));

            List<WebElement> items = driver.findElements(
                    By.cssSelector("article.article--result, li.article--result, .article--result"));
            if (items.isEmpty()) {
                items = driver.findElements(
                        By.cssSelector("ul.articles--list > li, .searchResults li, li[class*='result']"));
            }

            for (WebElement item : items) {
                try {
                    WebElement link;
                    try {
                        link = item.findElement(By.cssSelector("a[href*='JobDetail']"));
                    } catch (Exception ex) {
                        link = item.findElement(By.cssSelector("a"));
                    }

                    String title = link.getText().trim();
                    if (title.isEmpty()) continue;

                    String location = "";
                    List<WebElement> locEls = item.findElements(
                            By.cssSelector(".job-location, [class*='location'], li[class*='location']"));
                    if (!locEls.isEmpty()) {
                        location = locEls.get(0).getText().trim();
                    } else {
                        String all = item.getText();
                        int nl = all.indexOf('\n');
                        if (nl >= 0 && nl < all.length() - 1) {
                            location = all.substring(nl + 1).trim().split("\n")[0].trim();
                        }
                    }

                    JobPosting job = new JobPosting();
                    job.setCompany(COMPANY);
                    job.setTitle(title);
                    job.setLocation(location);
                    job.setDate(LocalDate.now().toString());
                    jobs.add(job);
                } catch (Exception e) {
                    log.warn("[Bloomberg] Failed to parse a posting, skipping: {}", e.getMessage());
                }
            }

            log.info("[Bloomberg] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Bloomberg] Error scraping Bloomberg jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
