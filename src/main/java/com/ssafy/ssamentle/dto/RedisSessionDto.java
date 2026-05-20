package com.ssafy.ssamentle.dto;

import lombok.Builder;

@Builder
public record RedisSessionDto(
        String currentAccessJti,
        CustomUserInfoDto customUserInfoDto,
        String rtHash, // 현재 RT 해시
        String prevRtHash, // 이전 RT 해시
        Long rotatedAtEpoch // 새로 교체된 시각
) {
}
