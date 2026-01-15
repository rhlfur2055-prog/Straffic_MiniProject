package com.example.straffic.member.serviceImp;

import com.example.straffic.member.dto.MemberCreateDTO;
import com.example.straffic.member.entity.MemberEntity;
import com.example.straffic.member.repository.MemberInfo;
import com.example.straffic.member.repository.MemberRepository;
import com.example.straffic.member.service.MemberService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public void memberinsert(MemberCreateDTO memberDTO, HttpServletResponse response) {
        if (memberRepository.existsById(memberDTO.getId())) {
            throw new IllegalStateException("이미 가입된 아이디입니다");
        }

        MemberEntity me = new MemberEntity();
        me.setId(memberDTO.getId());
        me.setPw(bCryptPasswordEncoder.encode(memberDTO.getPw()));
        me.setName(memberDTO.getName());
        me.setTel(memberDTO.getTel());
        me.setRole("ROLE_USER");
        memberRepository.save(me);
    }

    @Override
    public List<MemberInfo> memberOut() {
        return memberRepository.result();
    }

    @Override
    public List<MemberEntity> entityout() {
        return memberRepository.findAll();
    }

    @Override
    public Page<MemberEntity> entitypage(int page) {
        return memberRepository.findAll(PageRequest.of(page, 3));
    }

    @Override
    public long count() {
        return memberRepository.count();
    }

    @Override
    public void deleteSelf(String memberId, String rawPassword) {
        MemberEntity member = memberRepository.findOneById(memberId);
        if (member == null) {
            throw new IllegalStateException("회원 정보를 찾을 수 없습니다.");
        }
        if (!bCryptPasswordEncoder.matches(rawPassword, member.getPw())) {
            throw new IllegalStateException("비밀번호가 일치하지 않습니다.");
        }
        memberRepository.delete(member);
    }

    @Override
    public void changePassword(String memberId, String currentPassword, String newPassword) {
        MemberEntity member = memberRepository.findOneById(memberId);
        if (member == null) {
            throw new IllegalStateException("회원 정보를 찾을 수 없습니다.");
        }
        if (!bCryptPasswordEncoder.matches(currentPassword, member.getPw())) {
            throw new IllegalStateException("현재 비밀번호가 일치하지 않습니다.");
        }
        member.setPw(bCryptPasswordEncoder.encode(newPassword));
        memberRepository.save(member);
    }

    @Override
    public void changeTel(String memberId, String newTel) {
        MemberEntity member = memberRepository.findOneById(memberId);
        if (member == null) {
            throw new IllegalStateException("회원 정보를 찾을 수 없습니다.");
        }
        member.setTel(newTel);
        memberRepository.save(member);
    }
}
