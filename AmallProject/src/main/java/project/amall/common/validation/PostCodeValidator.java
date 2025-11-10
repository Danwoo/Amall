package project.amall.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * 우편번호 검증 Validator
 *
 * 한국 우편번호 형식: 5자리 숫자 (예: 12345)
 *
 * Phase 2-2: 입력 값 검증 강화
 */
public class PostCodeValidator implements ConstraintValidator<ValidPostCode, String> {

	// 5자리 숫자
	private static final Pattern POSTCODE_PATTERN = Pattern.compile("^\\d{5}$");

	@Override
	public void initialize(ValidPostCode constraintAnnotation) {
		// 초기화 로직 (필요시)
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		// null 또는 빈 문자열은 @NotBlank에서 처리
		if (value == null || value.trim().isEmpty()) {
			return true;
		}

		return POSTCODE_PATTERN.matcher(value).matches();
	}
}
