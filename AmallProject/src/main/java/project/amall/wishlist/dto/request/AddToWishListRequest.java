package project.amall.wishlist.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 위시리스트 추가 요청 DTO
 */
@Data
public class AddToWishListRequest {

	@NotNull(message = "상품 번호는 필수입니다.")
	private Integer prodNum;
}
