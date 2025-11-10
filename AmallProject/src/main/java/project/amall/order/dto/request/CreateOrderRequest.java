package project.amall.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import project.amall.common.validation.ValidPhoneNumber;
import project.amall.common.validation.ValidPostCode;

import java.util.List;

/**
 * 일반 주문 생성 요청 DTO
 *
 * Phase 9: Order System Implementation
 */
@Data
public class CreateOrderRequest {

	// 주문 상품 목록 (cartId 리스트)
	@NotEmpty(message = "주문할 상품을 선택해주세요.")
	private List<Integer> cartIds;

	// 배송 정보
	@NotBlank(message = "받는 사람 이름은 필수입니다.")
	private String deliveryName;

	@NotBlank(message = "받는 사람 전화번호는 필수입니다.")
	@ValidPhoneNumber
	private String deliveryPhone;

	@NotBlank(message = "우편번호는 필수입니다.")
	@ValidPostCode
	private String deliveryPostCode;

	@NotBlank(message = "주소는 필수입니다.")
	private String deliveryAddress;

	private String deliveryDetailAddress;       // 상세주소 (선택)
	private String deliveryMessage;             // 배송 메시지 (선택)

	// 결제 정보
	@NotBlank(message = "결제 방법은 필수입니다.")
	private String paymentMethod;               // 결제 방법 (CARD, CASH, etc.)
}
