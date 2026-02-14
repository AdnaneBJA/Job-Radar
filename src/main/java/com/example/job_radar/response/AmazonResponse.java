package com.example.job_radar.response;

import com.example.job_radar.model.AmazonDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AmazonResponse {
    @JsonProperty("jobs")
    private List<AmazonDTO> jobs;
}
