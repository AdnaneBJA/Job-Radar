package com.example.job_radar.scraper.greenhouse;

import org.springframework.stereotype.Component;

@Component
public class VerkadaScraper extends GreenhouseScraper {

    @Override
    protected String boardToken() {
        return "verkada";
    }

    @Override
    protected String company() {
        return "Verkada";
    }
}
