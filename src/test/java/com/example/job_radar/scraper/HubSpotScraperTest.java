package com.example.job_radar.scraper;

import com.example.job_radar.mapper.JobMapper;
import com.example.job_radar.model.HubSpotDTO;
import com.example.job_radar.model.JobPosting;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

class HubSpotScraperTest {

    @Test
    void testScrape() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://app.ripplematch.com/api/v2/public/roles/708"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ObjectMapper mapper = new ObjectMapper();
        List<HubSpotDTO> roles = mapper.readValue(response.body(), new TypeReference<>() {});

        JobMapper jobMapper = new JobMapper();
        List<JobPosting> jobs = roles.stream()
                .map(jobMapper::fromHubSpot)
                .toList();

        System.out.println("=== Scraped " + jobs.size() + " HubSpot jobs ===");
        for (JobPosting job : jobs) {
            System.out.printf("Title: %s | Location: %s | Date: %s%n",
                    job.getTitle(), job.getLocation(), job.getDate());
        }
    }
}
