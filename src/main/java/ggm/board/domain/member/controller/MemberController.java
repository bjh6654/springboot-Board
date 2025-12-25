package ggm.board.domain.member.controller;

import ggm.board.domain.auth.service.AuthService;
import ggm.board.domain.member.dto.MemberDTO;
import ggm.board.domain.member.dto.SignUpDTO;
import ggm.board.domain.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.util.Map;

@RequiredArgsConstructor
@Controller
@RequestMapping("/member")
public class MemberController {
    private final AuthService authService;
    private final MemberService memberService;

    @GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("signupDTO", new SignUpDTO());
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@Validated SignUpDTO signupDTO,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            return "signup";
        }

        MemberDTO memberDTO = memberService.signUp(signupDTO);

        redirectAttributes.addFlashAttribute("signupSuccess", true);
        redirectAttributes.addFlashAttribute("memberDTO", memberDTO);
        return "redirect:/member/signup/success";
    }

    @GetMapping("/signup/success")
    public String singupSuccess(HttpServletRequest request,
                                Model model) {
        Map<String, ?> flashMap = RequestContextUtils.getInputFlashMap(request);
        if (flashMap == null || flashMap.get("signupSuccess") == null) {
            return "redirect:/member/signup"; // 직접 접근 방지
        }
        model.addAttribute("memberDTO", flashMap.get("memberDTO"));
        return "signup-success";
    }

    @GetMapping("/delete")
    public String delete(@RequestParam("memberId") Long memberId, Model model) {
        memberService.deleteMemberUserById(memberId);

        model.addAttribute("signupDTO", new SignUpDTO());
        return "signup_complete";
    }
}
