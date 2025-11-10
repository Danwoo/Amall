package project.amall.cart.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import project.amall.cart.dto.CartDto;
import project.amall.cart.mapper.CartMapper;
import project.amall.common.exception.BusinessException;
import project.amall.common.exception.code.ErrorCode;
import project.amall.product.dto.ProductDto;
import project.amall.product.mapper.ProductMapper;

/**
 * 장바구니 서비스
 *
 * 장바구니 관련 비즈니스 로직 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

	private final CartMapper cartMapper;
	private final ProductMapper productMapper;

	/**
	 * 회원의 장바구니 조회
	 *
	 * @param memberId 회원 ID
	 * @return 장바구니 목록
	 */
	public List<CartDto> showMyCart(String memberId) {
		log.debug("장바구니 조회: memberId={}", memberId);
		return cartMapper.showMyCart(memberId);
	}

	/**
	 * 장바구니에 상품 추가
	 *
	 * 이미 존재하는 상품이면 수량 증가
	 *
	 * @param memberId 회원 ID
	 * @param prodNum 상품 번호
	 * @param quantity 수량
	 */
	@Transactional
	public void addToCart(String memberId, int prodNum, int quantity) {
		log.debug("장바구니 추가: memberId={}, prodNum={}, quantity={}", memberId, prodNum, quantity);

		// (1) 수량 유효성 검증
		if (quantity <= 0) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "수량은 1 이상이어야 합니다.");
		}

		// (2) 상품 존재 여부 확인
		ProductDto product = productMapper.getProductByNum(prodNum);
		if (product == null) {
			throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "prodNum=" + prodNum);
		}

		// (3) 재고 확인
		if (product.getProdStock() < quantity) {
			throw new BusinessException(
					ErrorCode.INVALID_INPUT_VALUE,
					String.format("재고 부족: 요청 수량=%d, 재고=%d", quantity, product.getProdStock())
			);
		}

		// (4) 이미 장바구니에 있는 상품인지 확인
		CartDto existingCart = cartMapper.findByMemberAndProduct(memberId, prodNum);

		if (existingCart != null) {
			// 이미 있으면 수량 증가
			int newQuantity = existingCart.getCartQuantity() + quantity;

			// 재고 재확인
			if (product.getProdStock() < newQuantity) {
				throw new BusinessException(
						ErrorCode.INVALID_INPUT_VALUE,
						String.format("재고 부족: 장바구니 수량=%d, 추가 수량=%d, 재고=%d",
								existingCart.getCartQuantity(), quantity, product.getProdStock())
				);
			}

			updateCartQuantity(existingCart.getCartId(), newQuantity);
			log.info("장바구니 수량 증가: cartId={}, {} → {}", existingCart.getCartId(),
					existingCart.getCartQuantity(), newQuantity);
		} else {
			// 새로 추가
			CartDto cartDto = new CartDto();
			cartDto.setCartId(generateCartId());
			cartDto.setCartQuantity(quantity);
			cartDto.setCartRegDate(getCurrentDateTime());
			cartDto.setMemberId(memberId);
			cartDto.setProdNum(prodNum);

			int result = cartMapper.addToCart(cartDto);
			if (result == 0) {
				throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "장바구니 추가 실패");
			}

			log.info("장바구니 추가 완료: cartId={}, prodNum={}, quantity={}",
					cartDto.getCartId(), prodNum, quantity);
		}
	}

	/**
	 * 장바구니 수량 수정
	 *
	 * @param cartId 장바구니 ID
	 * @param quantity 변경할 수량
	 */
	@Transactional
	public void updateCartQuantity(int cartId, int quantity) {
		log.debug("장바구니 수량 수정: cartId={}, quantity={}", cartId, quantity);

		if (quantity <= 0) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "수량은 1 이상이어야 합니다.");
		}

		int result = cartMapper.updateCartQuantity(cartId, quantity);
		if (result == 0) {
			throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "존재하지 않는 장바구니 항목: cartId=" + cartId);
		}

		log.info("장바구니 수량 수정 완료: cartId={}, quantity={}", cartId, quantity);
	}

	/**
	 * 장바구니에서 상품 삭제
	 *
	 * @param cartId 장바구니 ID
	 */
	@Transactional
	public void removeFromCart(int cartId) {
		log.debug("장바구니 삭제: cartId={}", cartId);

		int result = cartMapper.removeFromCart(cartId);
		if (result == 0) {
			throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "존재하지 않는 장바구니 항목: cartId=" + cartId);
		}

		log.info("장바구니 삭제 완료: cartId={}", cartId);
	}

	/**
	 * 회원의 장바구니 전체 삭제
	 *
	 * @param memberId 회원 ID
	 */
	@Transactional
	public void clearCart(String memberId) {
		log.debug("장바구니 전체 삭제: memberId={}", memberId);

		int result = cartMapper.clearCart(memberId);
		log.info("장바구니 전체 삭제 완료: memberId={}, 삭제 개수={}", memberId, result);
	}

	// ========== Private Helper Methods ==========

	/**
	 * 장바구니 ID 생성
	 *
	 * TODO: 실제로는 시퀀스나 AUTO_INCREMENT 사용 권장
	 */
	private int generateCartId() {
		return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
	}

	/**
	 * 현재 날짜시간 문자열 반환
	 */
	private String getCurrentDateTime() {
		return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
	}
}
