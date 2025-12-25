package ggm.board.domain.auth.security;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JWTUtil {

    private final SecretKey secretKey;

    /**
     * 생성자에서 application.properties에 저장된 SecretKey 값을 가져와 설정
     */
    public JWTUtil(@Value("${spring.jwt.secret}") String secret) {
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    public long getUserid(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseClaimsJws(token)
                .getPayload()
                .get("userid", Long.class);
    }

    /**
     * JWT에서 username 추출
     */
    public String getUsername(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseClaimsJws(token)
                .getPayload()
                .get("username", String.class);
    }

    /**
     * JWT에서 role(권한) 추출
     */
    public String getRole(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseClaimsJws(token)
                .getPayload()
                .get("role", String.class);
    }

    /**
     * JWT 만료 여부 확인
     */
    public Boolean isTokenExpired(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseClaimsJws(token)
                .getPayload()
                .getExpiration()
                .before(new Date());
    }

    /**
     * JWT 생성 메서드
     * - username, role(권한), 만료 시간(expiredMs)을 포함한 JWT 발급
     */
    public String createJwt(long userid, String username, String role, Long expiredMs) {
        return Jwts.builder()
                .claim("userid", userid)
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis())) // 발급 시간
                .expiration(new Date(System.currentTimeMillis() + expiredMs)) // 만료 시간
                .signWith(secretKey) // 비밀키를 사용하여 서명
                .compact();
    }
}