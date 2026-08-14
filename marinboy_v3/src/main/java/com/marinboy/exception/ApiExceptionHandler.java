package com.marinboy.exception;

import com.marinboy.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import java.util.NoSuchElementException;

// API 처리 중 발생한 예외를 고객 화면에서 읽을 수 있는 JSON 메시지로 바꾸는 공통 처리 클래스입니다.
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
        // 예약 중복, 휴무일, 노쇼 동의 누락처럼 사용자가 수정 가능한 오류를 400으로 내려줍니다.
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleMaxUploadSize(MaxUploadSizeExceededException exception) {
        // 여러 장을 올릴 때 전체 60MB를 넘으면 관리자에게 원인을 명확히 안내합니다.
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new ApiErrorResponse("이미지는 한 장당 10MB, 전체 60MB 이하로 등록해 주세요."));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(NoSuchElementException exception) {
        // 존재하지 않는 리소스는 입력 오류와 구분해 404로 반환합니다.
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        // Bean Validation 예외도 다른 API 오류와 같은 JSON 형식으로 통일합니다.
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst().map(error -> error.getField() + " 입력값을 확인해 주세요.")
                .orElse("입력값을 확인해 주세요.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiErrorResponse(message));
    }
}
