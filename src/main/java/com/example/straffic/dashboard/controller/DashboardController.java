package com.example.straffic.dashboard.controller;

import com.example.straffic.dashboard.dto.DailyViewsDTO;
import com.example.straffic.dashboard.dto.KtxReservationSummaryDTO;
import com.example.straffic.dashboard.dto.WeeklyStatsDTO;
import com.example.straffic.dashboard.dto.YearlyStatsDTO;
import com.example.straffic.mobility.entity.KtxReservationEntity;
import com.example.straffic.mobility.repository.KtxReservationRepository;
import com.example.straffic.board.repository.BoardRepository;
import com.example.straffic.board.repository.CommentRepository;
import com.example.straffic.notice.entity.NoticeEntity;
import com.example.straffic.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final KtxReservationRepository ktxReservationRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final NoticeRepository noticeRepository;

    @GetMapping("/admin/dashboard")
    public String dashboard(@RequestParam(value = "ktxPage", defaultValue = "0") int ktxPage,
                            Model model) {
        DailyViewsDTO dailyViews = new DailyViewsDTO(1234, 567, 89, 345);

        WeeklyStatsDTO weeklyStats = new WeeklyStatsDTO(
                Arrays.asList("월", "화", "수", "목", "금", "토", "일"),
                Arrays.asList(10, 20, 30, 25, 15, 40, 35),
                Arrays.asList(8, 18, 25, 20, 12, 35, 30)
        );

        YearlyStatsDTO yearlyStats = new YearlyStatsDTO(
                Arrays.asList("1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월"),
                Arrays.asList(100, 120, 90, 140, 160, 180, 200, 190, 170, 150, 130, 110),
                Arrays.asList(80, 100, 85, 120, 140, 150, 170, 165, 150, 130, 110, 100)
        );

        Page<KtxReservationEntity> ktxPageResult = loadKtxReservationsPage(ktxPage);
        List<KtxReservationSummaryDTO> ktxReservations = ktxPageResult.getContent()
                .stream()
                .map(this::toSummaryDto)
                .collect(Collectors.toList());

        long totalBoards = boardRepository.count();
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDateTime start = today.atStartOfDay();
        java.time.LocalDateTime end = today.plusDays(1).atStartOfDay();
        long todayBoards = boardRepository.countByCreatedAtBetween(start, end);
        long answeredBoards = commentRepository.countBoardsWithAdminComments("ADMIN");
        long unansweredBoards = Math.max(totalBoards - answeredBoards, 0);
        List<NoticeEntity> recent3Notices = noticeRepository.findTop3ByOrderByCreatedAtDesc();

        model.addAttribute("dailyViews", dailyViews);
        model.addAttribute("weeklyStats", weeklyStats);
        model.addAttribute("yearlyStats", yearlyStats);
        model.addAttribute("ktxReservations", ktxReservations);
        model.addAttribute("ktxPage", ktxPageResult.getNumber());
        model.addAttribute("ktxTotalPages", ktxPageResult.getTotalPages());
        model.addAttribute("boardTodayCount", todayBoards);
        model.addAttribute("boardTotalCount", totalBoards);
        model.addAttribute("boardAnsweredCount", answeredBoards);
        model.addAttribute("boardUnansweredCount", unansweredBoards);
        model.addAttribute("recent3Notices", recent3Notices);

        return "dashboard/dashboard";
    }

    private Page<KtxReservationEntity> loadKtxReservationsPage(int page) {
        if (page < 0) {
            page = 0;
        }
        PageRequest pageable = PageRequest.of(page, 5, Sort.by(Sort.Direction.DESC, "reservedAt"));
        return ktxReservationRepository.findAll(pageable);
    }

    private KtxReservationSummaryDTO toSummaryDto(KtxReservationEntity entity) {
        String paidAt = entity.getReservedAt() != null
                ? entity.getReservedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                : null;

        String amount = entity.getTotalPrice() != null
                ? entity.getTotalPrice().toString()
                : null;

        return new KtxReservationSummaryDTO(
                entity.getMemberId(),
                entity.getPassengerCount(),
                entity.getTrainNo(),
                entity.getDeparture(),
                entity.getArrival(),
                paidAt,
                amount,
                entity.getSeats()
        );
    }
}
