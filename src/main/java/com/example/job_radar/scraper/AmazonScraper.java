package com.example.job_radar.scraper;


import com.example.job_radar.model.AmazonDTO;
import com.example.job_radar.response.AmazonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class AmazonScraper {

    @Autowired
    private WebClient webClient;

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

        assert response != null;
        return response.getJobs();
    }
}
