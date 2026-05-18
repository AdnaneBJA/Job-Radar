package com.example.job_radar.service;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.repository.JobRepository;
import com.example.job_radar.scraper.JobScraper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class JobScrapingService {
    private static final Logger log = LoggerFactory.getLogger(JobScrapingService.class);

    // Cap any single scraper at 3 minutes — Selenium hangs (Chrome stalled on a
    // navigation, infinite redirect, etc.) can otherwise block the join() forever.
    private static final long SCRAPER_TIMEOUT_SECONDS = 180;

    private final List<JobScraper> scrapers;
    private final JobRepository jobRepository;
    private final Executor scraperExecutor;

    public JobScrapingService(List<JobScraper> scrapers,
                              JobRepository jobRepository,
                              Executor scraperExecutor) {
        this.scrapers = scrapers;
        this.jobRepository = jobRepository;
        this.scraperExecutor = scraperExecutor;
    }

    public List<JobPosting> scrapeAll() {
        // Layer 1: existing DB keys (pre-thread snapshot)
        Set<String> existingKeys = jobRepository.findAll().stream()
                .map(j -> j.getTitle() + "|" + j.getCompany() + "|" + (j.getLocation() == null ? "" : j.getLocation()))
                .collect(Collectors.toSet());

        // Layer 2: in-run dedup across threads
        Set<String> seenThisRun = ConcurrentHashMap.newKeySet();
        List<JobPosting> collectedJobs = Collections.synchronizedList(new ArrayList<>());

        List<CompletableFuture<Void>> futures = scrapers.stream()
                .map(scraper -> {
                    String name = scraper.getClass().getSimpleName();
                    CompletableFuture<Void> work = CompletableFuture.runAsync(() -> {
                        log.info("[{}] Starting on thread {}", name, Thread.currentThread().getName());
                        try {
                            for (JobPosting job : scraper.scrape()) {
                                String key = job.getTitle() + "|" + job.getCompany() + "|"
                                        + (job.getLocation() == null ? "" : job.getLocation());
                                if (seenThisRun.add(key)) {
                                    collectedJobs.add(job);
                                }
                            }
                            log.info("[{}] Done", name);
                        } catch (Exception e) {
                            log.error("[{}] Failed: {}", name, e.getMessage(), e);
                        }
                    }, scraperExecutor);
                    // Hard timeout so one stuck Selenium driver can't block the join().
                    return work.orTimeout(SCRAPER_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                            .exceptionally(ex -> {
                                log.error("[{}] Timed out or failed after {}s: {}",
                                        name, SCRAPER_TIMEOUT_SECONDS, ex.getMessage());
                                return null;
                            });
                })
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        List<JobPosting> newJobs = collectedJobs.stream()
                .filter(j -> !existingKeys.contains(j.getTitle() + "|" + j.getCompany() + "|" + (j.getLocation() == null ? "" : j.getLocation())))
                .toList();

        if (!newJobs.isEmpty()) {
            jobRepository.saveAll(newJobs);
        }
        return newJobs;
    }
}
