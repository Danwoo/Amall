package project.amall.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 상품 카테고리 Enum
 *
 * 하드코딩된 "GIFT", "COUPLE" 문자열을 Enum으로 관리
 */
@Getter
@RequiredArgsConstructor
public enum ProductCategory {

    GIFT("GIFT", "선물"),
    COUPLE("COUPLE", "커플");

    private final String code;
    private final String description;

    /**
     * 코드로 Enum 찾기
     */
    public static ProductCategory fromCode(String code) {
        for (ProductCategory category : values()) {
            if (category.getCode().equals(code)) {
                return category;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 상품 카테고리: " + code);
    }
}
