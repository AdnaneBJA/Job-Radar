package com.example.job_radar.scraper;

import com.example.job_radar.mapper.JobMapper;
import com.example.job_radar.model.JobPosting;
import com.example.job_radar.response.NokiaResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

class NokiaScraperTest {

    private static final String API_URL = "https://fa-evmr-saasfaprod1.fa.ocs.oraclecloud.com/hcmRestApi/resources/latest/recruitingCEJobRequisitions?onlyData=true&expand=requisitionList.workLocation,requisitionList.otherWorkLocations,requisitionList.secondaryLocations,flexFieldsFacet.values,requisitionList.requisitionFlexFields&finder=findReqs;siteNumber=CX_1,facetsList=LOCATIONS%3BWORK_LOCATIONS%3BWORKPLACE_TYPES%3BTITLES%3BCATEGORIES%3BORGANIZATIONS%3BPOSTING_DATES%3BFLEX_FIELDS,limit=25,lastSelectedFacet=WORKPLACE_TYPES,selectedLocationsFacet=300000000471544,selectedTitlesFacet=TRA,selectedWorkplaceTypesFacet=ORA_HYBRID,sortBy=POSTING_DATES_DESC";

    @Test
    void testScrape() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        NokiaResponse nokiaResponse = mapper.readValue(response.body(), NokiaResponse.class);

        JobMapper jobMapper = new JobMapper();
        List<JobPosting> jobs = nokiaResponse.getItemList().stream()
                .flatMap(item -> item.getRequisitionList().stream())
                .map(jobMapper::fromNokia)
                .toList();

        System.out.println("=== Scraped " + jobs.size() + " Nokia jobs ===");
        for (JobPosting job : jobs) {
            System.out.printf("Title: %s | Location: %s | Company: %s | Date: %s%n",
                    job.getTitle(), job.getLocation(), job.getCompany(), job.getDate());
        }
    }
}
