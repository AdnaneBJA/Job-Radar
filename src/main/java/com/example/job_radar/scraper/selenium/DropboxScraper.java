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
public class DropboxScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(DropboxScraper.class);

    private static final String BASE_URL =
            "https://www.dropbox.jobs/en/jobs/?search="
            + "&location=Remote+-+US%3A+All+locations"
            + "&location=Remote+-+Canada%3A+Select+locations"
            + "&team=Engineering&pagesize=20#results";

    private static final String COMPANY = "Dropbox";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(25));
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("a[href*='/en/jobs/']")));
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            Thread.sleep(2500);

            List<WebElement> links = driver.findElements(By.cssSelector("a[href*='/en/jobs/']"));

            Set<String> seenHrefs = new HashSet<>();
            for (WebElement link : links) {
                try {
                    String href = link.getAttribute("href");
                    if (href == null) continue;
                    if (!href.matches(".*/en/jobs/\\d+/.+")) continue;
                    if (!seenHrefs.add(href)) continue;

                    String title = link.getText().trim();
                    if (title.isEmpty()) {
                        title = link.getAttribute("textContent");
                        if (title != null) title = title.trim();
                    }
                    if (title == null || title.isEmpty()) continue;

                    // Walk up ancestors until innerText contains "Remote -", then pull the
                    // first such line. Cap at 5 levels so we don't bubble out to globals.
                    String location = "";
                    for (int level = 1; level <= 5 && location.isEmpty(); level++) {
                        try {
                            WebElement card = (WebElement) js.executeScript(
                                    "var n=arguments[0]; for(var i=0;i<arguments[1];i++){if(n.parentElement) n=n.parentElement;} return n;",
                                    link, level);
                            String text = (String) js.executeScript("return arguments[0].innerText;", card);
                            if (text == null) continue;
                            if (!text.contains("Remote -")) continue;
                            for (String l : text.split("\\r?\\n")) {
                                String t = l.trim();
                                if (t.startsWith("Remote -")) { location = t; break; }
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
                    log.warn("[Dropbox] Failed to parse a link, skipping: {}", e.getMessage());
                }
            }

            log.info("[Dropbox] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Dropbox] Error scraping Dropbox jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
