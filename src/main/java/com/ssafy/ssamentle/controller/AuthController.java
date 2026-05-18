package com.ssafy.ssamentle.controller;

import com.ssafy.ssamentle.dto.LoginRequestDto;
import com.ssafy.ssamentle.dto.LoginResponseDto;
import com.ssafy.ssamentle.dto.SignupRequestDto;
import com.ssafy.ssamentle.dto.SignupResponseDto;
import com.ssafy.ssamentle.service.AuthService;
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

    @PostMapping("signup")
    public ResponseEntity<SignupResponseDto> signup(@RequestBody final SignupRequestDto signupRequestDto) {

        return ResponseEntity.ok(authService.signup(signupRequestDto));
    }

    @PostMapping("login")
    public ResponseEntity<Long> createAuthToken(
            @Valid @RequestBody LoginRequestDto requestDto
            ) {

        LoginResponseDto loginResponse = authService.login(requestDto);


        // RefreshToken은 쿠키
            return ResponseEntity.ok();
    }

    @PostMapping("refresh")
    public ResponseEntity<> createRefreshToken(

    ) {

    }

    // TODO: 이메일 인증 로직 추가
}
