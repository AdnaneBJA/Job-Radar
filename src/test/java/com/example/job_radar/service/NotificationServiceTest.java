package com.example.job_radar.service;

import com.example.job_radar.model.JobPosting;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationServiceTest {

    private JobPosting job(long id, String company, String title, String location) {
        JobPosting j = new JobPosting();
        j.setId(id);
        j.setCompany(company);
        j.setTitle(title);
        j.setLocation(location);
        j.setDate("2026-06-11");
        return j;
    }

    private List<JobPosting> sampleJobs() {
        return List.of(
                job(1, "Apple", "SWE Intern", "Montreal"),
                job(2, "Apple", "ML Engineer", "Remote"),
                job(3, "Stripe", "Backend Engineer", "San Francisco"),
                job(4, "Google", "Older Role", "Mountain View"), // not new
                job(5, "Apple", "Old Apple Role", null)           // not new
        );
    }

    @Test
    void newJobsCardAppearsBeforeFullListAndGroupsByCompany() {
        NotificationService service = new NotificationService();

        String body = service.buildEmailBody(sampleJobs(), Set.of(1L, 2L, 3L));

        // Header reflects 3 new jobs across 2 companies.
        assertTrue(body.contains("3 NEW JOBS at 2 companies"),
                "card header should summarize new count and company count");

        // The card must come before the first full-list company heading (<h2>).
        int cardIdx = body.indexOf("NEW JOBS at");
        int firstListHeading = body.indexOf("<h2>");
        assertTrue(cardIdx >= 0 && firstListHeading >= 0 && cardIdx < firstListHeading,
                "new-jobs card should render before the All Tracked Postings list");

        // Within the card, the most active company (Apple, 2 new) leads Stripe (1 new).
        String card = body.substring(cardIdx, firstListHeading);
        assertTrue(card.indexOf("Apple") < card.indexOf("Stripe"),
                "company with more new jobs should be listed first in the card");

        // All new job titles are present in the card.
        assertTrue(card.contains("SWE Intern"));
        assertTrue(card.contains("ML Engineer"));
        assertTrue(card.contains("Backend Engineer"));

        // Non-new companies are not in the card.
        assertFalse(card.contains("Google"), "non-new companies must not appear in the card");
    }

    @Test
    void noNewJobsShowsGreyBannerAndNoCard() {
        NotificationService service = new NotificationService();

        String body = service.buildEmailBody(sampleJobs(), Set.of());

        assertTrue(body.contains("No new jobs since last email."),
                "grey banner should show when there are no new jobs");
        assertFalse(body.contains("NEW JOB"), "no new-jobs card when nothing is new");
        assertFalse(body.contains("&#127881;"), "no celebration emoji when nothing is new");
    }
}
