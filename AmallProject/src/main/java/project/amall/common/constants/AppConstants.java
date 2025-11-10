package project.amall.common.constants;

/**
 * 애플리케이션 전역 상수 정의
 *
 * Phase 6-2: Code Quality Improvement
 */
public final class AppConstants {

	// ========== Cart/Order Constants ==========

	/**
	 * 선물 여부 - 예
	 */
	public static final String IS_GIFT_YES = "Y";

	/**
	 * 선물 여부 - 아니오
	 */
	public static final String IS_GIFT_NO = "N";

	// ========== Order Status ==========

	/**
	 * 주문 상태 - 결제 대기
	 */
	public static final String ORDER_STATUS_PENDING = "PENDING";

	/**
	 * 주문 상태 - 결제 완료
	 */
	public static final String ORDER_STATUS_PAID = "PAID";

	/**
	 * 주문 상태 - 배송 중
	 */
	public static final String ORDER_STATUS_SHIPPED = "SHIPPED";

	/**
	 * 주문 상태 - 배송 완료
	 */
	public static final String ORDER_STATUS_DELIVERED = "DELIVERED";

	/**
	 * 주문 상태 - 취소됨
	 */
	public static final String ORDER_STATUS_CANCELLED = "CANCELLED";

	// ========== Gift Request Status ==========

	/**
	 * 선물 배송지 요청 상태 - 대기 중
	 */
	public static final String GIFT_REQUEST_STATUS_PENDING = "PENDING";

	/**
	 * 선물 배송지 요청 상태 - 완료
	 */
	public static final String GIFT_REQUEST_STATUS_COMPLETED = "COMPLETED";

	// ========== Validation Messages ==========

	/**
	 * 수량 검증 실패 메시지
	 */
	public static final String MSG_INVALID_QUANTITY = "수량은 1 이상이어야 합니다.";

	/**
	 * 재고 부족 메시지
	 */
	public static final String MSG_INSUFFICIENT_STOCK = "재고 부족: 요청 수량 %d, 현재 재고 %d";

	/**
	 * 선물 상품만 가능 메시지
	 */
	public static final String MSG_GIFT_ONLY = "선물 상품만 처리할 수 있습니다.";

	/**
	 * 일반 주문만 가능 메시지
	 */
	public static final String MSG_REGULAR_ORDER_ONLY = "일반 주문만 처리할 수 있습니다.";

	// ========== Quantity Limits ==========

	/**
	 * 최소 주문 수량
	 */
	public static final int MIN_ORDER_QUANTITY = 1;

	/**
	 * 기본 배송비 (3000원)
	 */
	public static final int DEFAULT_DELIVERY_FEE = 3000;

	/**
	 * 무료 배송 최소 금액 (50000원)
	 */
	public static final int FREE_DELIVERY_THRESHOLD = 50000;

	// Private constructor to prevent instantiation
	private AppConstants() {
		throw new AssertionError("Constants class cannot be instantiated");
	}
}
