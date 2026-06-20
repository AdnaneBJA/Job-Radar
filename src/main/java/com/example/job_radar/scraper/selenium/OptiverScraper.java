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
public class OptiverScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(OptiverScraper.class);

    private static final String BASE_URL =
            "https://optiver.com/working-at-optiver/career-opportunities/";
    private static final String COMPANY = "Optiver";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            Thread.sleep(8000);
            JavascriptExecutor js = (JavascriptExecutor) driver;

            // Accept the cookie banner if present so it doesn't intercept dropdown clicks.
            for (String sel : new String[]{
                    "#onetrust-accept-btn-handler",
                    "button[aria-label='Accept All']",
                    "button.optanon-allow-all"}) {
                try {
                    WebElement btn = driver.findElement(By.cssSelector(sel));
                    js.executeScript("arguments[0].click();", btn);
                    Thread.sleep(1000);
                    break;
                } catch (Exception ignored) { }
            }

            js.executeScript("window.scrollBy(0, 600);");
            Thread.sleep(1000);

            selectByVisibleText(driver, "All levels", "Internship");
            Thread.sleep(1500);
            selectByVisibleText(driver, "All Departments", "Technology");
            Thread.sleep(2500);

            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            Thread.sleep(2000);

            // Every posting renders as a card with a "Learn more" link to the job detail.
            // Pull each card's innerText so the title + "Department • Location" line are
            // preserved with line breaks.
            List<WebElement> cards = driver.findElements(
                    By.xpath("//a[.//text()[contains(., 'Learn more')]]"));
            if (cards.isEmpty()) {
                cards = driver.findElements(By.cssSelector("a[href*='/careers/']"));
            }
            log.info("[Optiver] found {} candidate cards", cards.size());

            Set<String> seenKeys = new HashSet<>();
            for (WebElement card : cards) {
                try {
                    String text = (String) js.executeScript("return arguments[0].innerText;", card);
                    if (text == null) continue;
                    String[] lines = text.split("\\r?\\n");
                    List<String> nonEmpty = new ArrayList<>();
                    for (String l : lines) {
                        String t = l.trim();
                        if (!t.isEmpty() && !t.equalsIgnoreCase("Learn more")) nonEmpty.add(t);
                    }
                    // Expected layout: "Internship" / "<Title>" / "<Dept> • <City>"
                    // Internship-only filter is applied, so skip cards missing that token.
                    boolean hasInternship = nonEmpty.stream().anyMatch(s -> s.equalsIgnoreCase("Internship"));
                    if (!hasInternship) continue;
                    if (nonEmpty.size() < 3) continue;

                    String title = null;
                    String location = null;
                    for (int i = 0; i < nonEmpty.size(); i++) {
                        if (nonEmpty.get(i).equalsIgnoreCase("Internship")) {
                            if (i + 1 < nonEmpty.size()) title = nonEmpty.get(i + 1);
                            if (i + 2 < nonEmpty.size()) location = nonEmpty.get(i + 2);
                            break;
                        }
                    }
                    if (title == null || title.isEmpty()) continue;
                    if (location == null) location = "";

                    String key = title + "|" + location;
                    if (!seenKeys.add(key)) continue;

                    JobPosting job = new JobPosting();
                    job.setCompany(COMPANY);
                    job.setTitle(title);
                    job.setLocation(location);
                    job.setDate(LocalDate.now().toString());
                    jobs.add(job);
                } catch (Exception ex) {
                    log.warn("[Optiver] Failed to parse a card, skipping: {}", ex.getMessage());
                }
            }

            log.info("[Optiver] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Optiver] Error scraping Optiver jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }

    // Optiver's filters are React-rendered custom dropdowns, not native <select>. The
    // pattern is: click the trigger button that currently shows <currentLabel>, then
    // click the option whose visible text equals <targetLabel>.
    private void selectByVisibleText(WebDriver driver, String currentLabel, String targetLabel) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        try {
            WebElement trigger = driver.findElement(By.xpath(
                    "//*[self::button or self::div or self::span]"
                            + "[normalize-space(text())='" + currentLabel + "']"));
            js.executeScript("arguments[0].scrollIntoView({block:'center'});", trigger);
            Thread.sleep(400);
            try {
                trigger.click();
            } catch (Exception ex) {
                js.executeScript("arguments[0].click();", trigger);
            }
            Thread.sleep(1000);

            WebElement option = driver.findElement(By.xpath(
                    "//*[self::li or self::div or self::span or self::button or self::a]"
                            + "[normalize-space(text())='" + targetLabel + "']"));
            js.executeScript("arguments[0].scrollIntoView({block:'center'});", option);
            Thread.sleep(300);
            try {
                option.click();
            } catch (Exception ex) {
                js.executeScript("arguments[0].click();", option);
            }
            log.info("[Optiver] selected '{}' in '{}' filter", targetLabel, currentLabel);
        } catch (Exception ex) {
            log.warn("[Optiver] Could not select '{}' in '{}': {}", targetLabel, currentLabel, ex.getMessage());
        }
    }
}
