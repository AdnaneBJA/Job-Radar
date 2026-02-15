package com.example.job_radar.repository;

import com.example.job_radar.model.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends JpaRepository<JobPosting, Integer> {
    boolean existsByTitleAndCompany(String title, String company);
}
