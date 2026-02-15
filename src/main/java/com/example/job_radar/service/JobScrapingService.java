package com.example.job_radar.service;


import com.example.job_radar.model.JobPosting;
import com.example.job_radar.repository.JobRepository;
import com.example.job_radar.scraper.JobScraper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class JobScrapingService {

    private final List<JobScraper> scrapers;
    private final JobRepository jobRepository;

    public JobScrapingService(List<JobScraper> scrapers, JobRepository jobRepository) {
        this.scrapers = scrapers;
        this.jobRepository = jobRepository;
    }

    public List<JobPosting> scrapeAll() {
        List<JobPosting> newJobs = new ArrayList<>();
        for (JobScraper scraper : scrapers) {
            for (JobPosting job : scraper.scrape()) {
                if (!jobRepository.existsByTitleAndCompany(job.getTitle(), job.getCompany())) {
                    newJobs.add(job);
                }
            }
        }
        jobRepository.saveAll(newJobs);
        return newJobs;
    }
}
