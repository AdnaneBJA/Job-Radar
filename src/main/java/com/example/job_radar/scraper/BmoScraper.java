package com.example.job_radar.scraper;

import org.springframework.stereotype.Component;

@Component
public class BmoScraper extends WorkdayScraper {

    private static final String BASE_URL =
            "https://bmo.wd3.myworkdayjobs.com/en-US/External"
                    + "?company=99ba898c88ac011c8a873c01f2016c06"
                    + "&Country=a30a87ed25634629aa6c3958aa2b91ea"
                    + "&jobFamilyGroup=c3170091f3cd01a93a216b6ad000d820"
                    + "&jobFamilyGroup=c3170091f3cd01924064646ad000c620";

    @Override
    protected String getBaseUrl() {
        return BASE_URL;
    }

    @Override
    protected String getCompanyName() {
        return "BMO";
    }
}
