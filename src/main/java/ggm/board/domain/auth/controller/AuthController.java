package ggm.board.domain.auth.controller;

import ggm.board.domain.member.dto.SignUpDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Controller
@RequestMapping("/auth")
public class AuthController {
    @GetMapping("/loginPage")
    public String login(Model model) {
        return "auth/login";
    }

    // Spring Security 에서 처리.
    // FilterChain의 loginFilter.setFilterProcessesUrl("/auth/login"); 으로 처리 됨.
    @PostMapping("/login")
    public String logina(Model model) {
        return "auth/login";
    }

    @GetMapping("/callback/naver")
    public String loginCallbackNaver(Model model) {

        return "auth/login";
    }
}
