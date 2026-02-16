package com.example.job_radar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MorganStanleyDTO {

    @JsonProperty("jobTitle")
    private String title;

    @JsonProperty("location")
    private String location;
}
