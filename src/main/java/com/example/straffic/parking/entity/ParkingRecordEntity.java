package com.example.straffic.parking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "parking_record")
@Data
@NoArgsConstructor
public class ParkingRecordEntity {
    @Id
    @SequenceGenerator(name = "parking_record_seq", sequenceName = "PARKING_RECORD_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "parking_record_seq")
    private Long id;

    @Column(nullable = false, length = 20)
    private String parkingSpot;

    @Column(nullable = false, length = 20)
    private String carNumber;

    @Column(nullable = false, length = 20)
    private String carType;

    @Column(nullable = false)
    private LocalDateTime entryTime;

    @Column(nullable = false)
    private LocalDateTime exitTime;

    @Column(nullable = false)
    private long durationMinutes;

    @Column(nullable = false)
    private int fee;
}

