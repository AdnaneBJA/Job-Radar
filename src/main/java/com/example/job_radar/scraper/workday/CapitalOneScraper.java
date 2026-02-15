package com.example.job_radar.scraper.workday;

import com.example.job_radar.model.JobPosting;

public class CapitalOneScraper extends WorkdayScraper{

    private static final String BASE_URL =
            "https://capitalone.wd12.myworkdayjobs.com/en-US/Capital_One"
                    + "?workerSubType=a12c70bf789e10572aab83c4780919ad";

    @Override
    protected String getBaseUrl() {
        return BASE_URL;
    }

    @Override
    protected String getCompanyName() {
        return "Capital One";
    }

    @Override
    protected boolean shouldInclude(JobPosting job) {
        return !job.getTitle().toUpperCase().contains("PHD");
    }
}
