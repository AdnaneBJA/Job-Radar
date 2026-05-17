package com.example.job_radar.scraper.selenium;

import com.example.job_radar.model.JobPosting;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class DoorDashScraper extends SeleniumScraper {

    // DoorDash's job-search SPA tail-renders rows progressively only when the locale
    // headers are NOT forced to en-US; setting --lang causes the SPA to return a
    // single-row response. Use a minimal Chrome config for this site.
    @Override
    public WebDriver createDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");
        return new ChromeDriver(options);
    }

    private static final Logger log = LoggerFactory.getLogger(DoorDashScraper.class);

    private static final String BASE_URL =
            "https://careersatdoordash.com/job-search/?intern=1&spage=1";

    private static final String COMPANY = "DoorDash";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            Thread.sleep(6000);

            List<WebElement> items = driver.findElements(By.cssSelector(".job-item"));

            for (WebElement item : items) {
                try {
                    // Rows beyond the first can be off-screen on initial render; getText()
                    // returns "" for those. Read textContent directly so we capture all rows.
                    String title;
                    try {
                        title = item.findElement(By.cssSelector(".title-container .value"))
                                .getAttribute("textContent").trim();
                    } catch (Exception ex) {
                        title = item.findElement(By.cssSelector(".value"))
                                .getAttribute("textContent").trim();
                    }
                    if (title.isEmpty()) continue;

                    // Layout (textContent, no newlines): "Title | Job ID: N | Location | <city, country> | Department | ..."
                    // Use innerText via textContent with explicit child queries to be safe.
                    String location = "";
                    List<WebElement> values = item.findElements(By.cssSelector(".value"));
                    for (int i = 0; i + 1 < values.size(); i++) {
                        String label = values.get(i).getAttribute("textContent").trim();
                        if (label.equalsIgnoreCase("Location")) {
                            location = values.get(i + 1).getAttribute("textContent").trim();
                            break;
                        }
                    }
                    if (location.isEmpty()) {
                        // Fall back to splitting the full textContent on labels
                        String full = item.getAttribute("textContent").replaceAll("\\s+", " ").trim();
                        int li = full.indexOf("Location");
                        int di = full.indexOf("Department");
                        if (li >= 0 && di > li) {
                            location = full.substring(li + "Location".length(), di).trim();
                        }
                    }

                    JobPosting job = new JobPosting();
                    job.setCompany(COMPANY);
                    job.setTitle(title);
                    job.setLocation(location);
                    job.setDate(LocalDate.now().toString());
                    jobs.add(job);
                } catch (Exception e) {
                    log.warn("[DoorDash] Failed to parse a job item, skipping: {}", e.getMessage());
                }
            }

            log.info("[DoorDash] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[DoorDash] Error scraping DoorDash jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
