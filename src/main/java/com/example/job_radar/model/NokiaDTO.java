package com.example.job_radar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NokiaDTO {
    @JsonProperty("Title")
    String title;

    @JsonProperty("PostedDate")
    String date;

    @JsonProperty("PrimaryLocation")
    String location;
}
