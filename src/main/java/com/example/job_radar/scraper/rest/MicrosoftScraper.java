package com.example.job_radar.scraper.rest;

import com.example.job_radar.mapper.JobMapper;
import com.example.job_radar.model.JobPosting;
import com.example.job_radar.response.MicrosoftResponse;
import com.example.job_radar.scraper.JobScraper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class MicrosoftScraper implements JobScraper {
    @Autowired
    private WebClient webClient;

    @Autowired
    private JobMapper jobMapper;

    @Override
    public List<JobPosting> scrape() {
        MicrosoftResponse response = webClient.get()
                .uri("https://apply.careers.microsoft.com/api/pcsx/search", uriBuilder -> uriBuilder
                        .queryParam("domain", "microsoft.com")
                        .queryParam("query", "intern")
                        .queryParam("location", "Canada")
                        .queryParam("start", "0")
                        .queryParam("sort_by", "relevance")
                        .queryParam("filter_include_remote", "1")
                        .build())
                .retrieve()
                .bodyToMono(MicrosoftResponse.class)
                .block();

        assert response != null;
        return response.getMicrosoftData().getPositions().stream()
                .map(jobMapper::fromMicrosoft)
                .toList();
    }
}
