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
public class SigScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(SigScraper.class);

    private static final String BASE_URL =
            "https://careers.sig.com/jobs?categories=Interns%20%2B%20Co-ops"
            + "&page=1&tags1=Technology%20-%20Software%20Engineering";

    private static final String COMPANY = "SIG";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            Thread.sleep(12000);
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
            Thread.sleep(3000);

            // SIG's job-detail links match /jobs/<id>?lang=...; pick those by href pattern
            // rather than the full domain so absolute and relative variants both match.
            List<WebElement> links = driver.findElements(
                    By.xpath("//a[contains(@href, '/jobs/') and contains(@href, '?lang')]"));

            Set<String> seenHrefs = new HashSet<>();
            for (WebElement link : links) {
                try {
                    String href = link.getAttribute("href");
                    if (href == null || !seenHrefs.add(href)) continue;
                    if (!href.matches(".*/jobs/\\d+.*")) continue;

                    String title = link.getText().trim();
                    if (title.isEmpty()) continue;

                    // SIG cards put the title link, then "Location\n<city>\n<country>\n..." in
                    // a sibling. Walk up and pull whichever line follows "Location".
                    String location = "";
                    try {
                        WebElement card = (WebElement) ((JavascriptExecutor) driver).executeScript(
                                "var n=arguments[0]; for(var i=0;i<6;i++){if(n.parentElement) n=n.parentElement;} return n;",
                                link);
                        String[] lines = card.getText().split("\\r?\\n");
                        StringBuilder loc = new StringBuilder();
                        for (int i = 0; i < lines.length; i++) {
                            if (lines[i].trim().equalsIgnoreCase("Location")) {
                                for (int j = i + 1; j < Math.min(lines.length, i + 4); j++) {
                                    String l = lines[j].trim();
                                    if (l.isEmpty()) continue;
                                    if (l.equalsIgnoreCase("Job Category")
                                            || l.equalsIgnoreCase("Start Date")
                                            || l.equalsIgnoreCase("Apply")) break;
                                    if (loc.length() > 0) loc.append(", ");
                                    loc.append(l);
                                }
                                break;
                            }
                        }
                        location = loc.toString();
                    } catch (Exception ignored) { }

                    JobPosting job = new JobPosting();
                    job.setCompany(COMPANY);
                    job.setTitle(title);
                    job.setLocation(location);
                    job.setDate(LocalDate.now().toString());
                    jobs.add(job);
                } catch (Exception e) {
                    log.warn("[SIG] Failed to parse a link, skipping: {}", e.getMessage());
                }
            }

            log.info("[SIG] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[SIG] Error scraping SIG jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
