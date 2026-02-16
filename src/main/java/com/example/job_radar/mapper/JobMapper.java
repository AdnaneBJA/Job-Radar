package com.example.job_radar.mapper;

import com.example.job_radar.model.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

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

    public JobPosting fromHubSpot(HubSpotDTO dto) {
        JobPosting job = new JobPosting();
        job.setCompany("HubSpot");
        job.setTitle(dto.getName());
        job.setDate(LocalDate.now().toString());
        job.setLocation(dto.getFormattedLocations());
        return job;
    }

    public JobPosting fromMicrosoft(MicrosoftDTO dto) {
        JobPosting job = new JobPosting();
        job.setCompany("Microsoft");
        job.setTitle(dto.getPositionName());
        job.setDate(LocalDate.now().toString());
        job.setLocation(dto.getLocation()[0]);
        return job;
    }

    public JobPosting fromMorganStanley(MorganStanleyDTO dto) {
        JobPosting job = new JobPosting();
        job.setCompany("Morgan Stanley");
        job.setTitle(dto.getTitle());
        job.setDate(LocalDate.now().toString());
        job.setLocation(dto.getLocation());
        return job;
    }
}
