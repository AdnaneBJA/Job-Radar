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

        StringBuilder body = new StringBuilder();
        body.append("<div style=\"font-family: Arial, sans-serif;\">");

        // Header
        body.append("<h1 style=\"margin-bottom: 4px;\">Job Radar - All Tracked Postings (")
                .append(jobs.size()).append(")</h1>");

        // New jobs banner
        if (newCount > 0) {
            body.append("<div style=\"background-color: #2e7d32; color: white; padding: 12px 16px; ")
                    .append("border-radius: 6px; margin: 12px 0; font-size: 16px; font-weight: bold;\">")
                    .append("&#127381; ").append(newCount).append(" NEW JOB")
                    .append(newCount > 1 ? "S" : "")
                    .append(" ADDED SINCE LAST EMAIL</div>");
        } else {
            body.append("<div style=\"background-color: #616161; color: white; padding: 10px 16px; ")
                    .append("border-radius: 6px; margin: 12px 0;\">")
                    .append("No new jobs since last email.</div>");
        }

        // Group by company
        Map<String, List<JobPosting>> byCompany = jobs.stream()
                .collect(Collectors.groupingBy(JobPosting::getCompany));

        for (Map.Entry<String, List<JobPosting>> entry : byCompany.entrySet()) {
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
            helper.setText(body.toString(), true);

            javaMailSender.send(mimeMessage);
            log.info("Summary email sent with {} jobs ({} new)", jobs.size(), newCount);
        } catch (MessagingException e) {
            log.error("Failed to send email", e);
        }
    }
}
