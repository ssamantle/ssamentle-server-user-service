package com.ssafy.ssamentle.dto;

public record SignupRequestDto(

        String nickname,
        String email,
        String password,
        String imageUrl
) {
}
