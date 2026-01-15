package com.example.straffic.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyStatsDTO {

    private List<String> labels;
    private List<Integer> current;
    private List<Integer> previous;
}

