package com.example.job_radar.model;

import lombok.Data;

@Data
public class JobPosting {
    private String company;
    private String title;
    private String city;
    private String jobCategory;
    private String date;
    private String url;
}
