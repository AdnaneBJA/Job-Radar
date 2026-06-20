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
public class TikTokScraper extends SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(TikTokScraper.class);

    private static final String BASE_URL =
            "https://lifeattiktok.com/search?recruitment_id_list=202%2C301"
                    + "&job_category_id_list=6704215862603155720"
                    + "&subject_id_list=7391494724038953254"
                    + "&location_code_list=&keyword=&limit=12&offset=";
    private static final String COMPANY = "TikTok";
    private static final int PAGE_SIZE = 12;
    private static final int MAX_PAGES = 10;

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();
        WebDriver driver = createDriver();
        Set<String> seenTitles = new HashSet<>();

        try {
            for (int page = 0; page < MAX_PAGES; page++) {
                int offset = page * PAGE_SIZE;
                driver.get(BASE_URL + offset);
                Thread.sleep(6000);
                ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
                Thread.sleep(1500);

                // Each job card is an anchor to /position/<id>/detail. Read its innerText
                // (preserves visual line breaks) so we can split title / location.
                List<WebElement> cards = driver.findElements(
                        By.cssSelector("a[href*='/position/']"));
                int beforeAdds = jobs.size();
                Set<String> cardKeys = new HashSet<>();
                for (WebElement card : cards) {
                    try {
                        String href = card.getAttribute("href");
                        if (href == null || !cardKeys.add(href)) continue;

                        String text = (String) ((JavascriptExecutor) driver)
                                .executeScript("return arguments[0].innerText;", card);
                        if (text == null) continue;
                        String[] lines = text.split("\\r?\\n");
                        List<String> nonEmpty = new ArrayList<>();
                        for (String l : lines) {
                            String t = l.trim();
                            if (!t.isEmpty()) nonEmpty.add(t);
                        }
                        if (nonEmpty.isEmpty()) continue;

                        String title = nonEmpty.get(0);
                        String location = nonEmpty.size() >= 2
                                ? nonEmpty.get(nonEmpty.size() - 1)
                                : "";
                        if (title.isEmpty()) continue;

                        String key = title + "|" + location;
                        if (!seenTitles.add(key)) continue;

                        JobPosting job = new JobPosting();
                        job.setCompany(COMPANY);
                        job.setTitle(title);
                        job.setLocation(location);
                        job.setDate(LocalDate.now().toString());
                        jobs.add(job);
                    } catch (Exception ex) {
                        log.warn("[TikTok] Failed to parse a card, skipping: {}", ex.getMessage());
                    }
                }

                int added = jobs.size() - beforeAdds;
                log.info("[TikTok] page offset={} added={} total={}", offset, added, jobs.size());
                if (added == 0) break;
            }

            log.info("[TikTok] Scraping complete. Total jobs: {}", jobs.size());
        } catch (Exception e) {
            log.error("[TikTok] Error scraping TikTok jobs", e);
        } finally {
            driver.quit();
        }

        return jobs;
    }
}
