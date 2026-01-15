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
public class MemberUpdateController {

    private final MemberService memberService;

    @PostMapping("/mypage/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        if (authentication == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인 후 이용해주세요.");
            return "redirect:/login";
        }
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("passwordError", "새 비밀번호가 서로 일치하지 않습니다.");
            return "redirect:/mypage";
        }
        String memberId = authentication.getName();
        try {
            memberService.changePassword(memberId, currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("passwordSuccess", "비밀번호가 변경되었습니다.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("passwordError", e.getMessage());
        }
        return "redirect:/mypage";
    }

    @PostMapping("/mypage/change-tel")
    public String changeTel(@RequestParam("tel") String tel,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        if (authentication == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인 후 이용해주세요.");
            return "redirect:/login";
        }
        String memberId = authentication.getName();
        try {
            memberService.changeTel(memberId, tel);
            redirectAttributes.addFlashAttribute("telSuccess", "전화번호가 변경되었습니다.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("telError", e.getMessage());
        }
        return "redirect:/mypage";
    }
}
