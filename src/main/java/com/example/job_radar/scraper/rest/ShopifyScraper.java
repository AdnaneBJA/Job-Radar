package com.example.job_radar.scraper.rest;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.scraper.JobScraper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class ShopifyScraper implements JobScraper {

    private static final Logger log = LoggerFactory.getLogger(ShopifyScraper.class);

    private static final String URL = "https://internships.shopify.com/";
    private static final String COMPANY = "Shopify";
    private static final String TRACKED_MESSAGE = "Look out for Fall 2026 applications in April!";

    @Override
    public List<JobPosting> scrape() {
        List<JobPosting> jobs = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(URL)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                            + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36")
                    .timeout(15000)
                    .get();

            String pageText = doc.body().text();

            if (pageText.contains(TRACKED_MESSAGE)) {
                log.info("[Shopify] Tracked message still present — no changes detected");
            } else {
                log.info("[Shopify] Tracked message is GONE — website has changed!");

                JobPosting job = new JobPosting();
                job.setCompany(COMPANY);
                job.setTitle("Their website has changed");
                job.setDate(LocalDate.now().toString());
                job.setLocation("");
                jobs.add(job);
            }
        } catch (Exception e) {
            log.error("[Shopify] Error checking Shopify internships page", e);
        }

        return jobs;
    }
}
