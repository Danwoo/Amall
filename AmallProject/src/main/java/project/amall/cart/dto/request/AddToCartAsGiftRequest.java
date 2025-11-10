package project.amall.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 선물로 장바구니 추가 요청 DTO
 */
@Data
public class AddToCartAsGiftRequest {

	@NotNull(message = "상품 번호는 필수입니다.")
	private Integer prodNum;

	@NotNull(message = "수량은 필수입니다.")
	@Min(value = 1, message = "수량은 1 이상이어야 합니다.")
	private Integer quantity;

	@NotBlank(message = "선물 받는 사람 ID는 필수입니다.")
	private String giftToMemberId;

	@Size(max = 500, message = "선물 메시지는 500자 이하여야 합니다.")
	private String giftMessage;
}
