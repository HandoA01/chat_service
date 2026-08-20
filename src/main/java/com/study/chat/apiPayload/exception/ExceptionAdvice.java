package com.study.chat.apiPayload.exception;

import com.study.chat.apiPayload.ApiResponse;
import com.study.chat.apiPayload.code.ErrorReasonDTO;
import com.study.chat.apiPayload.code.status.ErrorStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice(annotations = {RestController.class})
public class ExceptionAdvice extends ResponseEntityExceptionHandler {

    /**
     * @Valid 없이 커스텀 어노테이션만 붙은 경우(@PathVariable 등)에 바로 전달되는 예외.
     * Validator가 ErrorStatus의 enum 이름을 메시지로 심어두므로 valueOf로 복원한다.
     */
    @ExceptionHandler
    public ResponseEntity<Object> validation(ConstraintViolationException e, WebRequest request) {
        String errorMessage = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElse("");

        // 커스텀 어노테이션이 심은 ErrorStatus 이름이면 그 도메인 에러로 응답한다.
        // 표준 어노테이션(@NotBlank 등)은 사람이 읽는 문구를 담고 있어 enum으로 해석되지 않는다.
        // 예전에는 무조건 valueOf를 호출해서, 표준 메시지가 오면 IllegalArgumentException이
        // 핸들러 안에서 터지고 예외가 필터 체인까지 올라가 엉뚱하게 401로 응답됐다.
        Optional<ErrorStatus> domainError = toErrorStatus(errorMessage);
        if (domainError.isPresent()) {
            return handleExceptionInternalConstraint(e, domainError.get(), HttpHeaders.EMPTY, request);
        }

        Map<String, String> errors = new LinkedHashMap<>();
        e.getConstraintViolations().forEach(violation ->
                errors.merge(lastNodeOf(violation), violation.getMessage(),
                        (existing, added) -> existing + ", " + added));

        return handleExceptionInternalArgs(e, HttpHeaders.EMPTY, ErrorStatus._BAD_REQUEST, request, errors);
    }

    /** "search.keyword" 처럼 메서드명이 앞에 붙으므로 마지막 노드(파라미터명)만 남긴다. */
    private String lastNodeOf(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString();
        int lastDot = path.lastIndexOf('.');
        return lastDot < 0 ? path : path.substring(lastDot + 1);
    }

    /**
     * @Valid 검증 실패 시 진입점. 필드별 에러 메시지를 result에 담아 내려준다.
     * Spring Boot 3부터 4번째 파라미터 타입이 HttpStatus -> HttpStatusCode로 바뀌었다.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        Map<String, String> errors = new LinkedHashMap<>();

        e.getBindingResult().getFieldErrors()
                .forEach(fieldError -> {
                    String fieldName = fieldError.getField();
                    String errorMessage = Optional.ofNullable(fieldError.getDefaultMessage()).orElse("");
                    errors.merge(fieldName, errorMessage,
                            (existingErrorMessage, newErrorMessage) -> existingErrorMessage + ", " + newErrorMessage);
                });

        // 커스텀 어노테이션이 심어둔 ErrorStatus 이름이 섞여 있으면 그 도메인 에러로 응답한다.
        // 그대로 두면 enum 이름이 그대로 노출되고 code도 COMMON400으로 뭉개진다.
        Optional<ErrorStatus> domainError = errors.values().stream()
                .map(this::toErrorStatus)
                .flatMap(Optional::stream)
                .findFirst();

        if (domainError.isPresent()) {
            return handleExceptionInternalConstraint(e, domainError.get(), HttpHeaders.EMPTY, request);
        }

        return handleExceptionInternalArgs(e, HttpHeaders.EMPTY, ErrorStatus._BAD_REQUEST, request, errors);
    }

    private Optional<ErrorStatus> toErrorStatus(String message) {
        try {
            return Optional.of(ErrorStatus.valueOf(message));
        } catch (IllegalArgumentException notAnErrorStatusName) {
            return Optional.empty();
        }
    }

    @ExceptionHandler
    public ResponseEntity<Object> exception(Exception e, WebRequest request) {
        log.error("예상하지 못한 예외가 발생했습니다.", e);

        return handleExceptionInternalFalse(e, ErrorStatus._INTERNAL_SERVER_ERROR, HttpHeaders.EMPTY,
                ErrorStatus._INTERNAL_SERVER_ERROR.getHttpStatus(), request, e.getMessage());
    }

    @ExceptionHandler(value = GeneralException.class)
    public ResponseEntity<Object> onThrowException(GeneralException generalException, HttpServletRequest request) {
        ErrorReasonDTO errorReasonHttpStatus = generalException.getErrorReasonHttpStatus();
        return handleExceptionInternal(generalException, errorReasonHttpStatus, null, request);
    }

    private ResponseEntity<Object> handleExceptionInternal(Exception e, ErrorReasonDTO reason,
                                                          HttpHeaders headers, HttpServletRequest request) {

        ApiResponse<Object> body = ApiResponse.onFailure(reason.getCode(), reason.getMessage(), null);

        WebRequest webRequest = new ServletWebRequest(request);
        return super.handleExceptionInternal(
                e,
                body,
                headers,
                reason.getHttpStatus(),
                webRequest
        );
    }

    private ResponseEntity<Object> handleExceptionInternalFalse(Exception e, ErrorStatus errorCommonStatus,
                                                               HttpHeaders headers, HttpStatusCode status,
                                                               WebRequest request, String errorPoint) {
        ApiResponse<Object> body = ApiResponse.onFailure(errorCommonStatus.getCode(), errorCommonStatus.getMessage(), errorPoint);
        return super.handleExceptionInternal(
                e,
                body,
                headers,
                status,
                request
        );
    }

    private ResponseEntity<Object> handleExceptionInternalArgs(Exception e, HttpHeaders headers,
                                                              ErrorStatus errorCommonStatus,
                                                              WebRequest request, Map<String, String> errorArgs) {
        ApiResponse<Object> body = ApiResponse.onFailure(errorCommonStatus.getCode(), errorCommonStatus.getMessage(), errorArgs);
        return super.handleExceptionInternal(
                e,
                body,
                headers,
                errorCommonStatus.getHttpStatus(),
                request
        );
    }

    private ResponseEntity<Object> handleExceptionInternalConstraint(Exception e, ErrorStatus errorCommonStatus,
                                                                    HttpHeaders headers, WebRequest request) {
        ApiResponse<Object> body = ApiResponse.onFailure(errorCommonStatus.getCode(), errorCommonStatus.getMessage(), null);
        return super.handleExceptionInternal(
                e,
                body,
                headers,
                errorCommonStatus.getHttpStatus(),
                request
        );
    }
}
