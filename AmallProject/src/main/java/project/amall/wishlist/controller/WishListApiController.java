package project.amall.wishlist.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.amall.common.response.ApiResponse;
import project.amall.common.util.SecurityUtils;
import project.amall.product.dto.ProductDto;
import project.amall.wishlist.dto.request.AddToWishListRequest;
import project.amall.wishlist.dto.response.WishListResponse;
import project.amall.wishlist.service.WishListService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 위시리스트 RESTful API Controller
 *
 * Phase 9-3: 선물 기능 포함
 * - 내 위시리스트 조회/추가/삭제
 * - 커플 상대방 위시리스트 조회 (선물 쇼핑용)
 */
@Slf4j
@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishListApiController {

	private final WishListService wishListService;

	/**
	 * 내 위시리스트 조회
	 *
	 * GET /api/wishlist/{memberId}
	 *
	 * @param memberId 회원 ID
	 * @return 위시리스트 상품 목록
	 */
	@GetMapping("/{memberId}")
	public ResponseEntity<ApiResponse<List<WishListResponse>>> getMyWishList(
			@PathVariable String memberId) {

		log.info("위시리스트 조회 API: memberId={}", memberId);

		// 본인 확인
		SecurityUtils.validateMemberAccess(memberId);

		List<ProductDto> wishList = wishListService.getWishList(memberId);
		List<WishListResponse> response = wishList.stream()
				.map(WishListResponse::from)
				.collect(Collectors.toList());

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 커플 상대방의 위시리스트 조회 (선물 쇼핑용)
	 *
	 * GET /api/wishlist/{memberId}/partner
	 *
	 * Phase 9-3: 선물 기능
	 *
	 * @param memberId 회원 ID
	 * @return 상대방의 위시리스트 상품 목록
	 */
	@GetMapping("/{memberId}/partner")
	public ResponseEntity<ApiResponse<List<WishListResponse>>> getPartnerWishList(
			@PathVariable String memberId) {

		log.info("커플 위시리스트 조회 API: memberId={}", memberId);

		// 본인 확인
		SecurityUtils.validateMemberAccess(memberId);

		List<ProductDto> partnerWishList = wishListService.getPartnerWishList(memberId);
		List<WishListResponse> response = partnerWishList.stream()
				.map(WishListResponse::from)
				.collect(Collectors.toList());

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 위시리스트에 상품 추가
	 *
	 * POST /api/wishlist/{memberId}/items
	 *
	 * @param memberId 회원 ID
	 * @param request 추가 요청 (상품 번호)
	 * @return 성공 메시지
	 */
	@PostMapping("/{memberId}/items")
	public ResponseEntity<ApiResponse<Void>> addToWishList(
			@PathVariable String memberId,
			@Valid @RequestBody AddToWishListRequest request) {

		log.info("위시리스트 추가 API: memberId={}, prodNum={}",
				memberId, request.getProdNum());

		// 본인 확인
		SecurityUtils.validateMemberAccess(memberId);

		wishListService.addWishList(memberId, request.getProdNum());

		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(ApiResponse.success("위시리스트에 추가되었습니다."));
	}

	/**
	 * 위시리스트에서 상품 삭제
	 *
	 * DELETE /api/wishlist/{memberId}/items/{wishlistId}
	 *
	 * @param memberId 회원 ID
	 * @param wishlistId 위시리스트 ID
	 * @return 성공 메시지
	 */
	@DeleteMapping("/{memberId}/items/{wishlistId}")
	public ResponseEntity<ApiResponse<Void>> removeFromWishList(
			@PathVariable String memberId,
			@PathVariable int wishlistId) {

		log.info("위시리스트 삭제 API: memberId={}, wishlistId={}", memberId, wishlistId);

		// 본인 확인
		SecurityUtils.validateMemberAccess(memberId);

		wishListService.removeWishList(wishlistId);

		return ResponseEntity.ok(ApiResponse.success("위시리스트에서 삭제되었습니다."));
	}

	/**
	 * 위시리스트 전체 삭제
	 *
	 * DELETE /api/wishlist/{memberId}
	 *
	 * @param memberId 회원 ID
	 * @return 성공 메시지
	 */
	@DeleteMapping("/{memberId}")
	public ResponseEntity<ApiResponse<Void>> clearWishList(
			@PathVariable String memberId) {

		log.info("위시리스트 전체 삭제 API: memberId={}", memberId);

		// 본인 확인
		SecurityUtils.validateMemberAccess(memberId);

		wishListService.clearWishList(memberId);

		return ResponseEntity.ok(ApiResponse.success("위시리스트가 비워졌습니다."));
	}

	/**
	 * 상품이 위시리스트에 있는지 확인
	 *
	 * GET /api/wishlist/{memberId}/check?prodNum=123
	 *
	 * @param memberId 회원 ID
	 * @param prodNum 상품 번호
	 * @return 포함 여부
	 */
	@GetMapping("/{memberId}/check")
	public ResponseEntity<ApiResponse<Boolean>> checkInWishList(
			@PathVariable String memberId,
			@RequestParam int prodNum) {

		log.info("위시리스트 포함 여부 확인 API: memberId={}, prodNum={}", memberId, prodNum);

		// 본인 확인
		SecurityUtils.validateMemberAccess(memberId);

		boolean inWishList = wishListService.isInWishList(memberId, prodNum);

		String message = inWishList
				? "위시리스트에 포함되어 있습니다."
				: "위시리스트에 없습니다.";

		return ResponseEntity.ok(ApiResponse.success(message, inWishList));
	}
}
