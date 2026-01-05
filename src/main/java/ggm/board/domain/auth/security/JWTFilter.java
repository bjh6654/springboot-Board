package ggm.board.domain.auth.security;

import com.nimbusds.jwt.proc.ExpiredJWTException;
import ggm.board.domain.auth.entity.Auth;
import ggm.board.domain.auth.entity.CustomUserDetails;
import ggm.board.domain.auth.entity.RefreshToken;
import ggm.board.domain.auth.entity.UserRole;
import ggm.board.domain.auth.repository.RefreshRepository;
import ggm.board.domain.auth.security.jwt.TokenConstants;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;

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
            // 토큰 만료 확인. 만료 시 ExpiredJwtException 발생
            jwtUtil.isTokenExpired(token);
        } catch (ExpiredJwtException e) { // 토큰 만료 예외. refresh_token을 통해 토큰 재발급
            Cookie refreshTokenCookie = Arrays.stream(cookies).filter(c -> "refresh_token".equals(c.getName())).findFirst().orElse(null);
            if (refreshTokenCookie == null || jwtUtil.isTokenExpired(refreshTokenCookie.getValue())) {
                throw new AuthenticationCredentialsNotFoundException("Invalid refresh token");
            }

            String refresh_token = refreshTokenCookie.getValue();
            if  (!refreshRepository.existsByToken(refresh_token)) throw new EntityNotFoundException("Invalid refresh token");

            long userid = jwtUtil.getUserid(refresh_token);
            String username = jwtUtil.getUsername(refresh_token);
            String role = jwtUtil.getRole(refresh_token);

            String new_acces_token = jwtUtil.createJwt(userid, username, role, TokenConstants.ACCESS_TOKEN_EXPIRED_TIME);
            String new_refresh_token = jwtUtil.createJwt(userid, username, role, TokenConstants.REFRESH_TOKEN_EXPIRED_TIME);

            Cookie access_cookie = new Cookie("access_token", new_acces_token);
            access_cookie.setHttpOnly(true);
            access_cookie.setPath("/");
            response.addCookie(access_cookie);

            Cookie refresh_cookie = new Cookie("refresh_token", new_refresh_token);
            refresh_cookie.setHttpOnly(true);
            refresh_cookie.setPath("/");
            response.addCookie(refresh_cookie);

            refreshRepository.save(RefreshToken.builder()
                    .userId(userid)
                    .username(username)
                    .token(new_refresh_token)
                    .expiration(jwtUtil.getExpiration(new_refresh_token))
                    .build()
            );
            refreshRepository.deleteByToken(refresh_token);

            token = new_acces_token;
            System.out.println("Rotate refresh Token\n" + new_acces_token + "\n" + new_refresh_token);
        } catch (Exception e) {
            Cookie c = new Cookie("access_token", null);
            c.setMaxAge(0);
            response.addCookie(c);
            response.sendRedirect("/board");
        } finally {
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
        }

        filterChain.doFilter(request, response);
    }
}