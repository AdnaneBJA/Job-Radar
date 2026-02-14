package com.example.job_radar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AmazonDTO {

    @JsonProperty("company_name")
    private String companyName;

    @JsonProperty("title")
    private String title;

    @JsonProperty("posted_date")
    private String postedDate;

    @JsonProperty("location")
    private String location;



}
