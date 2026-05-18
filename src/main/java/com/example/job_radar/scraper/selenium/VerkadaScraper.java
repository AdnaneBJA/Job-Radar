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
public class VerkadaScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(VerkadaScraper.class);

    private static final String BASE_URL = "https://www.verkada.com/careers/";
    private static final String COMPANY = "Verkada";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            Thread.sleep(10000);
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            Thread.sleep(2000);

            // Uncheck Full-Time and Full Time - Fixed Term so only Intern/Co-op remains.
            for (String id : new String[]{"Full Time - Fixed Term Employment", "Full-Time"}) {
                try {
                    WebElement cb = driver.findElement(By.cssSelector("input[id='" + id + "']"));
                    if (cb.isSelected()) {
                        js.executeScript("arguments[0].click();", cb);
                        Thread.sleep(800);
                    }
                } catch (Exception ex) {
                    log.warn("[Verkada] Could not uncheck '{}': {}", id, ex.getMessage());
                }
            }
            Thread.sleep(3000);
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            Thread.sleep(2000);

            // After the filter, Verkada lists each job as an <a> with the title and the
            // location on separate lines.
            List<WebElement> links = driver.findElements(
                    By.cssSelector("a[href*='greenhouse'], a[href*='/careers/'], a[href*='boards.greenhouse']"));

            Set<String> seenHrefs = new HashSet<>();
            for (WebElement link : links) {
                try {
                    String href = link.getAttribute("href");
                    if (href == null || !seenHrefs.add(href)) continue;
                    if (href.endsWith("/careers/") || href.contains("#")) continue;

                    String text = (String) js.executeScript("return arguments[0].innerText;", link);
                    if (text == null) continue;
                    String[] lines = text.split("\\r?\\n");
                    String title = lines.length > 0 ? lines[0].trim() : "";
                    String location = lines.length > 1 ? lines[1].trim() : "";

                    if (title.isEmpty()) continue;
                    // Drop "Learn more" / "Apply" / nav-style links that match the same href filter.
                    if (title.equalsIgnoreCase("Learn more") || title.equalsIgnoreCase("Apply")
                            || title.equalsIgnoreCase("View role")) continue;
                    if (location.isEmpty()) {
                        try {
                            WebElement card = (WebElement) js.executeScript(
                                    "var n=arguments[0]; for(var i=0;i<4;i++){if(n.parentElement) n=n.parentElement;} return n;",
                                    link);
                            String full = (String) js.executeScript("return arguments[0].innerText;", card);
                            if (full != null) {
                                String[] cardLines = full.split("\\r?\\n");
                                for (int i = 0; i < cardLines.length; i++) {
                                    if (cardLines[i].trim().equals(title) && i + 1 < cardLines.length) {
                                        location = cardLines[i + 1].trim();
                                        break;
                                    }
                                }
                            }
                        } catch (Exception ignored) { }
                    }

                    JobPosting job = new JobPosting();
                    job.setCompany(COMPANY);
                    job.setTitle(title);
                    job.setLocation(location);
                    job.setDate(LocalDate.now().toString());
                    jobs.add(job);
                } catch (Exception e) {
                    log.warn("[Verkada] Failed to parse a link, skipping: {}", e.getMessage());
                }
            }

            log.info("[Verkada] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Verkada] Error scraping Verkada jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
