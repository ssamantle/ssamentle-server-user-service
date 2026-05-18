package com.ssafy.ssamentle.service;

import com.ssafy.ssamentle.error.ResponseCode;
import com.ssafy.ssamentle.dto.LoginRequestDto;
import com.ssafy.ssamentle.dto.LoginResponseDto;
import com.ssafy.ssamentle.dto.SignupRequestDto;
import com.ssafy.ssamentle.dto.SignupResponseDto;
import com.ssafy.ssamentle.entity.User;
import com.ssafy.ssamentle.error.exception.UserHandler;
import com.ssafy.ssamentle.repository.UserRepository;
import com.ssafy.ssamentle.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public SignupResponseDto signup(SignupRequestDto signupRequestDto) {

        final String email = signupRequestDto.email();
        final String password = signupRequestDto.password();
        Boolean isExistUser = userRepository.existsByEmail(email);

        if (isExistUser) throw new UserHandler(ResponseCode.USER_ALREADY_EXISTS);

        final String encodedPassword = passwordEncoder.encode(password);

        final User user = User.of(signupRequestDto, encodedPassword);
        final User savedUser = userRepository.save(user);

        return new SignupResponseDto(savedUser.getId(), savedUser.getEmail());
    }

    @Transactional
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {

        final String email = loginRequestDto.email();
        final String password = loginRequestDto.password();

        final User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserHandler(ResponseCode.USER_NOT_FOUND));

        // 비밀번호가 일치하지 않으면 예외 던짐
        if (!passwordEncoder.matches(password, user.getPassword())) throw new UserHandler(ResponseCode.USER_PASSWORD_MISMATCH);

        // 로그인에 성공하면 AccessToken을 생성, RefreshToken을 해시로 변환.
        // RefreshToken을 생성해서 Redis에 저장하고, 클라이언트에는 쿠키로 보내준다.
        // 클라이언트는 RefreshToken을 이용해서 짧은 만료기간을 가진 AccessToken을 재발급 받을 수 있다.
        
        // 1. SessionId(FamilyId), RefreshToken 생성, RefreshToken을 해시로 변환
        
        // 2. Redis에 저장할 User 정보 객체 생성
        
        // 3. Redis에 저장할 User 정보, RefreshToken 해시값, 만료기간 객체 생성
        
        // 4. Redis에 저장
    }
}
