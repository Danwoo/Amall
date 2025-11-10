package project.amall.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.amall.common.response.ApiResponse;
import project.amall.order.dto.OrderDto;
import project.amall.order.dto.request.CreateGiftOrderRequest;
import project.amall.order.dto.request.CreateOrderRequest;
import project.amall.order.dto.request.UpdateDeliveryAddressRequest;
import project.amall.order.dto.response.OrderResponse;
import project.amall.order.service.OrderService;
import project.amall.common.util.SecurityUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 주문 RESTful API Controller
 *
 * Phase 9: Order System Implementation
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderApiController {

	private final OrderService orderService;

	/**
	 * 일반 주문 생성
	 *
	 * POST /api/orders/{memberId}
	 *
	 * @param memberId 회원 ID
	 * @param request 주문 생성 요청
	 * @return 주문 ID
	 */
	@PostMapping("/{memberId}")
	public ResponseEntity<ApiResponse<String>> createOrder(
			@PathVariable String memberId,
			@Valid @RequestBody CreateOrderRequest request) {

		log.info("일반 주문 생성 API: memberId={}, cartIds={}", memberId, request.getCartIds());

		// 본인 확인
		SecurityUtils.validateMemberAccess(memberId);

		String orderId = orderService.createOrder(
				memberId,
				request.getCartIds(),
				request.getDeliveryName(),
				request.getDeliveryPhone(),
				request.getDeliveryPostCode(),
				request.getDeliveryAddress(),
				request.getDeliveryDetailAddress(),
				request.getDeliveryMessage(),
				request.getPaymentMethod()
		);

		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(ApiResponse.success(orderId, "주문이 생성되었습니다."));
	}

	/**
	 * 선물 주문 생성
	 *
	 * POST /api/orders/{memberId}/gift
	 *
	 * @param memberId 회원 ID (선물 보내는 사람)
	 * @param request 선물 주문 생성 요청
	 * @return 주문 ID
	 */
	@PostMapping("/{memberId}/gift")
	public ResponseEntity<ApiResponse<String>> createGiftOrder(
			@PathVariable String memberId,
			@Valid @RequestBody CreateGiftOrderRequest request) {

		log.info("선물 주문 생성 API: memberId={}, cartId={}", memberId, request.getCartId());

		// 본인 확인
		SecurityUtils.validateMemberAccess(memberId);

		String orderId = orderService.createGiftOrder(
				memberId,
				request.getCartId(),
				request.getGiftMessage(),
				request.getPaymentMethod()
		);

		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(ApiResponse.success(orderId, "선물 주문이 생성되었습니다."));
	}

	/**
	 * 주문 조회
	 *
	 * GET /api/orders/{orderId}
	 *
	 * @param orderId 주문 ID
	 * @return 주문 정보
	 */
	@GetMapping("/{orderId}")
	public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
			@PathVariable String orderId) {

		log.info("주문 조회 API: orderId={}", orderId);

		OrderDto order = orderService.getOrderWithItems(orderId);
		OrderResponse response = OrderResponse.from(order);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 회원의 주문 목록 조회
	 *
	 * GET /api/orders/member/{memberId}
	 *
	 * @param memberId 회원 ID
	 * @return 주문 목록
	 */
	@GetMapping("/member/{memberId}")
	public ResponseEntity<ApiResponse<List<OrderResponse>>> getMemberOrders(
			@PathVariable String memberId) {

		log.info("회원 주문 목록 조회 API: memberId={}", memberId);

		// 본인 확인
		SecurityUtils.validateMemberAccess(memberId);

		List<OrderDto> orders = orderService.getMemberOrders(memberId);
		List<OrderResponse> response = orders.stream()
				.map(OrderResponse::from)
				.collect(Collectors.toList());

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 회원이 선물 받은 주문 목록 조회
	 *
	 * GET /api/orders/member/{memberId}/received-gifts
	 *
	 * @param memberId 회원 ID (선물 받은 사람)
	 * @return 선물 받은 주문 목록
	 */
	@GetMapping("/member/{memberId}/received-gifts")
	public ResponseEntity<ApiResponse<List<OrderResponse>>> getReceivedGiftOrders(
			@PathVariable String memberId) {

		log.info("선물 받은 주문 목록 조회 API: memberId={}", memberId);

		// 본인 확인
		SecurityUtils.validateMemberAccess(memberId);

		List<OrderDto> orders = orderService.getGiftOrdersReceived(memberId);
		List<OrderResponse> response = orders.stream()
				.map(OrderResponse::from)
				.collect(Collectors.toList());

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 배송지 정보 업데이트 (선물 받는 사람)
	 *
	 * PUT /api/orders/{orderId}/delivery-address
	 *
	 * @param memberId 회원 ID (선물 받는 사람)
	 * @param orderId 주문 ID
	 * @param request 배송지 정보
	 * @return 성공 메시지
	 */
	@PutMapping("/{orderId}/delivery-address")
	public ResponseEntity<ApiResponse<Void>> updateDeliveryAddress(
			@RequestParam String memberId,
			@PathVariable String orderId,
			@Valid @RequestBody UpdateDeliveryAddressRequest request) {

		log.info("배송지 정보 업데이트 API: memberId={}, orderId={}", memberId, orderId);

		orderService.updateDeliveryAddress(
				memberId,
				orderId,
				request.getDeliveryName(),
				request.getDeliveryPhone(),
				request.getDeliveryPostCode(),
				request.getDeliveryAddress(),
				request.getDeliveryDetailAddress(),
				request.getDeliveryMessage()
		);

		return ResponseEntity.ok(ApiResponse.success("배송지 정보가 업데이트되었습니다."));
	}

	/**
	 * 주문 취소
	 *
	 * DELETE /api/orders/{orderId}
	 *
	 * @param memberId 회원 ID
	 * @param orderId 주문 ID
	 * @return 성공 메시지
	 */
	@DeleteMapping("/{orderId}")
	public ResponseEntity<ApiResponse<Void>> cancelOrder(
			@RequestParam String memberId,
			@PathVariable String orderId) {

		log.info("주문 취소 API: memberId={}, orderId={}", memberId, orderId);

		orderService.cancelOrder(memberId, orderId);

		return ResponseEntity.ok(ApiResponse.success("주문이 취소되었습니다."));
	}
}
