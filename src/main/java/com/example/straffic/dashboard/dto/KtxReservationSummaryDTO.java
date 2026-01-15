package com.example.straffic.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KtxReservationSummaryDTO {

    private String userId;
    private Integer peopleCount;
    private String trainNo;
    private String departure;
    private String arrival;
    private String paidAt;
    private String amount;
    private String seatInfo;
}

