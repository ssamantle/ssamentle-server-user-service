package com.ssafy.ssamentle.controller;

import com.ssafy.ssamentle.dto.*;
import com.ssafy.ssamentle.service.AuthService;
import com.ssafy.ssamentle.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CookieUtil cookieUtil;

    @PostMapping("signup")
    public ResponseEntity<SignupResponseDto> signup(@RequestBody final SignupRequestDto signupRequestDto) {

        return ResponseEntity.ok(authService.signup(signupRequestDto));
    }

    @PostMapping("login")
    public ResponseEntity<AccessTokenResponseDto> createAuthToken(
            @Valid @RequestBody LoginRequestDto requestDto,
            HttpServletResponse response
            ) {

        LoginResponseDto loginResponse = authService.login(requestDto);

        // RefreshToken과 familyId 쿠키를 사용하서 클라이언트로 보내준다.
        // HttpOnly를 사용해서 JS로 세션 정보를 탈취하지 못하도록 한다. XSS 공격 방지
        cookieUtil.addAuthCookies(
                response,
                loginResponse.refreshToken(),
                loginResponse.familyId());

        // AccessToken은 응답으로 보내준다.
        // 클라이언트는 클라이언트의 메모리에 AccessToken을 보내준다.
        // AccessToken은 암호화가 되어 있는 토큰이 아니므로 중요한 개인정보를 저장하며 안된다.
        // 공격을 당할 가능성이 있기에 AccessToken의 만료 시간은 짧게 설정한다.
        AccessTokenResponseDto accessTokenResponseDto = AccessTokenResponseDto.builder()
                .accessToken(loginResponse.accessToken())
                .userId(loginResponse.userId())
                .build();

        return ResponseEntity.ok(accessTokenResponseDto);
    }

    @PostMapping("refresh")
    public ResponseEntity<AccessTokenResponseDto> createRefreshToken(
            @CookieValue("refreshToken") String refreshToken,
            @CookieValue("sessionId") String sessionId,
            HttpServletResponse response
    ) {

        LoginResponseDto refreshResponseDto = authService.refresh(refreshToken, sessionId);
        cookieUtil.addAuthCookies(response,
                refreshResponseDto.refreshToken(),
                refreshResponseDto.familyId());

        AccessTokenResponseDto accessTokenResponseDto = AccessTokenResponseDto.builder()
                .accessToken(refreshResponseDto.accessToken())
                .userId(refreshResponseDto.userId())
                .build();

        return ResponseEntity.ok(accessTokenResponseDto);
    }

    // TODO: 이메일 인증 로직 추가
}
