package project.amall.cart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.amall.cart.dto.CartDto;
import project.amall.product.dto.ProductDto;

/**
 * 장바구니 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {

	private Integer cartId;
	private Integer cartQuantity;
	private String cartRegDate;
	private String memberId;
	private Integer prodNum;

	// 선물 정보
	private String isGift;
	private String giftToMemberId;
	private String giftMessage;

	// 상품 정보
	private ProductInfo product;

	/**
	 * CartDto로부터 CartResponse 생성
	 */
	public static CartResponse from(CartDto cartDto) {
		CartResponseBuilder builder = CartResponse.builder()
				.cartId(cartDto.getCartId())
				.cartQuantity(cartDto.getCartQuantity())
				.cartRegDate(cartDto.getCartRegDate())
				.memberId(cartDto.getMemberId())
				.prodNum(cartDto.getProdNum())
				.isGift(cartDto.getIsGift())
				.giftToMemberId(cartDto.getGiftToMemberId())
				.giftMessage(cartDto.getGiftMessage());

		// 상품 정보가 있으면 포함
		if (cartDto.getProductDto() != null) {
			builder.product(ProductInfo.from(cartDto.getProductDto()));
		}

		return builder.build();
	}

	/**
	 * 상품 정보 (간략)
	 */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ProductInfo {
		private Integer prodNum;
		private String prodName;
		private String prodCode;
		private Integer prodPrice;
		private Integer prodStock;
		private String prodImage1;
		private String sellerId;

		public static ProductInfo from(ProductDto productDto) {
			return ProductInfo.builder()
					.prodNum(productDto.getProdNum())
					.prodName(productDto.getProdName())
					.prodCode(productDto.getProdCode())
					.prodPrice(productDto.getProdPrice())
					.prodStock(productDto.getProdStock())
					.prodImage1(productDto.getProdImage1())
					.sellerId(productDto.getSellerId())
					.build();
		}
	}
}
