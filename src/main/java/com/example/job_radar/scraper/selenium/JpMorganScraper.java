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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class JpMorganScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(JpMorganScraper.class);

    private static final String BASE_URL =
            "https://www.jpmorganchase.com/careers/explore-opportunities/programs";
    private static final String COMPANY = "JPMorgan Chase";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            driver.get(BASE_URL);
            Thread.sleep(10000);
            JavascriptExecutor js = (JavascriptExecutor) driver;

            // Click the "Internship" filter chip so only that category section renders.
            js.executeScript(
                    "var els = document.querySelectorAll('button, a, p, span, label, div');" +
                    "for (var i = 0; i < els.length; i++) {" +
                    "  var t = els[i].textContent.trim();" +
                    "  if (t === 'Internship' || t === 'Internship (39)' || /^Internship\\s*\\(\\d+\\)$/.test(t)) {" +
                    "    els[i].click();" +
                    "    return;" +
                    "  }" +
                    "}");
            Thread.sleep(4000);
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            Thread.sleep(3000);
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            Thread.sleep(3000);

            // The page renders all programs grouped under category headings ("INTERNSHIP",
            // "FULL-TIME", "SCHOOL PROGRAMS & APPRENTICESHIPS", etc). Parse the visible body
            // and keep INTERNSHIP entries whose location list mentions Canada or the US.
            // The category chips that appear in the filter UI are title-case "Internship"
            // followed by "(N)" — we ignore those and match the actual UPPER-CASE heading.
            String body = driver.findElement(By.tagName("body")).getText();
            String[] lines = body.split("\\r?\\n");

            Set<String> seen = new HashSet<>();
            for (int i = 0; i + 2 < lines.length; i++) {
                if (!lines[i].trim().equals("INTERNSHIP")) continue;

                String title = "";
                String location = "";
                int j = i + 1;
                while (j < lines.length && lines[j].trim().isEmpty()) j++;
                if (j < lines.length) {
                    title = lines[j].trim();
                    j++;
                    while (j < lines.length && lines[j].trim().isEmpty()) j++;
                    if (j < lines.length) location = lines[j].trim();
                }

                if (title.isEmpty()) continue;
                if (title.length() < 6) continue;
                if (!seen.add(title)) continue;
                // The location filter the user applies (Canada AND United States) is an
                // AND on the page — both must appear in the role's available-locations list.
                if (!location.contains("Canada") || !location.contains("United States")) continue;

                JobPosting job = new JobPosting();
                job.setCompany(COMPANY);
                job.setTitle(title);
                job.setLocation(location);
                job.setDate(LocalDate.now().toString());
                jobs.add(job);
            }

            log.info("[JPMorgan] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[JPMorgan] Error scraping JPMorgan jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
