package project.amall.wishlist.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import project.amall.common.exception.BusinessException;
import project.amall.common.exception.code.ErrorCode;
import project.amall.common.util.IdGenerator;
import project.amall.member.dto.MemberDto;
import project.amall.member.mapper.MemberMapper;
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
	private final MemberMapper memberMapper;

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
		wishListDto.setWishlistId(IdGenerator.generateWishlistId());
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

	/**
	 * 커플 상대방의 위시리스트 조회 (선물 쇼핑용)
	 *
	 * Phase 9-3: 선물 기능
	 *
	 * @param memberId 내 회원 ID
	 * @return 상대방의 위시리스트
	 */
	public List<ProductDto> showPartnerWishList(String memberId) {
		log.debug("커플 위시리스트 조회: memberId={}", memberId);

		// (1) 내 정보 조회
		MemberDto member = memberMapper.reloadMemberData(memberId);
		if (member == null) {
			throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND, "memberId=" + memberId);
		}

		// (2) 매칭된 상대방 ID 확인
		String partnerMemberId = member.getMemberMatchId();
		if (partnerMemberId == null || partnerMemberId.trim().isEmpty()) {
			throw new BusinessException(
					ErrorCode.INVALID_INPUT_VALUE,
					"매칭된 상대방이 없습니다. 커플 매칭을 먼저 진행해주세요."
			);
		}

		// (3) 상대방 존재 확인
		MemberDto partnerMember = memberMapper.reloadMemberData(partnerMemberId);
		if (partnerMember == null) {
			throw new BusinessException(
					ErrorCode.MEMBER_NOT_FOUND,
					"매칭된 상대방을 찾을 수 없습니다: partnerMemberId=" + partnerMemberId
			);
		}

		// (4) 상대방의 위시리스트 조회
		List<ProductDto> partnerWishList = wishListMapper.showThisIdWishList(partnerMemberId);

		log.info("커플 위시리스트 조회 성공: memberId={}, partnerId={}, 위시리스트 개수={}",
				memberId, partnerMemberId, partnerWishList.size());

		return partnerWishList;
	}

	// ========== Private Helper Methods ==========

	// 위시리스트 ID 생성은 IdGenerator로 통합됨
}
