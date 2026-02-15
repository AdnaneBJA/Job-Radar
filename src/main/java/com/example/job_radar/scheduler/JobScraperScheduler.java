package com.example.job_radar.scheduler;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.service.JobScrapingService;
import com.example.job_radar.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Component
public class JobScraperScheduler {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private JobScrapingService jobScrapingService;

    @Scheduled(fixedRate = 1800000)
    public void scrapAndNotify() {
        log.info("STARTING SCRAP CYCLE");

        List<JobPosting> newJobs = jobScrapingService.scrapeAll();
        Set<Long> newJobIds = newJobs.stream()
                .map(JobPosting::getId)
                .collect(Collectors.toSet());

        log.info("Scraped {} new jobs", newJobIds.size());
        notificationService.sendAllJobsSummary(newJobIds);

        log.info("THE EMAIL WAS SENT WITH ALL JOBS DETAILS");
    }
}

