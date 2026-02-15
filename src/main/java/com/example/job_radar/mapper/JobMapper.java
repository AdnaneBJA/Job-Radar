package com.example.job_radar.mapper;

import com.example.job_radar.model.AmazonDTO;
import com.example.job_radar.model.JobPosting;
import org.springframework.stereotype.Component;

@Component
public class JobMapper {
    public JobPosting fromAmazon(AmazonDTO dto) {
        JobPosting job = new JobPosting();
        job.setCompany(dto.getCompanyName());
        job.setTitle(dto.getTitle());
        job.setDate(dto.getPostedDate());
        job.setLocation(dto.getLocation());
        return job;
    }
}
