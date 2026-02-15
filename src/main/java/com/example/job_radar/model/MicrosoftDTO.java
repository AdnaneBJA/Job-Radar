package com.example.job_radar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties (ignoreUnknown = true)
public class MicrosoftDTO {

    @JsonProperty("name")
    private String positionName;

    @JsonProperty("locations")
    private String[] location; // Microsoft stores location as array
}
