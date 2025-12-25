package ggm.board.domain.auth.security;

import ggm.board.domain.auth.entity.Auth;
import ggm.board.domain.auth.entity.CustomUserDetails;
import ggm.board.domain.auth.entity.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/.well-known/")
                || uri.startsWith("/css/")
                || uri.startsWith("/js/")
                || uri.startsWith("/images/")
                || uri.equals("/favicon.ico");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        // Authorization 헤더에서 JWT 토큰 추출
//        String authorizationHeader = request.getHeader("Authorization");
//
//        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//        // "Bearer " 이후의 토큰 값만 추출
//        String token = authorizationHeader.substring(7);

        // Cookie 에서 JWT 토큰 추출
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Cookie accessTokenCookie = Arrays.stream(cookies).filter(c -> "access_token".equals(c.getName())).findFirst().orElse(null);
        if (accessTokenCookie == null) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = accessTokenCookie.getValue();

        try {
            // JWT 만료 여부 검증
            if (jwtUtil.isTokenExpired(token)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT 토큰이 만료되었습니다.");
                return;
            }

            // JWT에서 사용자 정보 추출
            long userid = jwtUtil.getUserid(token);
            String username = jwtUtil.getUsername(token);
            UserRole role = UserRole.valueOf(jwtUtil.getRole(token));

            // 인증 객체 생성
            Auth auth = Auth.builder()
                    .id(userid)
                    .email(username)
                    .password("N/A")
                    .role(role)
                    .build();

            CustomUserDetails customUserDetails = new CustomUserDetails(auth);
            Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authToken);
//////             SecurityContext에 인증 정보 저장 (STATLESS 모드이므로 요청 종료 시 소멸)
//            if (SecurityContextHolder.getContext().getAuthentication() == null) {
//                SecurityContextHolder.getContext().setAuthentication(authToken);
//            }

        } catch (Exception e) {
            Cookie c = new Cookie("access_token", null);
            c.setMaxAge(0);
            response.addCookie(c);
            response.sendRedirect("/board");
//            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 토큰입니다.");
        }

        filterChain.doFilter(request, response);
    }
}