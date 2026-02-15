package com.example.job_radar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HubSpotDTO {

    @JsonProperty("name")
    private String name;

    @JsonProperty("formatted_locations")
    private String formattedLocations;

    @JsonProperty("job_type")
    private String jobType;
}
