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


    public void sendAllJobsSummary() {
        List<JobPosting> jobs = jobRepository.findAll();

        if (jobs.isEmpty()) {
            log.info("No jobs in database, skipping email.");
            return;
        }

        StringBuilder body = new StringBuilder();
        body.append("<h1>Job Radar - All Tracked Postings (")
                .append(jobs.size()).append(")</h1>");

        // Group by company
        Map<String, List<JobPosting>> byCompany = jobs.stream()
                .collect(Collectors.groupingBy(JobPosting::getCompany));

        for (Map.Entry<String, List<JobPosting>> entry : byCompany.entrySet()) {
            body.append("<h2>").append(entry.getKey()).append(" (")
                    .append(entry.getValue().size()).append(" jobs)</h2>");

            for (JobPosting job : entry.getValue()) {
                body.append("<p>");
                body.append("<b>Company:</b> ").append(job.getCompany()).append("<br>");
                body.append("<b>Title:</b> ").append(job.getTitle()).append("<br>");
                body.append("<b>Date:</b> ").append(job.getDate()).append("<br>");
                body.append("<b>Location:</b> ").append(job.getLocation());
                body.append("</p>");
            }
        }

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("Job Radar Summary - " + jobs.size() + " postings tracked");
            helper.setText(body.toString(), true);

            javaMailSender.send(mimeMessage);
            log.info("Summary email sent with {} jobs", jobs.size());
        } catch (MessagingException e) {
            log.error("Failed to send email", e);
        }
    }
}
