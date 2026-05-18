package com.ssafy.ssamentle.dto;

public record LoginResponseDto(
        Long userId,
        String accessToken,
        String refreshToken
        // TODO: ,String sessionId // familyId
) {
}
