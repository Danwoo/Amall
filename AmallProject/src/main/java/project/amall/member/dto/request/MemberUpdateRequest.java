package project.amall.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 회원정보 수정 요청 DTO
 */
@Data
public class MemberUpdateRequest {

    @Size(min = 2, max = 50, message = "이름은 2-50자 사이여야 합니다.")
    private String memberName;

    @Size(min = 8, max = 100, message = "비밀번호는 8자 이상이어야 합니다.")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]+$",
        message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다."
    )
    private String memberPwd;

    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String memberEmail;

    @Pattern(regexp = "^\\d{10,11}$", message = "전화번호는 10-11자리 숫자여야 합니다.")
    private String memberPhone;

    private String memberPostCode;
    private String memberAddress;
    private String memberDetailAddress;
    private String memberExtraAddress;
}
