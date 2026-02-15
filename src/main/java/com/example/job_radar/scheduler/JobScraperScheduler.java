package com.example.job_radar.scheduler;

import com.example.job_radar.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class JobScraperScheduler {

    @Autowired
    private NotificationService notificationService;

    @Scheduled(fixedRate = 1800000)
    public void scrapAndNotify() {
        log.info("STARTING SCRAP CYCLE");

        notificationService.sendAllJobsSummary();

        log.info("THE EMAIL WAS SENT WITH ALL JOBS DETAILS");
    }
}

