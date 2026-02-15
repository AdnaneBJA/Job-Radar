package com.example.job_radar;

import com.example.job_radar.model.AmazonDTO;
import com.example.job_radar.model.JobPosting;
import com.example.job_radar.service.JobScrapingService;
import com.example.job_radar.service.NotificationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
import java.util.List;

@SpringBootApplication
@EnableScheduling
public class JobRadarApplication {

	public static void main(String[] args) throws IOException {
		SpringApplication.run(JobRadarApplication.class, args);
	}


	@Bean
	CommandLineRunner run(NotificationService service) {
		return  args -> {
			//service.sendAllJobsSummary();
		};
	}

}
