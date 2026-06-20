package com.example.job_radar.scraper.greenhouse;

import org.springframework.stereotype.Component;

@Component
public class RobinHoodScraper extends GreenhouseScraper {

    @Override
    protected String boardToken() {
        return "robinhood";
    }

    @Override
    protected String company() {
        return "RobinHood";
    }
}
