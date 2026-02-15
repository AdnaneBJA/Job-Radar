package com.example.job_radar.response;

import com.example.job_radar.data.MicrosoftData;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicrosoftResponse {
    @JsonProperty("data")
    private MicrosoftData microsoftData;
}
