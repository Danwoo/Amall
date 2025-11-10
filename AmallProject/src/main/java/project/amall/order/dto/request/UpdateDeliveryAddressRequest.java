package project.amall.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 배송지 정보 수정 요청 DTO
 *
 * 선물 받는 사람이 배송지를 입력할 때 사용
 *
 * Phase 9: Order System Implementation
 */
@Data
public class UpdateDeliveryAddressRequest {

	@NotBlank(message = "받는 사람 이름은 필수입니다.")
	private String deliveryName;

	@NotBlank(message = "받는 사람 전화번호는 필수입니다.")
	private String deliveryPhone;

	@NotBlank(message = "우편번호는 필수입니다.")
	private String deliveryPostCode;

	@NotBlank(message = "주소는 필수입니다.")
	private String deliveryAddress;

	private String deliveryDetailAddress;       // 상세주소 (선택)
	private String deliveryMessage;             // 배송 메시지 (선택)
}
