package ggm.board.domain.auth.security.jwt;

public final class TokenConstants {
    private TokenConstants() {}

    public static final long ACCESS_TOKEN_EXPIRED_TIME = 60 * 60 * 1000L;
    public static final long REFRESH_TOKEN_EXPIRED_TIME = 60 * 60 * 24 * 30 * 1000L;
}