package com.example.job_radar.scraper.selenium;

import com.example.job_radar.model.JobPosting;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class NotionScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(NotionScraper.class);

    private static final String BASE_URL =
            "https://www.notion.com/careers?department=university#open-positions";
    private static final String COMPANY = "Notion";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            Thread.sleep(8000);
            JavascriptExecutor js = (JavascriptExecutor) driver;

            // Notion lazy-loads the careers grid as you scroll. Step down the page so the
            // University section's job links actually mount in the DOM.
            for (int i = 0; i < 8; i++) {
                js.executeScript("window.scrollBy(0, 1200);");
                Thread.sleep(800);
            }
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            Thread.sleep(2000);

            // Notion's career site routes job detail pages under /careers/<slug>. Pull every
            // anchor that points to a job detail, then parse each link's title + location.
            List<WebElement> links = driver.findElements(
                    By.cssSelector("a[href*='/careers/']"));
            log.info("[Notion] found {} candidate /careers/ links", links.size());

            Set<String> seenHrefs = new HashSet<>();
            for (WebElement link : links) {
                try {
                    String href = link.getAttribute("href");
                    if (href == null) continue;
                    // Filter out the listing page itself and other non-detail anchors.
                    if (href.endsWith("/careers")
                            || href.endsWith("/careers/")
                            || href.contains("#open-positions")
                            || href.contains("?department=")
                            || href.contains("/careers/life-at-notion")
                            || href.contains("/careers/benefits")) {
                        continue;
                    }
                    if (!seenHrefs.add(href)) continue;

                    String text = (String) js.executeScript("return arguments[0].innerText;", link);
                    if (text == null) continue;
                    String[] lines = text.split("\\r?\\n");
                    List<String> nonEmpty = new ArrayList<>();
                    for (String l : lines) {
                        String t = l.trim();
                        if (!t.isEmpty()) nonEmpty.add(t);
                    }
                    if (nonEmpty.size() < 2) continue;

                    String title = nonEmpty.get(0);
                    String location = nonEmpty.get(nonEmpty.size() - 1);
                    if (title.isEmpty()) continue;

                    JobPosting job = new JobPosting();
                    job.setCompany(COMPANY);
                    job.setTitle(title);
                    job.setLocation(location);
                    job.setDate(LocalDate.now().toString());
                    jobs.add(job);
                } catch (Exception ex) {
                    log.warn("[Notion] Failed to parse a link, skipping: {}", ex.getMessage());
                }
            }

            log.info("[Notion] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Notion] Error scraping Notion University jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
