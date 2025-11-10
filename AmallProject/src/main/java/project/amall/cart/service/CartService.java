package project.amall.cart.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import project.amall.cart.dto.CartDto;
import project.amall.cart.mapper.CartMapper;
import project.amall.common.constants.AppConstants;
import project.amall.common.exception.BusinessException;
import project.amall.common.exception.code.ErrorCode;
import project.amall.common.util.DateTimeUtils;
import project.amall.common.util.IdGenerator;
import project.amall.member.dto.MemberDto;
import project.amall.member.mapper.MemberMapper;
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
	private final MemberMapper memberMapper;

	/**
	 * 회원의 장바구니 조회
	 *
	 * @param memberId 회원 ID
	 * @return 장바구니 목록
	 */
	public List<CartDto> getCart(String memberId) {
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
	 * @throws BusinessException 검증 실패 시
	 */
	@Transactional
	public void addToCart(String memberId, int prodNum, int quantity) {
		log.debug("장바구니 추가: memberId={}, prodNum={}, quantity={}", memberId, prodNum, quantity);

		// (1) 수량 유효성 검증
		validateQuantity(quantity);

		// (2) 상품 존재 여부 및 재고 확인
		ProductDto product = validateProductAndStock(prodNum, quantity);

		// (4) 이미 장바구니에 있는 상품인지 확인
		CartDto existingCart = cartMapper.findByMemberAndProduct(memberId, prodNum);

		// (3) 이미 장바구니에 있는 경우 수량 증가, 없으면 새로 추가
		if (existingCart != null) {
			handleExistingCartItem(existingCart, product, quantity);
		} else {
			createNewCartItem(memberId, prodNum, quantity, false, null, null);
		}
	}

	/**
	 * 장바구니에 선물로 상품 추가
	 *
	 * @param memberId 회원 ID (구매자)
	 * @param prodNum 상품 번호
	 * @param quantity 수량
	 * @param giftToMemberId 선물 받는 사람 ID
	 * @param giftMessage 선물 메시지
	 * @throws BusinessException 검증 실패 시
	 */
	@Transactional
	public void addToCartAsGift(String memberId, int prodNum, int quantity,
								 String giftToMemberId, String giftMessage) {
		log.debug("선물 장바구니 추가: memberId={}, prodNum={}, quantity={}, giftTo={}",
				memberId, prodNum, quantity, giftToMemberId);

		// (1) 기본 검증
		validateQuantity(quantity);

		if (giftToMemberId == null || giftToMemberId.trim().isEmpty()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "선물 받는 사람을 지정해주세요.");
		}

		// (2) 커플 매칭 확인
		validateCoupleMatching(memberId, giftToMemberId);

		// (3) 상품 존재 여부 및 재고 확인
		ProductDto product = validateProductAndStock(prodNum, quantity);

		// (4) 이미 장바구니에 있는 경우 수량 증가 + 선물 정보 업데이트, 없으면 새로 추가
		CartDto existingCart = cartMapper.findByMemberAndProduct(memberId, prodNum);

		if (existingCart != null) {
			handleExistingCartItemAsGift(existingCart, product, quantity, giftToMemberId, giftMessage);
		} else {
			createNewCartItem(memberId, prodNum, quantity, true, giftToMemberId, giftMessage);
		}
	}

	/**
	 * 장바구니 아이템을 선물로 설정
	 *
	 * @param cartId 장바구니 ID
	 * @param giftToMemberId 선물 받는 사람 ID
	 * @param giftMessage 선물 메시지
	 * @throws BusinessException 검증 실패 시
	 */
	@Transactional
	public void setAsGift(int cartId, String giftToMemberId, String giftMessage) {
		log.debug("장바구니 선물 설정: cartId={}, giftTo={}", cartId, giftToMemberId);

		// (1) 장바구니 아이템 조회
		CartDto cart = getCartById(cartId);

		// (2) 커플 매칭 확인
		validateCoupleMatching(cart.getMemberId(), giftToMemberId);

		// (3) 선물 설정
		cart.setAsGift(giftToMemberId, giftMessage);
		int result = cartMapper.updateGiftInfo(cart);

		if (result == 0) {
			throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "장바구니 아이템을 찾을 수 없습니다: cartId=" + cartId);
		}

		log.info("선물 설정 완료: cartId={}, giftTo={}", cartId, giftToMemberId);
	}

	/**
	 * 장바구니 아이템을 일반 구매로 변경
	 *
	 * @param cartId 장바구니 ID
	 * @throws BusinessException 검증 실패 시
	 */
	@Transactional
	public void setAsNormalPurchase(int cartId) {
		log.debug("장바구니 일반 구매 변경: cartId={}", cartId);

		CartDto cart = getCartById(cartId);
		cart.setAsNormalPurchase();

		int result = cartMapper.updateGiftInfo(cart);
		if (result == 0) {
			throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "장바구니 아이템을 찾을 수 없습니다: cartId=" + cartId);
		}

		log.info("일반 구매 변경 완료: cartId={}", cartId);
	}

	/**
	 * 장바구니 수량 수정
	 *
	 * @param cartId 장바구니 ID
	 * @param quantity 변경할 수량
	 * @throws BusinessException 검증 실패 시
	 */
	@Transactional
	public void updateCartQuantity(int cartId, int quantity) {
		log.debug("장바구니 수량 수정: cartId={}, quantity={}", cartId, quantity);

		validateQuantity(quantity);

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
	 * @throws BusinessException 검증 실패 시
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
	 * 수량 유효성 검증
	 *
	 * @param quantity 수량
	 * @throws BusinessException 수량이 0 이하인 경우
	 */
	private void validateQuantity(int quantity) {
		if (quantity < AppConstants.MIN_ORDER_QUANTITY) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, AppConstants.MSG_INVALID_QUANTITY);
		}
	}

	/**
	 * 상품 존재 여부 및 재고 확인
	 *
	 * @param prodNum 상품 번호
	 * @param quantity 요청 수량
	 * @return 상품 정보
	 * @throws BusinessException 상품이 없거나 재고 부족 시
	 */
	private ProductDto validateProductAndStock(int prodNum, int quantity) {
		// 상품 존재 여부
		ProductDto product = productMapper.getProductByNum(prodNum);
		if (product == null) {
			throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "prodNum=" + prodNum);
		}

		// 재고 확인
		if (product.getProdStock() < quantity) {
			throw new BusinessException(
					ErrorCode.INVALID_INPUT_VALUE,
					String.format(AppConstants.MSG_INSUFFICIENT_STOCK, quantity, product.getProdStock())
			);
		}

		return product;
	}

	/**
	 * 기존 장바구니 아이템 수량 증가 처리
	 *
	 * @param existingCart 기존 장바구니 아이템
	 * @param product 상품 정보
	 * @param additionalQuantity 추가 수량
	 * @throws BusinessException 재고 부족 시
	 */
	private void handleExistingCartItem(CartDto existingCart, ProductDto product, int additionalQuantity) {
		int newQuantity = existingCart.getCartQuantity() + additionalQuantity;

		// 재고 재확인
		if (product.getProdStock() < newQuantity) {
			throw new BusinessException(
					ErrorCode.INVALID_INPUT_VALUE,
					String.format("재고 부족: 장바구니 수량=%d, 추가 수량=%d, 재고=%d",
							existingCart.getCartQuantity(), additionalQuantity, product.getProdStock())
			);
		}

		updateCartQuantity(existingCart.getCartId(), newQuantity);
		log.info("장바구니 수량 증가: cartId={}, {} → {}", existingCart.getCartId(),
				existingCart.getCartQuantity(), newQuantity);
	}

	/**
	 * 기존 장바구니 아이템 수량 증가 + 선물 정보 업데이트
	 *
	 * @param existingCart 기존 장바구니 아이템
	 * @param product 상품 정보
	 * @param additionalQuantity 추가 수량
	 * @param giftToMemberId 선물 받는 사람 ID
	 * @param giftMessage 선물 메시지
	 * @throws BusinessException 재고 부족 시
	 */
	private void handleExistingCartItemAsGift(CartDto existingCart, ProductDto product, int additionalQuantity,
											   String giftToMemberId, String giftMessage) {
		int newQuantity = existingCart.getCartQuantity() + additionalQuantity;

		// 재고 재확인
		if (product.getProdStock() < newQuantity) {
			throw new BusinessException(
					ErrorCode.INVALID_INPUT_VALUE,
					String.format("재고 부족: 장바구니 수량=%d, 추가 수량=%d, 재고=%d",
							existingCart.getCartQuantity(), additionalQuantity, product.getProdStock())
			);
		}

		updateCartQuantity(existingCart.getCartId(), newQuantity);
		existingCart.setAsGift(giftToMemberId, giftMessage);
		cartMapper.updateGiftInfo(existingCart);

		log.info("장바구니 수량 증가 + 선물 설정: cartId={}, {} → {}, giftTo={}",
				existingCart.getCartId(), existingCart.getCartQuantity(), newQuantity, giftToMemberId);
	}

	/**
	 * 새로운 장바구니 아이템 생성
	 *
	 * @param memberId 회원 ID
	 * @param prodNum 상품 번호
	 * @param quantity 수량
	 * @param isGift 선물 여부
	 * @param giftToMemberId 선물 받는 사람 ID (선물인 경우)
	 * @param giftMessage 선물 메시지 (선물인 경우)
	 * @throws BusinessException DB 삽입 실패 시
	 */
	private void createNewCartItem(String memberId, int prodNum, int quantity, boolean isGift,
								   String giftToMemberId, String giftMessage) {
		CartDto cartDto = new CartDto();
		cartDto.setCartId(IdGenerator.generateCartId());
		cartDto.setCartQuantity(quantity);
		cartDto.setCartRegDate(DateTimeUtils.getCurrentDateTime());
		cartDto.setMemberId(memberId);
		cartDto.setProdNum(prodNum);

		if (isGift) {
			cartDto.setAsGift(giftToMemberId, giftMessage);
		} else {
			cartDto.setAsNormalPurchase();
		}

		int result = cartMapper.addToCart(cartDto);
		if (result == 0) {
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
					isGift ? "선물 장바구니 추가 실패" : "장바구니 추가 실패");
		}

		log.info("{} 장바구니 추가 완료: cartId={}, prodNum={}, quantity={}{}",
				isGift ? "선물" : "일반",
				cartDto.getCartId(), prodNum, quantity,
				isGift ? ", giftTo=" + giftToMemberId : "");
	}

	/**
	 * 장바구니 ID로 조회
	 *
	 * @param cartId 장바구니 ID
	 * @return CartDto
	 * @throws BusinessException 찾을 수 없는 경우
	 */
	private CartDto getCartById(int cartId) {
		CartDto cart = cartMapper.findById(cartId);
		if (cart == null) {
			throw new BusinessException(
					ErrorCode.ENTITY_NOT_FOUND,
					"장바구니 아이템을 찾을 수 없습니다: cartId=" + cartId
			);
		}
		return cart;
	}

	/**
	 * 커플 매칭 확인
	 *
	 * @param memberId 회원 ID
	 * @param targetMemberId 대상 회원 ID
	 * @throws BusinessException 매칭되지 않은 경우
	 */
	private void validateCoupleMatching(String memberId, String targetMemberId) {
		// (1) 본인 확인
		MemberDto member = memberMapper.reloadMemberData(memberId);
		if (member == null) {
			throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND, "memberId=" + memberId);
		}

		// (2) 대상 회원 확인
		MemberDto targetMember = memberMapper.reloadMemberData(targetMemberId);
		if (targetMember == null) {
			throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND, "targetMemberId=" + targetMemberId);
		}

		// (3) 커플 매칭 확인
		String matchedId = member.getMemberMatchId();
		if (matchedId == null || !matchedId.equals(targetMemberId)) {
			throw new BusinessException(
					ErrorCode.INVALID_INPUT_VALUE,
					String.format("매칭되지 않은 회원입니다. memberId=%s, targetMemberId=%s, matchedId=%s",
							memberId, targetMemberId, matchedId)
			);
		}

		log.debug("커플 매칭 확인 완료: {} ↔ {}", memberId, targetMemberId);
	}

	// ID 생성 및 날짜시간 유틸리티는 IdGenerator와 DateTimeUtils로 통합됨
}
