package com.ssafy.ssamentle.dto;

import lombok.Builder;

@Builder
public record LoginResponseDto(
        Long userId,
        String accessToken,
        String refreshToken
        // TODO: ,String sessionId // familyId
) {
}
