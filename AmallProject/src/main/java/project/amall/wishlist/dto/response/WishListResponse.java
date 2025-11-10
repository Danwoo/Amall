package project.amall.wishlist.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.amall.product.dto.ProductDto;

/**
 * 위시리스트 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishListResponse {

	private Integer prodNum;
	private String prodName;
	private String prodCode;
	private String prodImage1;
	private Integer prodPrice;
	private Integer prodStock;
	private String sellerId;

	/**
	 * ProductDto로부터 WishListResponse 생성
	 */
	public static WishListResponse from(ProductDto productDto) {
		return WishListResponse.builder()
				.prodNum(productDto.getProdNum())
				.prodName(productDto.getProdName())
				.prodCode(productDto.getProdCode())
				.prodImage1(productDto.getProdImage1())
				.prodPrice(productDto.getProdPrice())
				.prodStock(productDto.getProdStock())
				.sellerId(productDto.getSellerId())
				.build();
	}
}
