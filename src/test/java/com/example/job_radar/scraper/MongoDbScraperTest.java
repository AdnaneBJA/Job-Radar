package com.example.job_radar.scraper;

import com.example.job_radar.mapper.JobMapper;
import com.example.job_radar.model.JobPosting;
import com.example.job_radar.model.MongoDbDTO;
import com.example.job_radar.response.MongoDbResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

class MongoDbScraperTest {

    @Test
    void testScrape() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://boards-api.greenhouse.io/v1/boards/mongodb/jobs"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ObjectMapper mapper = new ObjectMapper();
        MongoDbResponse mongoResponse = mapper.readValue(response.body(), MongoDbResponse.class);

        JobMapper jobMapper = new JobMapper();
        List<JobPosting> jobs = mongoResponse.getJobs().stream()
                .filter(job -> job.getTitle() != null
                        && job.getTitle().toLowerCase().contains("intern"))
                .filter(job -> job.getLocation() != null
                        && job.getLocation().getName() != null
                        && job.getLocation().getName().toLowerCase().contains("toronto"))
                .map(jobMapper::fromMongoDb)
                .toList();

        System.out.println("=== Scraped " + jobs.size() + " MongoDB jobs (Toronto) ===");
        for (JobPosting job : jobs) {
            System.out.printf("Title: %s | Location: %s | Date: %s%n",
                    job.getTitle(), job.getLocation(), job.getDate());
        }
    }
}
