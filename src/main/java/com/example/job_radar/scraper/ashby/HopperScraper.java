package com.example.job_radar.scraper.ashby;

import org.springframework.stereotype.Component;

@Component
public class HopperScraper extends AshbyScraper {

    @Override
    protected String company() {
        return "Hopper";
    }

    @Override
    protected String baseUrl() {
        return "https://jobs.ashbyhq.com/hopper?locationId=93f7a33d-6dec-4594-b7a4-9f1a299dbb7f";
    }
}
