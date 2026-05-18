package com.ssafy.ssamentle.dto;

import lombok.Builder;

@Builder
public record SignupResponseDto(
        Long userId,
        String email
) {
}
