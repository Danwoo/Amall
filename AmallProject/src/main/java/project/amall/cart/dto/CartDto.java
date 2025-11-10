package project.amall.cart.dto;

import org.springframework.context.annotation.PropertySource;

import lombok.Data;
import project.amall.product.dto.ProductDto;

/**
 * 장바구니 DTO
 *
 * 선물 기능 추가 (Phase 9-3)
 */
@Data
@PropertySource(value = "classpath:application.properties", encoding = "UTF-8")
public class CartDto {

	// 기본 정보
	private int cartId;
	private int cartQuantity;
	private String cartRegDate;
	private String memberId;          // 장바구니 소유자 (구매자)
	private int prodNum;

	// 선물 정보 (Phase 9-3)
	private String isGift;             // 선물 여부 ("Y" or "N")
	private String giftToMemberId;     // 선물 받는 사람 ID (매칭된 상대방)
	private String giftMessage;        // 선물 메시지

	// 조인 데이터
	private ProductDto productDto;     // 상품 정보

	/**
	 * 선물 여부 확인
	 */
	public boolean isGift() {
		return "Y".equals(this.isGift);
	}

	/**
	 * 일반 구매로 설정
	 */
	public void setAsNormalPurchase() {
		this.isGift = "N";
		this.giftToMemberId = null;
		this.giftMessage = null;
	}

	/**
	 * 선물로 설정
	 */
	public void setAsGift(String toMemberId, String message) {
		this.isGift = "Y";
		this.giftToMemberId = toMemberId;
		this.giftMessage = message;
	}
}