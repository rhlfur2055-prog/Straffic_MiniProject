package com.example.straffic.dashboard.repository;

import com.example.straffic.dashboard.entity.PageViewStatsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PageViewStatsRepository extends JpaRepository<PageViewStatsEntity, String> {
}

