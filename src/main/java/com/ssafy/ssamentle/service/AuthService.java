package com.ssafy.ssamentle.service;

import com.ssafy.ssamentle.dto.*;
import com.ssafy.ssamentle.error.ResponseCode;
import com.ssafy.ssamentle.entity.User;
import com.ssafy.ssamentle.error.exception.RedisHandler;
import com.ssafy.ssamentle.error.exception.UserHandler;
import com.ssafy.ssamentle.repository.UserRepository;
import com.ssafy.ssamentle.util.JwtUtil;
import com.ssafy.ssamentle.util.RedisKeyNamingUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

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
        final Boolean isExistUser = userRepository.existsByEmail(email);

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
        final String jti = jwtUtil.generateJTI();
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

        // 클라이언트가 쿠키로 보낸 refreshToken을 hash로 변환.
        final String incomingRtHash = jwtUtil.generateSHA256Token(refreshToken);

        // 1. 세션 조회 (Redis에 동일한 키가 있는지 확인)
        final RedisSessionDto sessionDto = Optional.ofNullable(
                redisTemplate.opsForValue().get(RedisKeyNamingUtil.REFRESH_TOKEN_REDIS_KEY_NAME(familyId))
        ).orElseThrow(() -> new RedisHandler(ResponseCode.SESSION_NOT_FOUND));

        // 2. 현재 RT 검증 (현재 유효한 해시인지 비교)
        // Refresh 되면 rtHash 값이 바뀐다. 즉, 이전 RefreshToken을 사용할 경우, 인증에 실패한다.
        final boolean isCurrentRt = sessionDto.rtHash().equals(incomingRtHash);

        // 3. 이전 RT 검증 (overlap 허용)
        // 재사용 탐지: 새로운 토큰이 생성 되었는데, 이전 토큰이 사용됨
        // 네트워크 문제나 동시 refresh를 할 경우, 정상적인 접근에도 인증에 실패할 수 있다.
        // 조금의 오차를 허용해줘서 사용자 경험을 개선한다.
        boolean isPrevRt = false;
        final long now = Instant.now().getEpochSecond();
        if (!isCurrentRt && sessionDto.prevRtHash() != null && sessionDto.rotatedAtEpoch() != null) {

            long secondsSinceRotation = now - sessionDto.rotatedAtEpoch();

            isPrevRt = sessionDto.prevRtHash().equals(incomingRtHash)
                    && secondsSinceRotation <= jwtUtil.getOVERLAP_WINDOW().toSeconds();
        }

        // 4. 둘 다 아니면 -> 재사용 공격 또는 만료
        if (!isCurrentRt && !isPrevRt) {

            // 재사용 탐지 -> 세션 전체 삭제
            redisTemplate.delete(RedisKeyNamingUtil.REFRESH_TOKEN_REDIS_KEY_NAME(familyId));
            throw new RedisHandler(ResponseCode.SESSION_REUSE_DETECTED);
        }

        // 5. 회전(새로운 RT 발급)
        final String jti = jwtUtil.generateJTI();
        final String newRefreshToken = jwtUtil.generateRefreshToken(); // 새로운 refreshToken 발급
        final String newRtHash = jwtUtil.generateSHA256Token(newRefreshToken); // 새로운 rtHash 발급

        // AccessToken 생성용 User 정보 초기화
        final CustomUserInfoDto customUserInfoDto = CustomUserInfoDto.builder()
                .userId(sessionDto.customUserInfoDto().userId())
                .email(sessionDto.customUserInfoDto().email())
                .nickname(sessionDto.customUserInfoDto().nickname())
                .userRole(sessionDto.customUserInfoDto().userRole())
                .build();

        // Redis에 저장할 세션, RefreshToken 정보
        final RedisSessionDto newRedisSessionDto = RedisSessionDto.builder()
                .customUserInfoDto(customUserInfoDto)
                .rtHash(newRtHash)
                .currentAccessJti(jti)
                // 이전 RT 기록: overlap 요청이었으면 prevRtHash 유지, 정상 rotate면 현재 걸 prev로
                .prevRtHash(isCurrentRt ? sessionDto.rtHash() : sessionDto.prevRtHash())
                .rotatedAtEpoch(isCurrentRt ? now : sessionDto.rotatedAtEpoch()) // prevRt면 갱신 안 함
                .build();

        // Redis에 새로운 세션 + refresh Token 저장
        redisTemplate.opsForValue().set(RedisKeyNamingUtil.REFRESH_TOKEN_REDIS_KEY_NAME(familyId), newRedisSessionDto, jwtUtil.getREFRESH_TTL());

        final String newAccessToken = jwtUtil.createAccessToken(customUserInfoDto, jti, familyId); // 새로운 AccessToken 발급
        return LoginResponseDto.builder()
                .refreshToken(newRefreshToken)
                .accessToken(newAccessToken)
                .userId(sessionDto.customUserInfoDto().userId())
                .familyId(familyId)
                .build();
    }
}
