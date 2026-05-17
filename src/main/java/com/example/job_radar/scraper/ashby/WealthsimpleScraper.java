package com.example.job_radar.scraper.ashby;

import org.springframework.stereotype.Component;

@Component
public class WealthsimpleScraper extends AshbyScraper {

    @Override
    protected String company() {
        return "Wealthsimple";
    }

    @Override
    protected String baseUrl() {
        return "https://jobs.ashbyhq.com/wealthsimple?employmentType=Intern";
    }
}
