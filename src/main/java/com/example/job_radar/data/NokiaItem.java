package com.example.job_radar.data;

import com.example.job_radar.model.NokiaDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NokiaItem {
    @JsonProperty("requisitionList")
    private List<NokiaDTO> requisitionList;
}
