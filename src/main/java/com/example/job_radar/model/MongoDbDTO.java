package com.example.job_radar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MongoDbDTO {

    @JsonProperty("title")
    private String title;

    @JsonProperty("location")
    private Location location;

    @JsonProperty("first_published")
    private String firstPublished;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Location {

        @JsonProperty("name")
        private String name;
    }
}
