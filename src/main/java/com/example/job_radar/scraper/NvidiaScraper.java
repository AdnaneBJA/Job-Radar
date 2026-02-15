package com.example.job_radar.scraper;

import com.example.job_radar.model.JobPosting;
import org.springframework.stereotype.Component;

@Component
public class NvidiaScraper extends WorkdayScraper {

    private static final String BASE_URL =
            "https://nvidia.wd5.myworkdayjobs.com/en-US/NVIDIAExternalCareerSite"
                    + "?workerSubType=0c40f6bd1d8f10adf6dae42e46d44a17"
                    + "&locationHierarchy1=2fcb99c455831013ea52fb338f2932d8"
                    + "&locationHierarchy1=2fcb99c455831013ea529c3b93ba3236";

    @Override
    protected String getBaseUrl() {
        return BASE_URL;
    }

    @Override
    protected String getCompanyName() {
        return "NVIDIA";
    }

    @Override
    protected boolean shouldInclude(JobPosting job) {
        return !job.getTitle().toUpperCase().contains("PHD");
    }
}
