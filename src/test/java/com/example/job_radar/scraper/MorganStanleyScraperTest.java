package com.example.job_radar.scraper;

import com.example.job_radar.mapper.JobMapper;
import com.example.job_radar.model.JobPosting;
import com.example.job_radar.model.MorganStanleyDTO;
import com.example.job_radar.response.MorganStanleyResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

class MorganStanleyScraperTest {

    @Test
    void testScrape() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.morganstanley.com/web/career_services/webapp/service/careerservice/resultset.json?opportunity=sg&location=Americas_Canada&lang=EN"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        MorganStanleyResponse msResponse = mapper.readValue(response.body(), MorganStanleyResponse.class);

        JobMapper jobMapper = new JobMapper();
        List<JobPosting> jobs = msResponse.getJobs().stream()
                .map(jobMapper::fromMorganStanley)
                .toList();

        System.out.println("=== Scraped " + jobs.size() + " Morgan Stanley jobs ===");
        for (JobPosting job : jobs) {
            System.out.printf("Title: %s | Location: %s | Company: %s | Date: %s%n",
                    job.getTitle(), job.getLocation(), job.getCompany(), job.getDate());
        }
    }
}
