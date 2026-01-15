package com.example.straffic.member.controller;

import com.example.straffic.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class MemberDeleteController {
    private final MemberService memberService;

    @PostMapping("/mypage/withdraw")
    public String withdraw(@RequestParam("password") String password,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {
        if (authentication == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인 후 이용해주세요.");
            return "redirect:/login";
        }
        String memberId = authentication.getName();
        try {
            memberService.deleteSelf(memberId, password);
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("withdrawError", e.getMessage());
            return "redirect:/mypage";
        }
        redirectAttributes.addFlashAttribute("logoutMessage", "회원 탈퇴가 완료되었습니다.");
        return "redirect:/logout";
    }
}
