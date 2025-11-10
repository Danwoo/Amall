package project.amall.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 장바구니 추가 요청 DTO
 */
@Data
public class AddToCartRequest {

	@NotNull(message = "상품 번호는 필수입니다.")
	private Integer prodNum;

	@NotNull(message = "수량은 필수입니다.")
	@Min(value = 1, message = "수량은 1 이상이어야 합니다.")
	private Integer quantity;
}
