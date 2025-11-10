package project.amall.common.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import project.amall.common.exception.code.ErrorCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 에러 응답 DTO
 *
 * API 에러 발생 시 클라이언트에 전달되는 응답 형식
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ErrorResponse {

    private String code;
    private String message;
    private String detail;
    private LocalDateTime timestamp;
    private List<FieldErrorDetail> errors;

    private ErrorResponse(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.timestamp = LocalDateTime.now();
        this.errors = new ArrayList<>();
    }

    private ErrorResponse(ErrorCode errorCode, String detail) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.detail = detail;
        this.timestamp = LocalDateTime.now();
        this.errors = new ArrayList<>();
    }

    private ErrorResponse(ErrorCode errorCode, List<FieldErrorDetail> errors) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.timestamp = LocalDateTime.now();
        this.errors = errors;
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode);
    }

    public static ErrorResponse of(ErrorCode errorCode, String detail) {
        return new ErrorResponse(errorCode, detail);
    }

    public static ErrorResponse of(ErrorCode errorCode, BindingResult bindingResult) {
        List<FieldErrorDetail> errors = bindingResult.getFieldErrors()
                .stream()
                .map(FieldErrorDetail::of)
                .collect(Collectors.toList());
        return new ErrorResponse(errorCode, errors);
    }

    /**
     * 필드 에러 상세 정보
     */
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class FieldErrorDetail {
        private String field;
        private String value;
        private String reason;

        private FieldErrorDetail(FieldError fieldError) {
            this.field = fieldError.getField();
            this.value = fieldError.getRejectedValue() == null ? "" : fieldError.getRejectedValue().toString();
            this.reason = fieldError.getDefaultMessage();
        }

        public static FieldErrorDetail of(FieldError fieldError) {
            return new FieldErrorDetail(fieldError);
        }
    }
}
