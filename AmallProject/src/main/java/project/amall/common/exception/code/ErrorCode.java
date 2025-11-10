package project.amall.common.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 에러 코드 정의
 *
 * 모든 비즈니스 예외에 대한 코드와 메시지 관리
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common (1xxx)
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C001", "내부 서버 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C002", "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C003", "허용되지 않은 HTTP 메서드입니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C004", "잘못된 타입의 값입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "C005", "접근 권한이 없습니다."),

    // Member (2xxx)
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "존재하지 않는 회원입니다."),
    MEMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "M002", "이미 존재하는 회원 ID입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "M003", "비밀번호가 일치하지 않습니다."),
    MEMBER_ALREADY_MATCHED(HttpStatus.CONFLICT, "M004", "이미 매칭된 회원입니다."),
    INVALID_MEMBER_HASH(HttpStatus.BAD_REQUEST, "M005", "유효하지 않은 회원 해시 코드입니다."),
    SELF_MATCHING_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "M006", "자기 자신과는 매칭할 수 없습니다."),
    MATCHING_REQUEST_ALREADY_SENT(HttpStatus.CONFLICT, "M007", "이미 매칭 요청을 보냈습니다."),

    // Product (3xxx)
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "존재하지 않는 상품입니다."),
    INVALID_PRODUCT_CATEGORY(HttpStatus.BAD_REQUEST, "P002", "유효하지 않은 상품 카테고리입니다."),

    // Cart (4xxx)
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "CR001", "장바구니를 찾을 수 없습니다."),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "CR002", "장바구니 항목을 찾을 수 없습니다."),

    // WishList (5xxx)
    WISHLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "W001", "위시리스트를 찾을 수 없습니다."),

    // Alarm (6xxx)
    ALARM_NOT_FOUND(HttpStatus.NOT_FOUND, "A001", "알림을 찾을 수 없습니다."),

    // Authentication & Authorization (7xxx)
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AU001", "인증이 필요합니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AU002", "로그인에 실패했습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AU003", "토큰이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AU004", "유효하지 않은 토큰입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
