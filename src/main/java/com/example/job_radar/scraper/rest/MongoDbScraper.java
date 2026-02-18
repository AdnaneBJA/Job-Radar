package com.example.job_radar.scraper.rest;

import com.example.job_radar.mapper.JobMapper;
import com.example.job_radar.model.JobPosting;
import com.example.job_radar.response.MongoDbResponse;
import com.example.job_radar.scraper.JobScraper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class MongoDbScraper implements JobScraper {

    @Autowired
    private WebClient webClient;

    @Autowired
    private JobMapper jobMapper;

    @Override
    public List<JobPosting> scrape() {
        MongoDbResponse response = webClient.get()
                .uri("https://boards-api.greenhouse.io/v1/boards/mongodb/jobs")
                .retrieve()
                .bodyToMono(MongoDbResponse.class)
                .block();

        assert response != null;
        return response.getJobs().stream()
                .filter(job -> job.getTitle() != null
                        && job.getTitle().toLowerCase().contains("intern"))
                .filter(job -> job.getLocation() != null
                        && job.getLocation().getName() != null
                        && job.getLocation().getName().toLowerCase().contains("toronto"))
                .map(jobMapper::fromMongoDb)
                .toList();
    }
}
