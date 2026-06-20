package com.example.job_radar.scraper.greenhouse;

import org.springframework.stereotype.Component;

@Component
public class CloudflareScraper extends GreenhouseScraper {

    @Override
    protected String boardToken() {
        return "cloudflare";
    }

    @Override
    protected String company() {
        return "Cloudflare";
    }
}
