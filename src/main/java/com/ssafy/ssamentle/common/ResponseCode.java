package com.ssafy.ssamentle.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * API 응답 코드 관리 enum
 *
 * 모든 에러 코드를 한 곳에서 중앙 관리
 * 새로운 에러 추가 시 이 파일에만 추가하면 됨
 *
 * 구성요소:
 * - httpStatus: 실제 HTTP 상태코드 (404, 500 등)
 * - code: 클라이언트가 에러를 식별하는 문자열 ("USER404")
 * - message: 사람이 읽을 수 있는 에러 메시지
 */
@Getter
@RequiredArgsConstructor
public enum ResponseCode {

    // 정상 code
    OK(HttpStatus.OK, "200", "Ok"),

    // Common Error
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "500", "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST, "400", "요청 값을 확인해주세요."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "401", "인증이 필요합니다. 로그인 후 이용해주세요."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "403", "접근 권한이 없습니다."),
    _METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "405", "허용되지 않는 요청 방식입니다."),

    // User Error
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER404", "존재하지 않는 사용자입니다.");

    private final HttpStatus httpStatus;
    private final String code; // 클라이언트 식별용 코드 (예: "USER404"
    private final String message; // 기본 에러 메시지

    /**
     * 예외와 함께 메시지를 반환
     * 기본 메시지에 예외 메시지를 붙여서 반환
     * 예시: "존재하지 않는 사용자입니다. - Could not find user id 1"
     */
    public String getMessage(Throwable e) {

        return this.getMessage(this.message + " - " + e.getMessage());
    }

    /**
     * 메시지가 null이거나 blank면 기본 메시지(this.message)로 fallback
     * 예시: getMessage("") → "존재하지 않는 사용자입니다."
     *       getMessage("커스텀 메시지") → "커스텀 메시지"
     */
    public String getMessage(String message) {

        return Optional.ofNullable(message)
                .filter(Predicate.not(String::isBlank))
                .orElse(this.getMessage());
    }
}
