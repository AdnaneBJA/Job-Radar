package com.example.job_radar.service;


import com.example.job_radar.response.AmazonResponse;
import com.example.job_radar.model.AmazonDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
public class JobScrapingService {

    private WebClient webClient;
    private ObjectMapper objectMapper;

    @Autowired
    public JobScrapingService(WebClient webClient, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }


    public List<AmazonDTO> getAllAmazonInternships() {
        AmazonResponse response = webClient.get()
                .uri("https://www.amazon.jobs/en/search.json", uriBuilder -> uriBuilder
                        .queryParam("base_query", "software intern")
                        .queryParam("normalized_country_code[]", "USA")
                        .queryParam("normalized_country_code[]", "CAN")
                        .queryParam("result_limit", "25")
                        .queryParam("offset", "0")
                        .queryParam("sort", "relevant")
                        .build())
                .retrieve()
                .bodyToMono(AmazonResponse.class)
                .block();

        return response.getJobs();
    }
}
