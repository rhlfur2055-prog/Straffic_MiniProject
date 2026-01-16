package com.example.straffic.member.controller;

import com.example.straffic.member.entity.MemberEntity;
import com.example.straffic.member.repository.MemberRepository;
import com.example.straffic.mobility.dto.KtxHistoryDTO;
import com.example.straffic.mobility.entity.KtxReservationEntity;
import com.example.straffic.mobility.repository.KtxReservationRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class MyPageController {

    private final KtxReservationRepository reservationRepository;
    private final MemberRepository memberRepository;

    public MyPageController(KtxReservationRepository reservationRepository,
                            MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
    }

    @GetMapping({"/mypage", "/mypage/"})
    public String myPage(Model model, Authentication authentication) {
        String memberId = authentication != null ? authentication.getName() : null;
        List<KtxHistoryDTO> ktxReservations;
        boolean hasProfileImage = false;
        String provider = null;
        String displayMemberId = memberId;
        if (memberId == null) {
            ktxReservations = List.of();
        } else {
            ktxReservations = reservationRepository.findByMemberIdOrderByReservedAtDesc(memberId)
                    .stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
            MemberEntity member = memberRepository.findOneById(memberId);
            hasProfileImage = member != null && member.getProfileImageData() != null;
            if (member != null) {
                provider = member.getProvider();
                if (provider != null) {
                    int idx = memberId.indexOf('_');
                    if (idx != -1 && idx + 1 < memberId.length()) {
                        String prefix = memberId.substring(0, idx + 1);
                        String rest = memberId.substring(idx + 1);
                        if (rest.length() > 3) {
                            rest = rest.substring(0, 3);
                        }
                        displayMemberId = prefix + rest;
                    }
                }
            }
        }
        model.addAttribute("pageTitle", "마이페이지");
        model.addAttribute("memberId", memberId);
        model.addAttribute("displayMemberId", displayMemberId);
        model.addAttribute("ktxReservations", ktxReservations);
        model.addAttribute("hasProfileImage", hasProfileImage);
        model.addAttribute("provider", provider);
        return "member/mypage";
    }

    private KtxHistoryDTO toDto(KtxReservationEntity e) {
        KtxHistoryDTO dto = new KtxHistoryDTO();
        dto.setId(e.getId());
        dto.setTrainNo(e.getTrainNo());
        dto.setDeparture(e.getDeparture());
        dto.setArrival(e.getArrival());
        dto.setDepartureTime(e.getDepartureTime());
        dto.setSeats(e.getSeats());
        dto.setStatus("예약완료");
        dto.setTravelDate(e.getTravelDate());
        return dto;
    }
}
