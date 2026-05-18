package com.ssafy.ssamentle.error;

import com.ssafy.ssamentle.common.ResponseCode;
import lombok.Getter;

/**
 * 비즈니스 로직에서 발생하는 커스텀 예외 클래스
 *
 * RuntimeException을 상속받아 언체크 예외로 동작
 * → try-catch 없이 예외를 던질 수 있고, GlobalExceptionHandler가 자동으로 처리
 *
 * 사용 예시:
 * throw new GeneralException(ResponseCode.USER_NOT_FOUND);
 * → httpStatus: 404, code: "USER404", message: "존재하지 않는 사용자입니다."
 */
@Getter
public class GeneralException extends RuntimeException {

    // 에러 코드 (httpStatus, code, message를 포함한 enum)
    private final ResponseCode errorCode;

    // 기본 생성자 - 에러코드 없이 던질 때, 자동으로 500 처리
    public GeneralException() {
        super(ResponseCode._INTERNAL_SERVER_ERROR.getMessage());
        this.errorCode = ResponseCode._INTERNAL_SERVER_ERROR;
    }

    // 500 에러 + 커스텀 메시지
    // 예: throw new GeneralException("DB 연결 실패")
    public GeneralException(String message) {
        super(ResponseCode._INTERNAL_SERVER_ERROR.getMessage(message));
        this.errorCode = ResponseCode._INTERNAL_SERVER_ERROR;
    }

    // 500 에러 + 커스텀 메시지 + 원인 예외 (스택트레이스 보존)
    // 예: throw new GeneralException("DB 연결 실패", e)
    public GeneralException(String message, Throwable cause) {
        super(ResponseCode._INTERNAL_SERVER_ERROR.getMessage(message), cause);
        this.errorCode = ResponseCode._INTERNAL_SERVER_ERROR;
    }

    // 500 에러 + 원인 예외만 (메시지는 ResponseCode 기본값 사용)
    // 예: throw new GeneralException(e)
    public GeneralException(Throwable cause) {
        super(ResponseCode._INTERNAL_SERVER_ERROR.getMessage(cause));
        this.errorCode = ResponseCode._INTERNAL_SERVER_ERROR;
    }

    // 특정 에러코드 지정
    // 예: throw new GeneralException(ResponseCode.USER_NOT_FOUND)
    public GeneralException(ResponseCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    // 특정 에러코드 + 커스텀 메시지
    // 예: throw new GeneralException(ResponseCode.USER_NOT_FOUND, "id가 1인 유저")
    public GeneralException(ResponseCode errorCode, String message) {
        super(errorCode.getMessage(message));
        this.errorCode = errorCode;
    }

    // 특정 에러코드 + 커스텀 메시지 + 원인 예외
    // 예: throw new GeneralException(ResponseCode.USER_NOT_FOUND, "id가 1인 유저", e)
    public GeneralException(ResponseCode errorCode, String message, Throwable cause) {
        super(errorCode.getMessage(message), cause);
        this.errorCode = errorCode;
    }

    // 특정 에러코드 + 원인 예외만
    // 예: throw new GeneralException(ResponseCode.USER_NOT_FOUND, e)
    public GeneralException(ResponseCode errorCode, Throwable cause) {
        super(errorCode.getMessage(cause), cause);
        this.errorCode = errorCode;
    }
}
