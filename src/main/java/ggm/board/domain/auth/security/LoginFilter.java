package ggm.board.domain.auth.security;

import ggm.board.domain.auth.entity.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@RequiredArgsConstructor
public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;

    /**
     * 로그인 요청 시 사용자 인증 처리
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res) throws AuthenticationException {
        String username = req.getParameter("email");
        String password = req.getParameter("password");

        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);

        // AuthenticationManager를 통해 인증 수행
        return authenticationManager.authenticate(authRequest);
    }

    /**
     * 로그인 성공 시 JWT 토큰 발급
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain, Authentication auth) throws IOException, ServletException {
        CustomUserDetails customUserDetails = (CustomUserDetails) auth.getPrincipal();
        String username = customUserDetails.getUsername();
        long userid = customUserDetails.getId();

        // 사용자 역할(Role) 조회
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority authority = iterator.next();

        String role = authority.getAuthority();
        String token = jwtUtil.createJwt(userid, username, role, 60 * 60 * 1000L); // 1시간 유효 토큰 생성

        Cookie cookie = new Cookie("access_token", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        res.addCookie(cookie);

        super.successfulAuthentication(req, res, chain, auth);

//        res.addHeader("Authorization", "Bearer " + token); // JWT를 Authorization 헤더에 추가
//        res.sendRedirect("/board");
    }

    /**
     * 로그인 실패 시 401 응답 반환
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest req, HttpServletResponse res, AuthenticationException failed) {
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized 응답
    }
}
