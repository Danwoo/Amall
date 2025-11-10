package project.amall.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 전화번호 형식 검증 어노테이션
 *
 * Phase 2-2: 입력 값 검증 강화
 */
@Documented
@Constraint(validatedBy = PhoneNumberValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhoneNumber {

	String message() default "유효하지 않은 전화번호 형식입니다";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
