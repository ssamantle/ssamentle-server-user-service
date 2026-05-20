package com.ssafy.ssamentle.dto;

import com.ssafy.ssamentle.entity.enums.UserRole;
import lombok.Builder;

@Builder
public record CustomUserInfoDto(

        Long userId,
        String email,
        String nickname,
        UserRole userRole,
        String jti
) {
}
