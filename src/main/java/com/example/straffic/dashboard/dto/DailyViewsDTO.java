package com.example.straffic.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DailyViewsDTO {

    private int parking;
    private int ktx;
    private int bike;
    private int subway;
}

