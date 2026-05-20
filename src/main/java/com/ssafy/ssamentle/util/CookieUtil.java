package com.ssafy.ssamentle.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class CookieUtil {

    private final JwtUtil jwtUtil;

    public void addAuthCookies(HttpServletResponse response,
                                      String refreshToken,
                                      String sessionId) {
        response.addCookie(createHttpOnlyCookie(
                "refreshToken", refreshToken, jwtUtil.getREFRESH_TTL()
        ));
        response.addCookie(createHttpOnlyCookie(
                "sessionId", sessionId, jwtUtil.getREFRESH_TTL()
        ));
    }

    private Cookie createHttpOnlyCookie(String name, String value, Duration maxAge) {

        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(Math.toIntExact(maxAge.getSeconds()));
        // TODO: 보안을 위한 추가 세팅 필요

        return cookie;
    }
}
