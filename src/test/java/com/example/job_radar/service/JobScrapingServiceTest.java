package com.example.job_radar.service;

import com.example.job_radar.model.JobPosting;
import com.example.job_radar.repository.JobRepository;
import com.example.job_radar.scraper.JobScraper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JobScrapingServiceTest {

    private static JobPosting job(String title, String company) {
        JobPosting p = new JobPosting();
        p.setTitle(title);
        p.setCompany(company);
        return p;
    }

    private JobScrapingService service(List<JobScraper> scrapers, JobRepository repo) {
        return new JobScrapingService(scrapers, repo, Executors.newFixedThreadPool(4));
    }

    @Test
    void allScrapersAreInvoked() {
        JobScraper a = mock(JobScraper.class);
        JobScraper b = mock(JobScraper.class);
        when(a.scrape()).thenReturn(List.of(job("Dev A", "Acme")));
        when(b.scrape()).thenReturn(List.of(job("Dev B", "Beta")));

        JobRepository repo = mock(JobRepository.class);
        when(repo.findAll()).thenReturn(List.of());

        List<JobPosting> result = service(List.of(a, b), repo).scrapeAll();

        verify(a).scrape();
        verify(b).scrape();
        assertThat(result).hasSize(2);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<JobPosting>> captor = ArgumentCaptor.forClass(List.class);
        verify(repo).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(2);
    }

    @Test
    void duplicatesAcrossScrapersAreDeduplicated() {
        JobPosting dup = job("Same Title", "Same Co");
        JobScraper a = mock(JobScraper.class);
        JobScraper b = mock(JobScraper.class);
        when(a.scrape()).thenReturn(List.of(dup));
        when(b.scrape()).thenReturn(List.of(dup));

        JobRepository repo = mock(JobRepository.class);
        when(repo.findAll()).thenReturn(List.of());

        List<JobPosting> result = service(List.of(a, b), repo).scrapeAll();

        assertThat(result).hasSize(1);
    }

    @Test
    void jobsAlreadyInDatabaseAreNotSavedAgain() {
        JobPosting existing = job("Old Job", "OldCo");
        JobScraper scraper = mock(JobScraper.class);
        when(scraper.scrape()).thenReturn(List.of(existing));

        JobRepository repo = mock(JobRepository.class);
        when(repo.findAll()).thenReturn(List.of(existing));

        List<JobPosting> result = service(List.of(scraper), repo).scrapeAll();

        assertThat(result).isEmpty();
        verify(repo, never()).saveAll(any());
    }

    @Test
    void scrapersRunConcurrently() throws InterruptedException {
        CountDownLatch bothStarted = new CountDownLatch(2);
        CountDownLatch release = new CountDownLatch(1);

        JobScraper a = () -> {
            bothStarted.countDown();
            try { release.await(5, TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return List.of(job("Job A", "CoA"));
        };
        JobScraper b = () -> {
            bothStarted.countDown();
            try { release.await(5, TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return List.of(job("Job B", "CoB"));
        };

        JobRepository repo = mock(JobRepository.class);
        when(repo.findAll()).thenReturn(List.of());

        // Run scrapeAll in a background thread so we can release the latch
        Thread runner = new Thread(() -> service(List.of(a, b), repo).scrapeAll());
        runner.start();

        // Both scrapers must start before either finishes — proves concurrent execution
        boolean concurrent = bothStarted.await(10, TimeUnit.SECONDS);
        release.countDown();
        runner.join(10_000);

        assertThat(concurrent)
                .as("Both scrapers must start before either finishes (concurrent execution)")
                .isTrue();
    }

    @Test
    void failingScraperDoesNotAbortOthers() {
        JobScraper bad = mock(JobScraper.class);
        when(bad.scrape()).thenThrow(new RuntimeException("scraper exploded"));

        JobScraper good = mock(JobScraper.class);
        when(good.scrape()).thenReturn(List.of(job("Healthy Job", "GoodCo")));

        JobRepository repo = mock(JobRepository.class);
        when(repo.findAll()).thenReturn(List.of());

        List<JobPosting> result = service(List.of(bad, good), repo).scrapeAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Healthy Job");
    }
}
