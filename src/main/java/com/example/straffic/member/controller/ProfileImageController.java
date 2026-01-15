package com.example.straffic.member.controller;

import com.example.straffic.member.entity.MemberEntity;
import com.example.straffic.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ProfileImageController {

    private final MemberRepository memberRepository;

    @PostMapping("/mypage/profile-image")
    public String uploadProfileImage(@RequestParam("image") MultipartFile image,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes) {
        if (authentication == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인 후 이용해주세요.");
            return "redirect:/login";
        }
        if (image == null || image.isEmpty()) {
            redirectAttributes.addFlashAttribute("profileError", "이미지를 선택해주세요.");
            return "redirect:/mypage";
        }
        String memberId = authentication.getName();
        MemberEntity member = memberRepository.findOneById(memberId);
        if (member == null) {
            redirectAttributes.addFlashAttribute("profileError", "회원 정보를 찾을 수 없습니다.");
            return "redirect:/mypage";
        }
        try {
            member.setProfileImageContentType(image.getContentType() != null ? image.getContentType() : MediaType.IMAGE_JPEG_VALUE);
            member.setProfileImageData(image.getBytes());
            memberRepository.save(member);
            redirectAttributes.addFlashAttribute("profileSuccess", "프로필 사진이 변경되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("profileError", "이미지 업로드 중 오류가 발생했습니다.");
        }
        return "redirect:/mypage";
    }

    @GetMapping("/member/profile-image")
    public ResponseEntity<byte[]> profileImage(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.notFound().build();
        }
        String memberId = authentication.getName();
        MemberEntity member = memberRepository.findOneById(memberId);
        if (member == null || member.getProfileImageData() == null) {
            return ResponseEntity.notFound().build();
        }
        MediaType mediaType = MediaType.IMAGE_JPEG;
        if (member.getProfileImageContentType() != null) {
            try {
                mediaType = MediaType.parseMediaType(member.getProfileImageContentType());
            } catch (Exception ignored) {}
        }
        return ResponseEntity.ok().contentType(mediaType).body(member.getProfileImageData());
    }
}

