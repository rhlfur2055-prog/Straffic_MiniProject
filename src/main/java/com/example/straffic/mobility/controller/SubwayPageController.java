package com.example.straffic.mobility.controller;

import com.example.straffic.mobility.service.SubwayService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping("/subway")
public class SubwayPageController {

    private final SubwayService subwayService;

    public SubwayPageController(SubwayService subwayService) {
        this.subwayService = subwayService;
    }

    @GetMapping
    public String subwayPage(Model model) {
        model.addAttribute("pageTitle", "지하철 정보");
        return "mobility/subway";
    }

    @GetMapping("/api/arrival")
    @ResponseBody
    public Map<String, Object> getArrival(@RequestParam String stationName) {
        return subwayService.getArrival(stationName);
    }

    @GetMapping("/api/route")
    @ResponseBody
    public Map<String, Object> getRoute(@RequestParam String startStation, @RequestParam String endStation) {
        return subwayService.getRoute(startStation, endStation);
    }
}

