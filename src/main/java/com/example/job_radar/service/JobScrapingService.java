package com.example.job_radar.service;


import com.example.job_radar.mapper.JobMapper;
import com.example.job_radar.model.JobPosting;
import com.example.job_radar.repository.JobRepository;
import com.example.job_radar.response.AmazonResponse;
import com.example.job_radar.model.AmazonDTO;
import com.example.job_radar.scraper.AmazonScraper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Service
public class JobScrapingService {

    private final AmazonScraper amazonScraper;
    private JobRepository jobRepository;
    private JobMapper jobMapper;

    @Autowired
    public JobScrapingService(AmazonScraper amazonScraper, WebClient webClient, JobRepository jobRepository, JobMapper jobMapper) {
        this.amazonScraper = amazonScraper;
        this.jobRepository = jobRepository;
        this.jobMapper = jobMapper;
    }

    public List<JobPosting> scrapeAll() {
        List<JobPosting> newJobs = new ArrayList<>();

        newJobs.addAll(getAllAmazonInternships());
        // newJobs.addAll(scrapeStripe());
        // newJobs.addAll(scrapeGoogle());

        return newJobs;
    }


    public List<JobPosting> getAllAmazonInternships() {
        List<JobPosting> newJobs = new ArrayList<>();
        for (AmazonDTO dto : amazonScraper.getAllAmazonInternships()) {
            JobPosting job = jobMapper.fromAmazon(dto);
            jobRepository.save(job);
            newJobs.add(job);
        }
        return newJobs;
    }
}
