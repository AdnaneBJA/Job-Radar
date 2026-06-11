package com.example.job_radar.service;


import com.example.job_radar.model.JobPosting;
import com.example.job_radar.repository.JobRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NotificationService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${notification.email.to}")
    private String to;


    public void sendAllJobsSummary(Set<Long> newJobIds) {
        List<JobPosting> jobs = jobRepository.findAll();

        if (jobs.isEmpty()) {
            log.info("No jobs in database, skipping email.");
            return;
        }

        int newCount = newJobIds.size();
        String body = buildEmailBody(jobs, newJobIds);

        // Subject line includes new count
        String subject = newCount > 0
                ? "Job Radar - " + newCount + " NEW job" + (newCount > 1 ? "s" : "") + " added! (" + jobs.size() + " total)"
                : "Job Radar Summary - " + jobs.size() + " postings tracked";

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            javaMailSender.send(mimeMessage);
            log.info("Summary email sent with {} jobs ({} new)", jobs.size(), newCount);
        } catch (MessagingException e) {
            log.error("Failed to send email", e);
        }
    }

    /**
     * Builds the full HTML email body. Extracted from {@link #sendAllJobsSummary} so the
     * rendered output can be unit-tested without sending mail.
     */
    String buildEmailBody(List<JobPosting> jobs, Set<Long> newJobIds) {
        int newCount = newJobIds.size();

        StringBuilder body = new StringBuilder();
        body.append("<div style=\"font-family: Arial, sans-serif;\">");

        // Header
        body.append("<h1 style=\"margin-bottom: 4px;\">Job Radar - All Tracked Postings (")
                .append(jobs.size()).append(")</h1>");

        // New jobs card (replaces the old banner) — surfaces exactly what's new, at the top.
        if (newCount > 0) {
            appendNewJobsCard(body, jobs, newJobIds);
        } else {
            body.append("<div style=\"background-color: #616161; color: white; padding: 10px 16px; ")
                    .append("border-radius: 6px; margin: 12px 0;\">")
                    .append("No new jobs since last email.</div>");
        }

        // Group by company (full list)
        Map<String, List<JobPosting>> byCompany = jobs.stream()
                .collect(Collectors.groupingBy(JobPosting::getCompany));

        for (Map.Entry<String, List<JobPosting>> entry : byCompany.entrySet().stream()
                .sorted((a, b) -> {
                    boolean aHasNew = a.getValue().stream().anyMatch(j -> newJobIds.contains(j.getId()));
                    boolean bHasNew = b.getValue().stream().anyMatch(j -> newJobIds.contains(j.getId()));
                    if (aHasNew != bHasNew) return aHasNew ? -1 : 1;
                    return a.getKey().compareTo(b.getKey());
                }).toList()) {
            long newInCompany = entry.getValue().stream()
                    .filter(j -> newJobIds.contains(j.getId()))
                    .count();

            body.append("<h2>").append(entry.getKey()).append(" (")
                    .append(entry.getValue().size()).append(" jobs");
            if (newInCompany > 0) {
                body.append(" - <span style=\"color: #2e7d32;\">").append(newInCompany).append(" new</span>");
            }
            body.append(")</h2>");

            for (JobPosting job : entry.getValue()) {
                boolean isNew = newJobIds.contains(job.getId());

                if (isNew) {
                    body.append("<div style=\"border-left: 4px solid #2e7d32; background-color: #e8f5e9; ")
                            .append("padding: 8px 12px; margin: 8px 0; border-radius: 0 4px 4px 0;\">");
                    body.append("<span style=\"background-color: #2e7d32; color: white; padding: 2px 8px; ")
                            .append("border-radius: 3px; font-size: 12px; font-weight: bold;\">NEW</span><br>");
                } else {
                    body.append("<div style=\"padding: 8px 12px; margin: 8px 0; ")
                            .append("border-left: 4px solid #e0e0e0;\">");
                }

                body.append("<b>Title:</b> ").append(job.getTitle()).append("<br>");
                body.append("<b>Date:</b> ").append(job.getDate()).append("<br>");
                body.append("<b>Location:</b> ").append(job.getLocation());
                body.append("</div>");
            }
        }

        body.append("</div>");
        return body.toString();
    }

    /**
     * Renders the green "New Jobs" card: new postings grouped by company, companies ordered by
     * how many new roles they dropped (desc) then alphabetically, so the most active company leads.
     */
    private void appendNewJobsCard(StringBuilder body, List<JobPosting> jobs, Set<Long> newJobIds) {
        List<JobPosting> newJobs = jobs.stream()
                .filter(j -> newJobIds.contains(j.getId()))
                .toList();

        Map<String, List<JobPosting>> newByCompany = newJobs.stream()
                .collect(Collectors.groupingBy(JobPosting::getCompany));

        int newCount = newJobs.size();
        int companyCount = newByCompany.size();

        // Card container
        body.append("<div style=\"background-color: #e8f5e9; border: 2px solid #2e7d32; ")
                .append("border-radius: 8px; padding: 16px; margin: 12px 0;\">");

        // Card header
        body.append("<div style=\"color: #2e7d32; font-size: 18px; font-weight: bold; margin-bottom: 8px;\">")
                .append("&#127881; ").append(newCount).append(" NEW JOB").append(newCount > 1 ? "S" : "")
                .append(" at ").append(companyCount).append(" compan").append(companyCount > 1 ? "ies" : "y")
                .append(" since last email</div>");

        newByCompany.entrySet().stream()
                .sorted((a, b) -> {
                    int cmp = Integer.compare(b.getValue().size(), a.getValue().size());
                    return cmp != 0 ? cmp : a.getKey().compareTo(b.getKey());
                })
                .forEach(entry -> {
                    body.append("<div style=\"margin-top: 10px;\">");
                    body.append("<span style=\"font-weight: bold; color: #1b5e20;\">")
                            .append(entry.getKey()).append("</span> ")
                            .append("<span style=\"color: #2e7d32;\">(")
                            .append(entry.getValue().size()).append(" new)</span>");
                    body.append("<ul style=\"margin: 4px 0 0 0; padding-left: 20px;\">");
                    for (JobPosting job : entry.getValue()) {
                        body.append("<li>").append(job.getTitle());
                        String loc = job.getLocation();
                        if (loc != null && !loc.isBlank()) {
                            body.append(" &mdash; ").append(loc);
                        }
                        body.append("</li>");
                    }
                    body.append("</ul>");
                    body.append("</div>");
                });

        body.append("</div>");
    }
}
