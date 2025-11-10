package project.amall.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 선물 주문 생성 요청 DTO
 *
 * 선물 주문은 배송지 정보 없이 생성되며,
 * 선물 받는 사람이 나중에 배송지를 입력합니다.
 *
 * Phase 9: Order System Implementation
 */
@Data
public class CreateGiftOrderRequest {

	// 장바구니 ID (선물 상품 1개)
	@NotNull(message = "장바구니 ID는 필수입니다.")
	private Integer cartId;

	// 선물 메시지
	@NotBlank(message = "선물 메시지는 필수입니다.")
	private String giftMessage;

	// 결제 정보
	@NotBlank(message = "결제 방법은 필수입니다.")
	private String paymentMethod;               // 결제 방법
}
