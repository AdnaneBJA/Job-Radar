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
public class FaireScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(FaireScraper.class);

    private static final String BASE_URL = "https://www.faire.com/careers/openings";
    private static final String COMPANY = "Faire";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(25));
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("a[href*='greenhouse.io/faire/jobs']")));
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
            Thread.sleep(2500);

            // Locate the Engineering section heading and gather every greenhouse link
            // that sits between it and the next sibling section heading.
            @SuppressWarnings("unchecked")
            List<WebElement> engLinks = (List<WebElement>) ((JavascriptExecutor) driver).executeScript(
                    "var headings = document.querySelectorAll('h1, h2, h3, h4, h5');" +
                    "var start = null, stop = null;" +
                    "for (var i = 0; i < headings.length; i++) {" +
                    "  var t = headings[i].textContent.trim();" +
                    "  if (start === null && (t === 'Engineering' || t === 'Ingénierie')) { start = headings[i]; continue; }" +
                    "  if (start !== null) { stop = headings[i]; break; }" +
                    "}" +
                    "if (!start) return [];" +
                    "var stopRange = stop;" +
                    "var all = Array.from(document.querySelectorAll(\"a[href*='greenhouse.io/faire/jobs']\"));" +
                    "return all.filter(function(a){" +
                    "  return (start.compareDocumentPosition(a) & Node.DOCUMENT_POSITION_FOLLOWING)" +
                    "    && (!stopRange || (stopRange.compareDocumentPosition(a) & Node.DOCUMENT_POSITION_PRECEDING));" +
                    "});");

            Set<String> seenHrefs = new HashSet<>();
            for (WebElement link : engLinks) {
                try {
                    String href = link.getAttribute("href");
                    if (href == null || !seenHrefs.add(href)) continue;

                    List<WebElement> paragraphs = link.findElements(By.tagName("p"));
                    String title = "";
                    String location = "";
                    if (paragraphs.size() >= 1) title = paragraphs.get(0).getText().trim();
                    if (paragraphs.size() >= 2) location = paragraphs.get(1).getText().trim();
                    if (title.isEmpty()) {
                        title = link.getText().trim();
                    }
                    if (title.isEmpty()) continue;

                    JobPosting job = new JobPosting();
                    job.setCompany(COMPANY);
                    job.setTitle(title);
                    job.setLocation(location);
                    job.setDate(LocalDate.now().toString());
                    jobs.add(job);
                } catch (Exception e) {
                    log.warn("[Faire] Failed to parse a link, skipping: {}", e.getMessage());
                }
            }

            log.info("[Faire] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Faire] Error scraping Faire jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
