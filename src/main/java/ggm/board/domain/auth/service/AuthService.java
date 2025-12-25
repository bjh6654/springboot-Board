package ggm.board.domain.auth.service;

import ggm.board.domain.auth.entity.Auth;
import ggm.board.domain.auth.repository.AuthRepository;
import ggm.board.domain.member.dto.SignUpDTO;
import ggm.board.domain.member.entity.MemberProfile;
import ggm.board.domain.member.entity.Member;
import ggm.board.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthRepository authRepository;
    private final MemberService memberService;

    @Transactional
    public void createAccount(SignUpDTO signupDTO) {
        authRepository.save(Auth.builder()
                .email(signupDTO.getEmail())
                .password(signupDTO.getPassword())
                .build()
        );

        Member mu = Member.builder()
                .name(signupDTO.getEmail())
                .build();

        Member result = memberService.saveMemberUser(mu);
        memberService.saveMemberAuth(
                MemberProfile.builder()
                        .email(result.getName())
                        .type("naver")
                        .profileMember(mu)
                        .build()
        );
    }
}
