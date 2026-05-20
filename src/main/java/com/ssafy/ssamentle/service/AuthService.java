package com.ssafy.ssamentle.service;

import com.ssafy.ssamentle.dto.*;
import com.ssafy.ssamentle.error.ResponseCode;
import com.ssafy.ssamentle.entity.User;
import com.ssafy.ssamentle.error.exception.UserHandler;
import com.ssafy.ssamentle.repository.UserRepository;
import com.ssafy.ssamentle.util.JwtUtil;
import com.ssafy.ssamentle.util.RedisKeyNamingUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, RedisSessionDto> redisTemplate;

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
        String jti = jwtUtil.generateJTI();
        final String familyId = jwtUtil.generateFamilyId(); // familyId 생성
        final String refreshToken = jwtUtil.generateRefreshToken(); // Refresh Token 생성
        final String rtHash = jwtUtil.generateSHA256Token(refreshToken); // refresh Token을 해시로 변환

        // 2. Redis에 저장할 User 정보 객체 생성
        final CustomUserInfoDto userInfo = CustomUserInfoDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .userRole(user.getUserRole())
                .build();

        // 3. Redis에 저장할 User 정보, RefreshToken 해시값, 만료기간 객체 생성
        final RedisSessionDto newRedisSessionDto = RedisSessionDto.builder()
                .customUserInfoDto(userInfo)
                .rtHash(rtHash)
                .currentAccessJti(jti)
                .prevRtHash(null) // 최초 로그인 시 이전 토큰 없음
                .rotatedAtEpoch(null)
                .build();

        // 4. Redis에 저장
        // Redis에 User 정보와 sessionId, refreshToken, 만료기간을 기록한다.

        redisTemplate.opsForValue().set(RedisKeyNamingUtil.REFRESH_TOKEN_REDIS_KEY_NAME(familyId), newRedisSessionDto, jwtUtil.getREFRESH_TTL());

        // RefreshToken과 familyId, 유저 정보를 기반으로 AccessToken을 만들고 반환.
        return LoginResponseDto.builder()
                .accessToken(jwtUtil.createAccessToken(userInfo, jti, familyId))
                .userId(user.getId())
                .refreshToken(refreshToken)
                .familyId(familyId)
                .build();
    }

    public LoginResponseDto refresh(String refreshToken, String familyId) {

    }
}
