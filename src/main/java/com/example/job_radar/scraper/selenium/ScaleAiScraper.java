package com.example.job_radar.scraper.selenium;

import com.example.job_radar.model.JobPosting;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class ScaleAiScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(ScaleAiScraper.class);

    private static final String BASE_URL = "https://scale.com/careers#open-roles";
    private static final String COMPANY = "Scale AI";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            Thread.sleep(10000);
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            Thread.sleep(3000);

            // Scale renders every department's jobs as a separate section in the DOM
            // regardless of which one the dropdown filter has visually selected. Parse the
            // body text for the "University" section and read the title/location pairs.
            String body = (String) js.executeScript("return document.body.innerText;");
            log.info("[Scale AI] body length: {}", body == null ? 0 : body.length());
            String[] lines = body.split("\\r?\\n");

            // Strip empty lines so blank separators between the heading, the role count,
            // and the rows don't break the parser.
            List<String> ne = new ArrayList<>();
            for (String l : lines) if (!l.trim().isEmpty()) ne.add(l.trim());

            int sectionStart = -1;
            for (int i = 0; i < ne.size() - 1; i++) {
                if (ne.get(i).equals("University")
                        && ne.get(i + 1).matches("\\d+\\s*ROLES?")) {
                    sectionStart = i + 2;
                    break;
                }
            }
            if (sectionStart < 0) {
                log.warn("[Scale AI] Could not locate the University section");
                return jobs;
            }

            for (int i = sectionStart; i + 1 < ne.size(); ) {
                String title = ne.get(i);
                if (title.equalsIgnoreCase("APPLY ?") || title.equalsIgnoreCase("APPLY")) { i++; continue; }
                // Stop at the next section / footer
                if (title.equals("Join University") || title.equals("Student Careers")
                        || title.equals("Explore Opportunities") || title.equals("PRODUCTS")) {
                    break;
                }
                String location = ne.get(i + 1);
                String apply = i + 2 < ne.size() ? ne.get(i + 2) : "";
                if (!apply.toUpperCase().startsWith("APPLY")) {
                    break;
                }

                JobPosting job = new JobPosting();
                job.setCompany(COMPANY);
                job.setTitle(title);
                job.setLocation(location);
                job.setDate(LocalDate.now().toString());
                jobs.add(job);

                i += 3;
            }

            log.info("[Scale AI] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[Scale AI] Error scraping Scale AI jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
