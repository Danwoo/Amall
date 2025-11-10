package project.amall.wishlist.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import project.amall.common.exception.BusinessException;
import project.amall.common.exception.code.ErrorCode;
import project.amall.product.dto.ProductDto;
import project.amall.product.mapper.ProductMapper;
import project.amall.wishlist.dto.WishListDto;
import project.amall.wishlist.mapper.WishListMapper;

/**
 * 위시리스트 서비스
 *
 * 위시리스트 관련 비즈니스 로직 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishListService {

	private final WishListMapper wishListMapper;
	private final ProductMapper productMapper;

	/**
	 * 회원의 위시리스트 조회
	 *
	 * @param memberId 회원 ID
	 * @return 위시리스트 상품 목록 (최대 4개)
	 */
	public List<ProductDto> showThisIdWishList(String memberId) {
		log.debug("위시리스트 조회: memberId={}", memberId);
		return wishListMapper.showThisIdWishList(memberId);
	}

	/**
	 * 위시리스트에 상품 추가
	 *
	 * 이미 존재하는 상품이면 중복 추가 방지
	 *
	 * @param memberId 회원 ID
	 * @param prodNum 상품 번호
	 */
	@Transactional
	public void addWishList(String memberId, int prodNum) {
		log.debug("위시리스트 추가: memberId={}, prodNum={}", memberId, prodNum);

		// (1) 상품 존재 여부 확인
		ProductDto product = productMapper.getProductByNum(prodNum);
		if (product == null) {
			throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "prodNum=" + prodNum);
		}

		// (2) 이미 위시리스트에 있는지 확인
		WishListDto existing = wishListMapper.findByMemberAndProduct(memberId, prodNum);
		if (existing != null) {
			log.warn("위시리스트 중복 추가 시도: memberId={}, prodNum={}", memberId, prodNum);
			throw new BusinessException(
					ErrorCode.INVALID_INPUT_VALUE,
					"이미 위시리스트에 추가된 상품입니다."
			);
		}

		// (3) 위시리스트에 추가
		WishListDto wishListDto = new WishListDto();
		wishListDto.setWishlistId(generateWishlistId());
		wishListDto.setMemberId(memberId);
		wishListDto.setProdNum(prodNum);

		int result = wishListMapper.addWishList(wishListDto);
		if (result == 0) {
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "위시리스트 추가 실패");
		}

		log.info("위시리스트 추가 완료: wishlistId={}, prodNum={}", wishListDto.getWishlistId(), prodNum);
	}

	/**
	 * 위시리스트에서 상품 삭제
	 *
	 * @param wishlistId 위시리스트 ID
	 */
	@Transactional
	public void removeWishList(int wishlistId) {
		log.debug("위시리스트 삭제: wishlistId={}", wishlistId);

		int result = wishListMapper.removeWishList(wishlistId);
		if (result == 0) {
			throw new BusinessException(
					ErrorCode.ENTITY_NOT_FOUND,
					"존재하지 않는 위시리스트 항목: wishlistId=" + wishlistId
			);
		}

		log.info("위시리스트 삭제 완료: wishlistId={}", wishlistId);
	}

	/**
	 * 회원의 위시리스트 전체 삭제
	 *
	 * @param memberId 회원 ID
	 */
	@Transactional
	public void clearWishList(String memberId) {
		log.debug("위시리스트 전체 삭제: memberId={}", memberId);

		int result = wishListMapper.clearWishList(memberId);
		log.info("위시리스트 전체 삭제 완료: memberId={}, 삭제 개수={}", memberId, result);
	}

	/**
	 * 특정 상품이 위시리스트에 있는지 확인
	 *
	 * @param memberId 회원 ID
	 * @param prodNum 상품 번호
	 * @return 위시리스트에 있으면 true, 없으면 false
	 */
	public boolean isInWishList(String memberId, int prodNum) {
		log.debug("위시리스트 포함 여부 확인: memberId={}, prodNum={}", memberId, prodNum);
		WishListDto wishList = wishListMapper.findByMemberAndProduct(memberId, prodNum);
		return wishList != null;
	}

	// ========== Private Helper Methods ==========

	/**
	 * 위시리스트 ID 생성
	 *
	 * TODO: 실제로는 시퀀스나 AUTO_INCREMENT 사용 권장
	 */
	private int generateWishlistId() {
		return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
	}
}
