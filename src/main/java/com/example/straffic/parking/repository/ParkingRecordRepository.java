package com.example.straffic.parking.repository;

import com.example.straffic.parking.entity.ParkingRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParkingRecordRepository extends JpaRepository<ParkingRecordEntity, Long> {
}

