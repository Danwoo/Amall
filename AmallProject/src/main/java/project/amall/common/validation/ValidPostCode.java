package project.amall.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 우편번호 형식 검증 어노테이션
 *
 * Phase 2-2: 입력 값 검증 강화
 */
@Documented
@Constraint(validatedBy = PostCodeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPostCode {

	String message() default "유효하지 않은 우편번호 형식입니다 (5자리 숫자)";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
