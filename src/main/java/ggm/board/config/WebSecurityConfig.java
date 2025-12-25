package ggm.board.config;

import ggm.board.domain.auth.entity.UserRole;
import ggm.board.domain.auth.security.JWTFilter;
import ggm.board.domain.auth.security.JWTUtil;
import ggm.board.domain.auth.security.LoginFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class WebSecurityConfig {
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        return request -> {
            CorsConfiguration corsConfiguration = new CorsConfiguration();
            corsConfiguration.setAllowedOrigins(Collections.singletonList("http://localhost:8080"));
            corsConfiguration.setAllowedMethods(Collections.singletonList("*")); // 모든 HTTP 메서드 허용
            corsConfiguration.setAllowCredentials(true); // 인증 정보 포함 허용
            corsConfiguration.setAllowedHeaders(Collections.singletonList("*")); // 모든 헤더 허용
            corsConfiguration.setExposedHeaders(Collections.singletonList("Authorization")); // Authorization 헤더 노출
            corsConfiguration.setMaxAge(3600L); // 1시간 동안 캐싱
            return corsConfiguration;
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        LoginFilter loginFilter = new LoginFilter(authenticationManager(), jwtUtil);
        loginFilter.setFilterProcessesUrl("/auth/login");
        // 로그인 성공 시 리다이렉트 설정
        loginFilter.setAuthenticationSuccessHandler((req, res, authentication) -> {
            String redirectUri = req.getParameter("redirectUri");
            System.out.println(redirectUri);
            if (redirectUri != null && redirectUri.startsWith("/") &&  !redirectUri.startsWith("//")) {
                res.sendRedirect(redirectUri);
                return;
            }

            res.sendRedirect("/board");
        });
        http
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/*")
                        .defaultSuccessUrl("/home", true)
                        .failureUrl("/login?error"))
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .authorizeHttpRequests(authReq ->
                        authReq
                                .requestMatchers("/admin").hasAnyAuthority("ADMIN")
                                .requestMatchers("/auth/login", "/auth/loginPage", "/board/**", "/member/signup/**", "/error").permitAll()
                                .anyRequest().authenticated())
                .addFilterBefore(new JWTFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(loginFilter, JWTFilter.class)
                // logout. delete local cookie(access_token)
                .logout(logout -> logout.logoutUrl("/auth/logout")
                        .deleteCookies("access_token")
                        .logoutSuccessUrl("/board"))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
