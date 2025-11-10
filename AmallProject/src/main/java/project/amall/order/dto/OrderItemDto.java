package project.amall.order.dto;

import lombok.Data;

/**
 * 주문 상품 DTO
 *
 * Phase 9: Order System Implementation
 */
@Data
public class OrderItemDto {

	// 기본 정보
	private Integer orderItemId;                // 주문 상품 ID
	private String orderId;                     // 주문 번호
	private Integer prodNum;                    // 상품 번호

	// 주문 시점 히스토리 (가격/이름 변경 대비)
	private String prodName;                    // 주문 시점의 상품명
	private Integer prodPrice;                  // 주문 시점의 가격

	// 주문 수량
	private Integer quantity;                   // 주문 수량

	/**
	 * 주문 상품 금액 계산
	 */
	public Integer getTotalPrice() {
		if (prodPrice != null && quantity != null) {
			return prodPrice * quantity;
		}
		return 0;
	}
}
