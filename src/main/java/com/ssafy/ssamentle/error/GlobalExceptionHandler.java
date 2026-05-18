package com.ssafy.ssamentle.error;

import com.ssafy.ssamentle.common.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리 클래스
 *
 * @RestControllerAdvice
 * - 모든 @RestController에서 발생하는 예외를 한 곳에서 처리
 * - @ControllerAdvice + @ResponseBody 의 합성 어노테이션
 * - 예외 발생 시 각 컨트롤러마다 try-catch를 작성할 필요 없이
 *   이 클래스에서 중앙 관리
 *
 * 처리 우선순위 (예외 타입 기반으로 매칭, 순차 실행 아님)
 * 1. IllegalArgumentException, IllegalStateException → 400
 * 2. GeneralException (커스텀) → ResponseCode에 정의된 상태코드
 * 3. Exception (나머지 모든 예외) → 500
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // IllegalArgumentException: 잘못된 인자가 전달됐을 때 (예: 음수 나이)
    // IllegalStateException: 객체 상태가 올바르지 않을 때 (예: 이미 시작된 게임 재시작)
    @ExceptionHandler({
            IllegalArgumentException.class,
            IllegalStateException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequestException(Exception e) {

        log.warn("Bad request exception: {}", e.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // 항상 400
                .body(new ErrorResponse(
                        ResponseCode._BAD_REQUEST.getCode(), // "400"
                        e.getMessage()                       // 예외 메시지 그대로
                ));
    }

    // GeneralException: 비즈니스 로직에서 의도적으로 던진 커스텀 예외
    // ResponseCode에 정의된 httpStatus, code, message를 그대로 사용
    @ExceptionHandler({
            GeneralException.class,
    })
    public ResponseEntity<ErrorResponse> handleGeneralException(GeneralException e) {

        log.warn("General exception: {}", e.getMessage());

        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus()) // ResponseCode의 httpStatus 사용
                .body(new ErrorResponse(
                        e.getErrorCode().getCode(),       // "USER404" 같은 식별코드
                        e.getMessage()                    // ResponseCode의 message
                ));
    }

    // 위에서 처리되지 않은 모든 예외의 최후 처리
    // e.getMessage()는 내부 정보가 노출될 수 있으므로 고정 메시지 사용
    @ExceptionHandler({
            Exception.class
    })
    public ResponseEntity<ErrorResponse> handleException(Exception e) {

        // 500 에러는 반드시 로깅 (원인 추적을 위해 스택트레이스 포함)
        log.error("Unhandled exception occurred", e);

        return ResponseEntity.internalServerError() // 항상 500
                .body(new ErrorResponse(
                        ResponseCode._INTERNAL_SERVER_ERROR.getCode(),
                        ResponseCode._INTERNAL_SERVER_ERROR.getMessage() // 고정 메시지로 변경 (보안)
                ));
    }
}
