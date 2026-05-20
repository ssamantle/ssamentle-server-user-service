package com.ssafy.ssamentle.dto;

import lombok.Builder;

@Builder
public record AccessTokenResponseDto(
        String accessToken,
        Long userId
) {
}
