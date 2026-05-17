package com.example.job_radar.scraper.ashby;

import org.springframework.stereotype.Component;

@Component
public class CohereScraper extends AshbyScraper {

    @Override
    protected String company() {
        return "Cohere";
    }

    @Override
    protected String baseUrl() {
        return "https://jobs.ashbyhq.com/cohere?employmentType=Intern";
    }

    @Override
    protected boolean accept(String title, String location) {
        return isCanada(location);
    }
}
