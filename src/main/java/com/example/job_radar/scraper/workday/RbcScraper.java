package com.example.job_radar.scraper.workday;

import org.springframework.stereotype.Component;

/**
 * RBC's public careers site (jobs.rbc.com, a Phenom front-end) is backed by Workday
 * ({@code rbc.wd3.myworkdayjobs.com}, tenant {@code rbc}, site {@code RBCGLOBAL1}), so we hit
 * Workday directly like the other Workday scrapers.
 *
 * <p>Filters: Canada ({@code Country}) + Student/Coop (Fixed Term) ({@code workerSubType}).
 * Note this can legitimately return 0 between RBC co-op intakes — it auto-populates once
 * Canadian student/co-op roles are posted.
 */
@Component
public class RbcScraper extends WorkdayScraper {

    private static final String BASE_URL =
            "https://rbc.wd3.myworkdayjobs.com/en-CA/RBCGLOBAL1"
                    + "?Country=a30a87ed25634629aa6c3958aa2b91ea"
                    + "&workerSubType=7911130ac48001184e788bd8f87d0001";

    @Override
    protected String getBaseUrl() {
        return BASE_URL;
    }

    @Override
    protected String getCompanyName() {
        return "RBC";
    }
}
