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
public class TenstorrentScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(TenstorrentScraper.class);

    private static final String BASE_URL = "https://tenstorrent.com/university";
    private static final String COMPANY = "Tenstorrent";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            Thread.sleep(8000);
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            Thread.sleep(3000);

            // Click "Show all postings" — the React handler only fires on a real user-style
            // click, so use WebElement.click() rather than JS .click().
            try {
                WebElement btn = driver.findElement(
                        By.xpath("//button[contains(., 'Show all postings')]"));
                js.executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
                Thread.sleep(500);
                btn.click();
                log.info("[Tenstorrent] clicked 'Show all postings'");
            } catch (Exception ex) {
                log.warn("[Tenstorrent] Could not click 'Show all postings': {}", ex.getMessage());
            }
            Thread.sleep(4000);
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            Thread.sleep(3000);
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            Thread.sleep(2000);

            int ghCount = driver.findElements(By.cssSelector("a[href*='greenhouse']")).size();
            int ttCount = driver.findElements(By.cssSelector("a[href*='greenhouse.io/tenstorrent']")).size();
            log.info("[Tenstorrent] post-click link counts: greenhouse={} tenstorrent-scoped={}", ghCount, ttCount);

            // Each greenhouse-hosted job link's textContent is laid out as:
            // <Department>\n<Title>\n<Location>\nApply
            List<WebElement> links = driver.findElements(
                    By.cssSelector("a[href*='greenhouse']"));

            Set<String> seenHrefs = new HashSet<>();
            for (WebElement link : links) {
                try {
                    String href = link.getAttribute("href");
                    if (href == null || !seenHrefs.add(href)) continue;

                    // textContent strips visual line breaks for inline children, so use
                    // innerText which preserves the rendered layout.
                    String text = (String) js.executeScript("return arguments[0].innerText;", link);
                    if (text == null) continue;
                    String[] lines = text.split("\\r?\\n");
                    List<String> nonEmpty = new ArrayList<>();
                    for (String l : lines) {
                        String t = l.trim();
                        if (!t.isEmpty() && !t.equalsIgnoreCase("Apply")) nonEmpty.add(t);
                    }
                    if (nonEmpty.size() < 2) continue;

                    // Last non-empty line is location; the line before it is the title.
                    String location = nonEmpty.get(nonEmpty.size() - 1);
                    String title = nonEmpty.get(nonEmpty.size() - 2);
                    if (title.isEmpty()) continue;

                    JobPosting job = new JobPosting();
                    job.setCompany(COMPANY);
                    job.setTitle(title);
                    job.setLocation(location);
                    job.setDate(LocalDate.now().toString());
                    jobs.add(job);
                } catch (Exception e) {
                    log.warn("[Tenstorrent] Failed to parse a link, skipping: {}", e.getMessage());
                }
            }

            log.info("[Tenstorrent] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Tenstorrent] Error scraping Tenstorrent jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
