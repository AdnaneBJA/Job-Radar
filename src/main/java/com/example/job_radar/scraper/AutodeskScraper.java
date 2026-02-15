package com.example.job_radar.scraper;

import org.springframework.stereotype.Component;

@Component
public class AutodeskScraper extends WorkdayScraper {

    private static final String BASE_URL =
            "https://autodesk.wd1.myworkdayjobs.com/fr-CA/Ext"
                    + "?workerSubType=39f5af07b0c54bc588b1a47788da7f81"
                    + "&State_Region=ef8a53af14534128b795fd55d75ec08b"
                    + "&State_Region=218a720b28a74c67b5c6d42c00bdadfa";

    @Override
    protected String getBaseUrl() {
        return BASE_URL;
    }

    @Override
    protected String getCompanyName() {
        return "Autodesk";
    }
}
