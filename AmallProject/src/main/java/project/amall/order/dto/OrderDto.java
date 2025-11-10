package project.amall.order.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 주문 DTO
 *
 * Phase 9: Order System Implementation
 */
@Data
public class OrderDto {

	// 기본 정보
	private String orderId;                     // 주문 번호
	private LocalDateTime orderDate;            // 주문 일시
	private String orderStatus;                 // 주문 상태 (PENDING, PAID, SHIPPED, DELIVERED, CANCELLED)
	private String memberId;                    // 주문자 ID

	// 배송 정보
	private String deliveryName;                // 받는 사람 이름
	private String deliveryPhone;               // 받는 사람 전화번호
	private String deliveryPostCode;            // 우편번호
	private String deliveryAddress;             // 주소
	private String deliveryDetailAddress;       // 상세주소
	private String deliveryMessage;             // 배송 메시지

	// 결제 정보
	private Integer totalAmount;                // 총 금액
	private Integer deliveryFee;                // 배송비
	private String paymentMethod;               // 결제 방법

	// 선물 정보
	private String isGift;                      // 선물 여부 ("Y" or "N")
	private String giftFromMemberId;            // 선물 보낸 사람
	private String giftMessage;                 // 선물 메시지

	// 주문 상품 목록 (조인 데이터)
	private List<OrderItemDto> orderItems;

	/**
	 * 선물 여부 확인
	 */
	public boolean isGift() {
		return "Y".equals(this.isGift);
	}

	/**
	 * 일반 주문으로 설정
	 */
	public void setAsNormalOrder() {
		this.isGift = "N";
		this.giftFromMemberId = null;
		this.giftMessage = null;
	}

	/**
	 * 선물 주문으로 설정
	 */
	public void setAsGiftOrder(String fromMemberId, String message) {
		this.isGift = "Y";
		this.giftFromMemberId = fromMemberId;
		this.giftMessage = message;
	}

	/**
	 * 주문 상태 Enum
	 */
	public enum OrderStatus {
		PENDING("PENDING", "결제대기"),
		PAID("PAID", "결제완료"),
		SHIPPED("SHIPPED", "배송중"),
		DELIVERED("DELIVERED", "배송완료"),
		CANCELLED("CANCELLED", "주문취소");

		private final String code;
		private final String description;

		OrderStatus(String code, String description) {
			this.code = code;
			this.description = description;
		}

		public String getCode() {
			return code;
		}

		public String getDescription() {
			return description;
		}
	}
}
