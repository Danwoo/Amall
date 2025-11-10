package project.amall.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * 전화번호 검증 Validator
 *
 * 한국 전화번호 형식:
 * - 010-1234-5678
 * - 01012345678
 * - 02-1234-5678
 * - 0212345678
 *
 * Phase 2-2: 입력 값 검증 강화
 */
public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {

	// 한국 전화번호 정규식 (하이픈 있거나 없거나 모두 허용)
	private static final Pattern PHONE_PATTERN = Pattern.compile(
			"^(010|011|016|017|018|019|02|031|032|033|041|042|043|044|051|052|053|054|055|061|062|063|064|070)-?\\d{3,4}-?\\d{4}$"
	);

	@Override
	public void initialize(ValidPhoneNumber constraintAnnotation) {
		// 초기화 로직 (필요시)
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		// null 또는 빈 문자열은 @NotBlank에서 처리
		if (value == null || value.trim().isEmpty()) {
			return true;
		}

		return PHONE_PATTERN.matcher(value).matches();
	}
}
