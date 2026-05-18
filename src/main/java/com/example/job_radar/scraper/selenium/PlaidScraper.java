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
import java.util.regex.Pattern;

@Component
public class PlaidScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(PlaidScraper.class);

    private static final String BASE_URL = "https://plaid.com/careers/?search=Software#search";
    private static final String COMPANY = "Plaid";

    private static final Pattern EXCLUDE = Pattern.compile(
            "\\b(Senior|Staff)\\b", Pattern.CASE_INSENSITIVE);

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(25));
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("a[href*='/careers/openings/']")));
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
            Thread.sleep(2500);

            List<WebElement> links = driver.findElements(
                    By.cssSelector("a[href*='/careers/openings/']"));

            Set<String> seenHrefs = new HashSet<>();
            for (WebElement link : links) {
                try {
                    String href = link.getAttribute("href");
                    if (href == null || !seenHrefs.add(href)) continue;

                    // Plaid's <a> text is just "See role". Both title and location live in
                    // the URL itself: /openings/<cost-center>/<office-slug>/<role-slug>/.
                    String path = href.replaceAll(".*/openings/", "").replaceAll("/+$", "");
                    String[] segs = path.split("/");
                    if (segs.length < 3) continue;
                    String officeSlug = segs[segs.length - 2];
                    String roleSlug = segs[segs.length - 1];
                    String title = toTitleCase(roleSlug.replace('-', ' '));
                    String location = toTitleCase(officeSlug.replace('-', ' '));
                    if (title.isEmpty()) continue;
                    if (EXCLUDE.matcher(title).find()) continue;

                    JobPosting job = new JobPosting();
                    job.setCompany(COMPANY);
                    job.setTitle(title);
                    job.setLocation(location);
                    job.setDate(LocalDate.now().toString());
                    jobs.add(job);
                } catch (Exception e) {
                    log.warn("[Plaid] Failed to parse a link, skipping: {}", e.getMessage());
                }
            }

            log.info("[Plaid] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Plaid] Error scraping Plaid jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }

    private static String toTitleCase(String input) {
        if (input == null || input.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(input.length());
        boolean prevSpace = true;
        for (char c : input.toCharArray()) {
            if (Character.isWhitespace(c)) {
                sb.append(c);
                prevSpace = true;
            } else if (prevSpace) {
                sb.append(Character.toUpperCase(c));
                prevSpace = false;
            } else {
                sb.append(c);
            }
        }
        // Common acronym fixups so they don't read like "Ai" or "Ml"
        String s = sb.toString();
        s = s.replaceAll("(?i)\\bAi\\b", "AI");
        s = s.replaceAll("(?i)\\bMl\\b", "ML");
        s = s.replaceAll("(?i)\\bGbr\\b", "Europe");
        return s;
    }
}
