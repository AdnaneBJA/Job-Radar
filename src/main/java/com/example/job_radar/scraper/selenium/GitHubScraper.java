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
import java.util.List;

@Component
public class GitHubScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(GitHubScraper.class);

    private static final String BASE_URL =
            "https://www.github.careers/careers-home/jobs?tags4=Intern&page=1";

    private static final String COMPANY = "GitHub";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            // github.careers is now only a hub: it lists no jobs itself and links out to iCIMS
            // login portals (careers-githubinc.icims.com), which redirect/refuse both headless
            // browsers and server fetches. Wait briefly for real job links and degrade quietly
            // rather than hanging the batch with a 20s timeout stack trace.
            // TODO: revisit via the iCIMS portal feed (needs the portal's hashed id / session).
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(12));
            List<WebElement> links;
            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("a[href*='/careers-home/jobs/']")));
                Thread.sleep(2000);
                links = driver.findElements(
                        By.cssSelector("a[href*='/careers-home/jobs/']"));
            } catch (org.openqa.selenium.TimeoutException te) {
                log.info("[GitHub] No job listings on the careers hub (jobs moved behind iCIMS portals); returning 0.");
                return jobs;
            }

            for (WebElement link : links) {
                try {
                    String href = link.getAttribute("href");
                    if (href == null || !href.matches(".*/careers-home/jobs/\\d+.*")) continue;

                    String title = link.getText().trim();
                    if (title.isEmpty()) continue;

                    String location = "";
                    try {
                        WebElement card = (WebElement) ((JavascriptExecutor) driver).executeScript(
                                "var n=arguments[0]; for(var i=0;i<5;i++){if(n.parentElement) n=n.parentElement;} return n;",
                                link);
                        String full = card.getText();
                        // Card layout: Title | Req ID: NNN | Location | <country> | home | <remote/onsite> | Categories | <cat>
                        String[] lines = full.split("\\r?\\n");
                        for (int i = 0; i < lines.length - 1; i++) {
                            if (lines[i].trim().equalsIgnoreCase("Location")) {
                                StringBuilder loc = new StringBuilder();
                                for (int j = i + 1; j < lines.length; j++) {
                                    String l = lines[j].trim();
                                    if (l.isEmpty()) continue;
                                    if (l.equalsIgnoreCase("home") || l.equalsIgnoreCase("Categories")
                                            || l.equalsIgnoreCase("Apply Now")) break;
                                    if (loc.length() > 0) loc.append(", ");
                                    loc.append(l);
                                }
                                location = loc.toString();
                                break;
                            }
                        }
                    } catch (Exception ignored) { }

                    JobPosting job = new JobPosting();
                    job.setCompany(COMPANY);
                    job.setTitle(title);
                    job.setLocation(location);
                    job.setDate(LocalDate.now().toString());
                    jobs.add(job);
                } catch (Exception e) {
                    log.warn("[GitHub] Failed to parse a link, skipping: {}", e.getMessage());
                }
            }

            log.info("[GitHub] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[GitHub] Error scraping GitHub jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
