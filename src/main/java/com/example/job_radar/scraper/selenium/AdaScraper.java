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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class AdaScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(AdaScraper.class);

    private static final String BASE_URL = "https://job-boards.greenhouse.io/ada18";
    private static final String COMPANY = "Ada";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("a[href*='/jobs/']")));
            Thread.sleep(2000);

            // Greenhouse boards group postings under a department heading. Locate the
            // "Product Development" heading and capture every job link until the next heading.
            @SuppressWarnings("unchecked")
            List<WebElement> deptLinks = (List<WebElement>) ((JavascriptExecutor) driver).executeScript(
                    "var headings = document.querySelectorAll('h1, h2, h3, h4');" +
                    "var start = null, stop = null;" +
                    "for (var i = 0; i < headings.length; i++) {" +
                    "  var t = headings[i].textContent.trim();" +
                    "  if (start === null && t === 'Product Development') { start = headings[i]; continue; }" +
                    "  if (start !== null && headings[i].tagName === start.tagName) { stop = headings[i]; break; }" +
                    "}" +
                    "if (!start) return [];" +
                    "var stopNode = stop;" +
                    "var all = Array.from(document.querySelectorAll(\"a[href*='/jobs/']\"));" +
                    "return all.filter(function(a){" +
                    "  return (start.compareDocumentPosition(a) & Node.DOCUMENT_POSITION_FOLLOWING)" +
                    "    && (!stopNode || (stopNode.compareDocumentPosition(a) & Node.DOCUMENT_POSITION_PRECEDING));" +
                    "});");

            Set<String> seenHrefs = new HashSet<>();
            for (WebElement link : deptLinks) {
                try {
                    String href = link.getAttribute("href");
                    if (href == null || !seenHrefs.add(href)) continue;

                    String title = link.getText().trim();
                    String location = "";

                    String[] lines = title.split("\\r?\\n");
                    if (lines.length >= 2) {
                        title = lines[0].trim();
                        location = lines[1].trim();
                    }
                    if (title.isEmpty()) continue;

                    JobPosting job = new JobPosting();
                    job.setCompany(COMPANY);
                    job.setTitle(title);
                    job.setLocation(location);
                    job.setDate(LocalDate.now().toString());
                    jobs.add(job);
                } catch (Exception e) {
                    log.warn("[Ada] Failed to parse a link, skipping: {}", e.getMessage());
                }
            }

            log.info("[Ada] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Ada] Error scraping Ada jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
