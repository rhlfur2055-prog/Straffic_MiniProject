package com.example.straffic.mobility.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class KtxHistoryDTO {
    private Long id;
    private String trainNo;
    private String departure;
    private String arrival;
    private String departureTime;
    private String seats;
    private String status;
    private LocalDate travelDate;
}
