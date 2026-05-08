package com.example.job_radar.scraper.workday;

import org.springframework.stereotype.Component;

@Component
public class AutodeskScraper extends WorkdayScraper {

    private static final String BASE_URL =
            "https://autodesk.wd1.myworkdayjobs.com/fr-CA/Ext"
                    + "?workerSubType=39f5af07b0c54bc588b1a47788da7f81"
                    + "&locationCountry=a30a87ed25634629aa6c3958aa2b91ea";

    @Override
    protected String getBaseUrl() {
        return BASE_URL;
    }

    @Override
    protected String getCompanyName() {
        return "Autodesk";
    }
}
