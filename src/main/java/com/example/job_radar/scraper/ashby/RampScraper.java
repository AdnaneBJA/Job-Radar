package com.example.job_radar.scraper.ashby;

import org.springframework.stereotype.Component;

@Component
public class RampScraper extends AshbyScraper {

    @Override
    protected String company() {
        return "Ramp";
    }

    @Override
    protected String baseUrl() {
        return "https://jobs.ashbyhq.com/ramp?employmentType=Intern";
    }
}
