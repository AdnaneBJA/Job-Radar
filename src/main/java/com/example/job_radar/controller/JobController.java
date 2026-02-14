package com.example.job_radar.controller;

import com.example.job_radar.model.AmazonDTO;
import com.example.job_radar.service.JobScrapingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class JobController {
    @Autowired
    JobScrapingService jobScrapingService;

    @GetMapping("/amazon")
    public List<AmazonDTO> getAllAmazonJobs() {
       return jobScrapingService.getAllAmazonInternships();
    }
}
