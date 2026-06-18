package com.example.job_radar.scraper.workday;

import com.example.job_radar.model.JobPosting;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkdayScraperTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void cxsEndpointDerivedFromCareersUrl() {
        URI url = URI.create(
                "https://autodesk.wd1.myworkdayjobs.com/fr-CA/Ext?workerSubType=abc");
        assertEquals("https://autodesk.wd1.myworkdayjobs.com/wday/cxs/autodesk/Ext/jobs",
                WorkdayScraper.cxsEndpoint(url));
    }

    @Test
    void facetsJsonGroupsRepeatedKeysIntoArrays() {
        // NVIDIA/BMO repeat keys like locationHierarchy1 / jobFamilyGroup
        String json = WorkdayScraper.facetsJson(
                "workerSubType=A&locationHierarchy1=B&locationHierarchy1=C");
        assertEquals("{\"workerSubType\":[\"A\"],\"locationHierarchy1\":[\"B\",\"C\"]}", json);
    }

    @Test
    void facetsJsonIsEmptyObjectWhenNoQuery() {
        assertEquals("{}", WorkdayScraper.facetsJson(null));
        assertEquals("{}", WorkdayScraper.facetsJson(""));
    }

    @Test
    void mapsWorkdayResponseToJobPostings() throws Exception {
        List<JobPosting> jobs =
                WorkdayScraper.mapPostings(MAPPER.readTree(AUTODESK_JSON), "Autodesk");

        assertEquals(6, jobs.size(), "Canada + Intern facets yield 6 postings");

        // Second posting is fully ASCII — assert it end to end.
        JobPosting second = jobs.get(1);
        assertEquals("Autodesk", second.getCompany());
        assertEquals("Research Intern, Physics-Informed Machine Learning", second.getTitle());
        assertEquals("Toronto, ON, CAN", second.getLocation());
        assertEquals("Posted 7 Days Ago", second.getDate());

        // First posting maps title/location/date from the right JSON fields.
        assertTrue(jobs.get(0).getTitle().startsWith("Stagiaire"));
        assertEquals("Quebec, CAN - Remote", jobs.get(0).getLocation());
        assertEquals("Posted Today", jobs.get(0).getDate());

        // Multi-location postings surface Workday's "N Locations" summary text.
        assertEquals("2 Locations", jobs.get(5).getLocation());
    }

    /** Real response captured from the Autodesk CXS API (Canada + Intern facets), trimmed to mapped fields. */
    private static final String AUTODESK_JSON = """
            {"total":6,"jobPostings":[
            {"title":"Stagiaire en Développement Cloud, Intern Cloud Developer","locationsText":"Quebec, CAN - Remote","postedOn":"Posted Today","bulletFields":["26WD99464"]},
            {"title":"Research Intern, Physics-Informed Machine Learning","locationsText":"Toronto, ON, CAN","postedOn":"Posted 7 Days Ago","bulletFields":["26WD94881"]},
            {"title":"Intern, Machine Learning Developer","locationsText":"Toronto, ON, CAN","postedOn":"Posted 8 Days Ago","bulletFields":["25WD92311"]},
            {"title":"Stagiaire en Développement Cloud, Intern Cloud Developer","locationsText":"Quebec, CAN - Remote","postedOn":"Posted 16 Days Ago","bulletFields":["26WD98608"]},
            {"title":"Intern Software Developer, Stagiaire en Développement Logiciel","locationsText":"Quebec, CAN - Remote","postedOn":"Posted 21 Days Ago","bulletFields":["26WD98603"]},
            {"title":"PhD Intern, HCI & Visualization Research","locationsText":"2 Locations","postedOn":"Posted 30+ Days Ago","bulletFields":["25WD91740"]}
            ]}
            """;
}
