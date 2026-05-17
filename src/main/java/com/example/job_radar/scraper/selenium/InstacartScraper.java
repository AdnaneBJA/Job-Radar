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
import java.util.List;
import java.util.regex.Pattern;

@Component
public class InstacartScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(InstacartScraper.class);

    private static final String BASE_URL = "https://www.instacart.careers/current-openings";
    private static final String COMPANY = "Instacart";

    // Word-boundary match on "Intern" so titles like "International" don't slip through.
    private static final Pattern INTERN_WORD = Pattern.compile("\\bIntern(s|ship)?\\b");

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            Thread.sleep(8000);
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            Thread.sleep(2000);

            // Expand every department accordion so its jobs render in the DOM.
            List<WebElement> summaries = driver.findElements(
                    By.cssSelector("summary[class*='AllJobsSection_accordionSummary']"));
            log.info("[Instacart] Found {} department accordions", summaries.size());
            for (WebElement s : summaries) {
                try { js.executeScript("arguments[0].click();", s); } catch (Exception ignored) { }
            }
            Thread.sleep(2500);

            List<WebElement> items = driver.findElements(
                    By.cssSelector("li[class*='AllJobsSection_jobItem']"));
            log.info("[Instacart] Found {} job items across all departments", items.size());

            for (WebElement item : items) {
                try {
                    WebElement titleEl = item.findElement(
                            By.cssSelector("a[class*='AllJobsSection_jobLink']"));
                    String title = titleEl.getAttribute("textContent");
                    if (title == null) continue;
                    title = title.trim();
                    if (title.isEmpty()) continue;
                    if (!INTERN_WORD.matcher(title).find()) continue;

                    String location = "";
                    List<WebElement> locEls = item.findElements(
                            By.cssSelector("span[class*='AllJobsSection_jobLocation']"));
                    if (!locEls.isEmpty()) {
                        String t = locEls.get(0).getAttribute("textContent");
                        if (t != null) location = t.trim();
                    }

                    String employmentType = "";
                    List<WebElement> typeEls = item.findElements(
                            By.cssSelector("span[class*='AllJobsSection_jobEmploymentType']"));
                    if (!typeEls.isEmpty()) {
                        String t = typeEls.get(0).getAttribute("textContent");
                        if (t != null) employmentType = t.trim();
                    }

                    if (!location.isEmpty() && !employmentType.isEmpty()) {
                        location = location + " · " + employmentType;
                    }

                    JobPosting job = new JobPosting();
                    job.setCompany(COMPANY);
                    job.setTitle(title);
                    job.setLocation(location);
                    job.setDate(LocalDate.now().toString());
                    jobs.add(job);
                } catch (Exception e) {
                    log.warn("[Instacart] Failed to parse a job item, skipping: {}", e.getMessage());
                }
            }

            log.info("[Instacart] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Instacart] Error scraping Instacart jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
