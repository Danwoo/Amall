package project.amall.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 성별 Enum
 *
 * 0/1 숫자 대신 명확한 Enum 사용
 */
@Getter
@RequiredArgsConstructor
public enum Gender {

    MALE(0, "남성"),
    FEMALE(1, "여성");

    private final int code;
    private final String description;

    /**
     * 코드로 Enum 찾기
     */
    public static Gender fromCode(int code) {
        for (Gender gender : values()) {
            if (gender.getCode() == code) {
                return gender;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 성별 코드: " + code);
    }
}
