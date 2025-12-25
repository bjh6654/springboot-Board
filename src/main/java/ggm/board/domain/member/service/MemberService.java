package ggm.board.domain.member.service;

import ggm.board.domain.auth.entity.Auth;
import ggm.board.domain.auth.entity.UserRole;
import ggm.board.domain.auth.repository.AuthRepository;
import ggm.board.domain.member.dto.MemberDTO;
import ggm.board.domain.member.dto.SignUpDTO;
import ggm.board.domain.member.entity.Member;
import ggm.board.domain.member.entity.MemberProfile;
import ggm.board.domain.member.repository.MemberProfileRepository;
import ggm.board.domain.member.repository.MemberUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final AuthRepository authRepository;
    private final MemberUserRepository memberUserRepository;
    private final MemberProfileRepository memberProfileRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public Member saveMemberUser(Member mu) {
        return memberUserRepository.save(mu);
    }

    @Transactional
    public MemberDTO signUp(SignUpDTO signupDTO) {


        Member result = memberUserRepository.save(
                Member.builder()
                .name(signupDTO.getName())
                .build()
        );
        memberProfileRepository.save(
                MemberProfile.builder()
                        .email(signupDTO.getEmail())
                        .type("email")
                        .profileMember(result)
                        .build()
        );

        authRepository.save(Auth.builder()
                .authMember(memberUserRepository.getReferenceById(result.getId()))
                .email(signupDTO.getEmail())
                .password(bCryptPasswordEncoder.encode(signupDTO.getPassword()))
                .role(UserRole.USER)
                .build()
        );

        return MemberDTO.builder()
                .id(result.getId())
                .name(result.getName())
                .email(signupDTO.getEmail())
                .build();
    }

    public Member getMemberUserById(long id) {
        return memberUserRepository.findById(id).orElse(null);
    }

    public void deleteMemberUserById(long id) {
        memberUserRepository.deleteById(id);
    }

    public MemberProfile saveMemberAuth(MemberProfile memberProfile) {
        return memberProfileRepository.save(memberProfile);
    }

    public MemberProfile findById(long id) {
        return memberProfileRepository.findById(id).orElse(null);
    }
}
