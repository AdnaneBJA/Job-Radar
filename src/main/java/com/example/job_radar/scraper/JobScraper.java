package com.example.job_radar.scraper;

import com.example.job_radar.model.JobPosting;

import java.util.List;

public interface JobScraper {
    List<JobPosting> scrape();
}
