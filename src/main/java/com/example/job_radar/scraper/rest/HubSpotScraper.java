package com.example.job_radar.scraper.rest;

import com.example.job_radar.mapper.JobMapper;
import com.example.job_radar.model.HubSpotDTO;
import com.example.job_radar.model.JobPosting;
import com.example.job_radar.scraper.JobScraper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class HubSpotScraper implements JobScraper {

    @Autowired
    private WebClient webClient;

    @Autowired
    private JobMapper jobMapper;

    @Override
    public List<JobPosting> scrape() {
        List<HubSpotDTO> roles = webClient.get()
                .uri("https://app.ripplematch.com/api/v2/public/roles/708")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<HubSpotDTO>>() {})
                .block();

        assert roles != null;
        return roles.stream()
                .map(jobMapper::fromHubSpot)
                .toList();
    }
}
