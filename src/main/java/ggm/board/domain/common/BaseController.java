package ggm.board.domain.common;

import ggm.board.domain.member.dto.SignUpDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Controller
@RequestMapping("/")
public class BaseController {
    @GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("signupDTO", new SignUpDTO());
        return "signup";
    }
}
