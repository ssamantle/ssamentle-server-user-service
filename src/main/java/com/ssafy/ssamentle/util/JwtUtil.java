package com.ssafy.ssamentle.util;

import com.ssafy.ssamentle.dto.CustomUserInfoDto;
import com.ssafy.ssamentle.error.ResponseCode;
import com.ssafy.ssamentle.error.exception.JwtHandler;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.security.SecureRandom;
import java.sql.Date;
import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
public class JwtUtil {

    private final Key KEY; // secretKey를 HMAC 알고리즘에 맞게 저장. 해시 서명용 비밀키
    private final long ACCESSTOKEN_EXP_TIME; // accessToken 만료 시간
    private final Duration OVERLAP_WINDOW; // 오버랩 허용 시간
    private final Duration REFRESH_TTL; // Refresh Token 만료 시간

    // 무작위 토큰 생성을 위한 실수
    // Random은 예측이 가능하고 시드가 유추되면 다음 값도 예측 가능하기에 암호학적으로 안전한 SecureRandom 사용
    private static final SecureRandom RANDOM = new SecureRandom();

    public JwtUtil(
            @Value("${jwt.secret}") final String SECRET_KEY,
            @Value("${jwt.accesstoken-expiration-time}") final long ACCESSTOKEN_EXP_TIME,
            @Value("${jwt.overlap-window}") final Duration OVERLAP_WINDOW,
            @Value("${jwt.refresh-ttl}") final Duration REFRESH_TTL
    ) {

        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY); // BASE64로 인코딩된 문자열을 바이트로 되돌림

        // JJWT에서 쓰는 코드인데, byte를 기반으로 HMAC 알고리즘에 맞는 Key 객체 생성
        // HMAC은 비밀키를 이용해서 데이터가 위조되지 않았음을 증명하는 위조 방지 서명이다. (해시 방식 암호화가 아니다)
        // 비밀키 + 메시지를 섞고 SHA-256 해시 계산을 하고 또 한 번 감싸서 해시 계산 -> 무결성 + 인증 보장
        this.KEY = Keys.hmacShaKeyFor(keyBytes);
        this.ACCESSTOKEN_EXP_TIME = ACCESSTOKEN_EXP_TIME;
        this.OVERLAP_WINDOW = OVERLAP_WINDOW;
        this.REFRESH_TTL = REFRESH_TTL;
    }

    /**
     * AccessToken 생성
     * @param user JWT 페이로드에 추가할 사용자 기본 정보
     * @return Access Token String
     */
    public String createAccessToken(CustomUserInfoDto user) { return createToken(user, ACCESSTOKEN_EXP_TIME); }

    private String createToken(CustomUserInfoDto user, long expireTime) {

        // 1. JWT Payload를 만드는 코드
        // Claims는 JWT의 payload 영역을 의미. JWT 안에 사용자 정보를 넣는다.
        // 서버가 매번 DB를 조회하지 않고 사용자 정보 및 필요한 정보를 조회 가능
        Claims claims = Jwts.claims();
        claims.put("userId", user.userId());
        claims.put("email", user.email());
        claims.put("nickname", user.nickname());
        claims.put("userRole", user.userRole());
        claims.put("sessionId", user.sessionId());

        Instant now = Instant.now();
        final Instant tokenValidity = now.plusSeconds(expireTime);

        return Jwts.builder()
                .setClaims(claims) // 사용자 정보를 JWT에 넣음
                .setIssuedAt(Date.from(now)) // JWT 발급 시간 (토큰 재사용 방지)
                .setExpiration(Date.from(tokenValidity)) // JWT 만료 시간
                .signWith(KEY, SignatureAlgorithm.HS256) // SHA-256 알고리즘으로 서명, KEY = 서버만 알고 있는 비밀 키
                .compact(); // JWT를 최종 문자열 형태로 생성
    }

    // Token에서 UserId를 추출
    public Long getUserId(String token) {

        return parseClaims(token).get("userId", Long.class);
    }

    /**
     * JWT Claims 추출
     * @param accessToken
     * @return JWT Claims
     */
    public Claims parseClaims(String accessToken) {

        try {
            return Jwts.parserBuilder()
                    .setSigningKey(KEY) // 서명 검증용 비밀키 생성
                    .build()
                    .parseClaimsJws(accessToken) // JWT 파싱 + 서명 검증
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new JwtHandler(ResponseCode.JWT_EXPIRED_TOKEN);
        } catch (MalformedJwtException e) {
            throw new JwtHandler(ResponseCode.JWT_MALFORMED_TOKEN); // 위조된 토큰
        } catch (UnsupportedJwtException e) {
            throw new JwtHandler(ResponseCode.JWT_UNSUPPORTED_TOKEN); // 지원하지 않는 토큰
        } catch (Exception e) {
            throw new JwtHandler(ResponseCode._INTERNAL_SERVER_ERROR);
        }
    }
}
