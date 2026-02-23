package com.example.job_radar.scraper.rest;

import com.example.job_radar.mapper.JobMapper;
import com.example.job_radar.model.JobPosting;
import com.example.job_radar.response.NokiaResponse;
import com.example.job_radar.scraper.JobScraper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.util.List;
import java.util.Set;

@Component
public class NokiaScraper implements JobScraper {

    private static final String API_URL = "https://fa-evmr-saasfaprod1.fa.ocs.oraclecloud.com/hcmRestApi/resources/latest/recruitingCEJobRequisitions?onlyData=true&expand=requisitionList.workLocation,requisitionList.otherWorkLocations,requisitionList.secondaryLocations,flexFieldsFacet.values,requisitionList.requisitionFlexFields&finder=findReqs;siteNumber=CX_1,facetsList=LOCATIONS%3BWORK_LOCATIONS%3BWORKPLACE_TYPES%3BTITLES%3BCATEGORIES%3BORGANIZATIONS%3BPOSTING_DATES%3BFLEX_FIELDS,limit=25,lastSelectedFacet=WORKPLACE_TYPES,selectedLocationsFacet=300000000471544,selectedTitlesFacet=TRA,selectedWorkplaceTypesFacet=ORA_HYBRID,sortBy=POSTING_DATES_DESC";
    private static final Set<String> KEYWORDS = Set.of("SOFTWARE", "DEVELOPER", "WEB");


    @Autowired
    private WebClient webClient;

    @Autowired
    private JobMapper jobMapper;

    @Override
    public List<JobPosting> scrape() {
        NokiaResponse response = webClient.get()
                .uri(URI.create(API_URL))
                .retrieve()
                .bodyToMono(NokiaResponse.class)
                .block();

        assert response != null;
        return response.getItemList().stream()
                .flatMap(item -> item.getRequisitionList().stream())
                .filter(item -> {
                    String title = item.getTitle();
                    if (title == null) return false;

                    String normalized = title.toUpperCase();
                    return KEYWORDS.stream().anyMatch(normalized::contains);
                })
                .map(jobMapper::fromNokia)
                .toList();
    }
}
