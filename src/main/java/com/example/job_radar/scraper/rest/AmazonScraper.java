package com.example.job_radar.scraper.rest;


import com.example.job_radar.mapper.JobMapper;
import com.example.job_radar.model.JobPosting;
import com.example.job_radar.response.AmazonResponse;
import com.example.job_radar.scraper.JobScraper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class AmazonScraper implements JobScraper {

    @Autowired
    private WebClient webClient;

    @Autowired
    private JobMapper jobMapper;

    private final String COMPANY_NAME = "HubSpot";

    @Override
    public List<JobPosting> scrape() {
        AmazonResponse response = webClient.get()
                .uri("https://www.amazon.jobs/en/search.json", uriBuilder -> uriBuilder
                        .queryParam("base_query", "intern")
                        .queryParam("normalized_country_code[]", "CAN")
                        .queryParam("result_limit", "25")
                        .queryParam("offset", "0")
                        .queryParam("sort", "relevant")
                        .build())
                .retrieve()
                .bodyToMono(AmazonResponse.class)
                .block();

        assert response != null;
        return response.getJobs().stream()
                .map(jobMapper::fromAmazon).filter(job -> !job.getTitle().toUpperCase().contains("PHD"))
                .toList();
    }
}
