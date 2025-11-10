package project.amall.cart.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.amall.cart.dto.CartDto;
import project.amall.cart.dto.request.AddToCartAsGiftRequest;
import project.amall.cart.dto.request.AddToCartRequest;
import project.amall.cart.dto.request.SetGiftRequest;
import project.amall.cart.dto.request.UpdateCartQuantityRequest;
import project.amall.cart.dto.response.CartResponse;
import project.amall.cart.service.CartService;
import project.amall.common.response.ApiResponse;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 장바구니 RESTful API Controller
 *
 * Phase 9-3: 선물 기능 포함
 */
@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartApiController {

	private final CartService cartService;

	/**
	 * 장바구니 조회
	 *
	 * GET /api/cart/{memberId}
	 *
	 * @param memberId 회원 ID
	 * @return 장바구니 목록
	 */
	@GetMapping("/{memberId}")
	public ResponseEntity<ApiResponse<List<CartResponse>>> getCart(
			@PathVariable String memberId) {

		log.info("장바구니 조회 API: memberId={}", memberId);

		List<CartDto> cartList = cartService.showMyCart(memberId);
		List<CartResponse> response = cartList.stream()
				.map(CartResponse::from)
				.collect(Collectors.toList());

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 장바구니에 일반 상품 추가
	 *
	 * POST /api/cart/{memberId}/items
	 *
	 * @param memberId 회원 ID
	 * @param request 추가 요청 (상품 번호, 수량)
	 * @return 성공 메시지
	 */
	@PostMapping("/{memberId}/items")
	public ResponseEntity<ApiResponse<Void>> addToCart(
			@PathVariable String memberId,
			@Valid @RequestBody AddToCartRequest request) {

		log.info("장바구니 추가 API: memberId={}, prodNum={}, quantity={}",
				memberId, request.getProdNum(), request.getQuantity());

		cartService.addToCart(memberId, request.getProdNum(), request.getQuantity());

		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(ApiResponse.success("장바구니에 추가되었습니다."));
	}

	/**
	 * 장바구니에 선물로 상품 추가
	 *
	 * POST /api/cart/{memberId}/gift-items
	 *
	 * @param memberId 회원 ID (구매자)
	 * @param request 선물 추가 요청 (상품 번호, 수량, 받는 사람, 메시지)
	 * @return 성공 메시지
	 */
	@PostMapping("/{memberId}/gift-items")
	public ResponseEntity<ApiResponse<Void>> addToCartAsGift(
			@PathVariable String memberId,
			@Valid @RequestBody AddToCartAsGiftRequest request) {

		log.info("선물 장바구니 추가 API: memberId={}, prodNum={}, giftTo={}",
				memberId, request.getProdNum(), request.getGiftToMemberId());

		cartService.addToCartAsGift(
				memberId,
				request.getProdNum(),
				request.getQuantity(),
				request.getGiftToMemberId(),
				request.getGiftMessage()
		);

		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(ApiResponse.success("선물이 장바구니에 추가되었습니다."));
	}

	/**
	 * 장바구니 수량 수정
	 *
	 * PATCH /api/cart/{memberId}/items/{cartId}/quantity
	 *
	 * @param memberId 회원 ID
	 * @param cartId 장바구니 ID
	 * @param request 수량 수정 요청
	 * @return 성공 메시지
	 */
	@PatchMapping("/{memberId}/items/{cartId}/quantity")
	public ResponseEntity<ApiResponse<Void>> updateQuantity(
			@PathVariable String memberId,
			@PathVariable int cartId,
			@Valid @RequestBody UpdateCartQuantityRequest request) {

		log.info("장바구니 수량 수정 API: memberId={}, cartId={}, quantity={}",
				memberId, cartId, request.getQuantity());

		cartService.updateCartQuantity(cartId, request.getQuantity());

		return ResponseEntity.ok(ApiResponse.success("수량이 수정되었습니다."));
	}

	/**
	 * 장바구니 아이템을 선물로 설정
	 *
	 * PUT /api/cart/{memberId}/items/{cartId}/gift
	 *
	 * @param memberId 회원 ID
	 * @param cartId 장바구니 ID
	 * @param request 선물 설정 요청 (받는 사람, 메시지)
	 * @return 성공 메시지
	 */
	@PutMapping("/{memberId}/items/{cartId}/gift")
	public ResponseEntity<ApiResponse<Void>> setAsGift(
			@PathVariable String memberId,
			@PathVariable int cartId,
			@Valid @RequestBody SetGiftRequest request) {

		log.info("장바구니 선물 설정 API: memberId={}, cartId={}, giftTo={}",
				memberId, cartId, request.getGiftToMemberId());

		cartService.setAsGift(cartId, request.getGiftToMemberId(), request.getGiftMessage());

		return ResponseEntity.ok(ApiResponse.success("선물로 설정되었습니다."));
	}

	/**
	 * 장바구니 아이템을 일반 구매로 변경
	 *
	 * DELETE /api/cart/{memberId}/items/{cartId}/gift
	 *
	 * @param memberId 회원 ID
	 * @param cartId 장바구니 ID
	 * @return 성공 메시지
	 */
	@DeleteMapping("/{memberId}/items/{cartId}/gift")
	public ResponseEntity<ApiResponse<Void>> setAsNormalPurchase(
			@PathVariable String memberId,
			@PathVariable int cartId) {

		log.info("장바구니 일반 구매 변경 API: memberId={}, cartId={}", memberId, cartId);

		cartService.setAsNormalPurchase(cartId);

		return ResponseEntity.ok(ApiResponse.success("일반 구매로 변경되었습니다."));
	}

	/**
	 * 장바구니 아이템 삭제
	 *
	 * DELETE /api/cart/{memberId}/items/{cartId}
	 *
	 * @param memberId 회원 ID
	 * @param cartId 장바구니 ID
	 * @return 성공 메시지
	 */
	@DeleteMapping("/{memberId}/items/{cartId}")
	public ResponseEntity<ApiResponse<Void>> removeItem(
			@PathVariable String memberId,
			@PathVariable int cartId) {

		log.info("장바구니 삭제 API: memberId={}, cartId={}", memberId, cartId);

		cartService.removeFromCart(cartId);

		return ResponseEntity.ok(ApiResponse.success("장바구니에서 삭제되었습니다."));
	}

	/**
	 * 장바구니 전체 삭제
	 *
	 * DELETE /api/cart/{memberId}
	 *
	 * @param memberId 회원 ID
	 * @return 성공 메시지
	 */
	@DeleteMapping("/{memberId}")
	public ResponseEntity<ApiResponse<Void>> clearCart(
			@PathVariable String memberId) {

		log.info("장바구니 전체 삭제 API: memberId={}", memberId);

		cartService.clearCart(memberId);

		return ResponseEntity.ok(ApiResponse.success("장바구니가 비워졌습니다."));
	}
}
