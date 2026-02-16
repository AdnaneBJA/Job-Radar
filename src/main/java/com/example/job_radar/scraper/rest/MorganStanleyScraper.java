package com.example.job_radar.scraper.rest;

import com.example.job_radar.mapper.JobMapper;
import com.example.job_radar.model.JobPosting;
import com.example.job_radar.model.MorganStanleyDTO;
import com.example.job_radar.response.MorganStanleyResponse;
import com.example.job_radar.scraper.JobScraper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

public class MorganStanleyScraper implements JobScraper {
    @Autowired
    private WebClient webClient;

    @Autowired
    private JobMapper jobMapper;


    @Override
    public List<JobPosting> scrape() {
        MorganStanleyResponse response = webClient.get()
                .uri("https://www.morganstanley.com/web/career_services/webapp/service/careerservice/resultset.json",
                        uriBuilder -> uriBuilder
                                .queryParam("opportunity", "sg")
                                .queryParam("location", "Americas_Canada")
                                .queryParam("lang", "EN")
                                .build())
                .retrieve()
                .bodyToMono(MorganStanleyResponse.class)
                .block();

        assert response != null;
        return response.getJobs().stream()
                .map(jobMapper::fromMorganStanley)
                .toList();    }
}
