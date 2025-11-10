package project.amall.member.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import project.amall.common.validation.ValidPhoneNumber;
import project.amall.common.validation.ValidPostCode;

/**
 * 회원가입 요청 DTO
 */
@Data
public class MemberSignUpRequest {

    @NotBlank(message = "아이디는 필수입니다.")
    @Size(min = 4, max = 20, message = "아이디는 4-20자 사이여야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "아이디는 영문, 숫자, _, - 만 사용 가능합니다.")
    private String memberId;

    @NotBlank(message = "이름은 필수입니다.")
    @Size(min = 2, max = 50, message = "이름은 2-50자 사이여야 합니다.")
    private String memberName;

    @NotBlank(message = "생년월일은 필수입니다.")
    @Pattern(regexp = "^\\d{8}$", message = "생년월일은 8자리 숫자여야 합니다. (예: 19900101)")
    private String memberBirth;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 100, message = "비밀번호는 8자 이상이어야 합니다.")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]+$",
        message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다."
    )
    private String memberPwd;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String memberEmail;

    @NotBlank(message = "전화번호는 필수입니다.")
    @ValidPhoneNumber
    private String memberPhone;

    @NotNull(message = "성별은 필수입니다.")
    @Min(value = 0, message = "성별 값이 올바르지 않습니다.")
    @Max(value = 1, message = "성별 값이 올바르지 않습니다.")
    private Integer memberSex;

    @ValidPostCode
    private String memberPostCode;
    private String memberAddress;
    private String memberDetailAddress;
    private String memberExtraAddress;
}
