package project.amall.cart.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 장바구니 아이템을 선물로 설정 요청 DTO
 */
@Data
public class SetGiftRequest {

	@NotBlank(message = "선물 받는 사람 ID는 필수입니다.")
	private String giftToMemberId;

	@Size(max = 500, message = "선물 메시지는 500자 이하여야 합니다.")
	private String giftMessage;
}
