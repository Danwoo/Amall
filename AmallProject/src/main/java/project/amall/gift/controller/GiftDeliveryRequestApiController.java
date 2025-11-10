package project.amall.gift.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.amall.common.response.ApiResponse;
import project.amall.common.util.SecurityUtils;
import project.amall.gift.dto.GiftDeliveryRequestDto;
import project.amall.gift.dto.response.GiftDeliveryRequestResponse;
import project.amall.gift.service.GiftDeliveryRequestService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 선물 배송지 입력 요청 RESTful API Controller
 *
 * Phase 9: Order System Implementation
 */
@Slf4j
@RestController
@RequestMapping("/api/gift-delivery-requests")
@RequiredArgsConstructor
public class GiftDeliveryRequestApiController {

	private final GiftDeliveryRequestService giftDeliveryRequestService;

	/**
	 * 회원의 대기 중인 선물 배송지 입력 요청 목록 조회
	 *
	 * GET /api/gift-delivery-requests/member/{memberId}/pending
	 *
	 * @param memberId 회원 ID (선물 받는 사람)
	 * @return 대기 중인 요청 목록
	 */
	@GetMapping("/member/{memberId}/pending")
	public ResponseEntity<ApiResponse<List<GiftDeliveryRequestResponse>>> getPendingRequests(
			@PathVariable String memberId) {

		log.info("대기 중인 선물 배송지 입력 요청 조회 API: memberId={}", memberId);

		// 본인 확인
		SecurityUtils.validateMemberAccess(memberId);

		List<GiftDeliveryRequestDto> requests = giftDeliveryRequestService.getPendingRequests(memberId);
		List<GiftDeliveryRequestResponse> response = requests.stream()
				.map(GiftDeliveryRequestResponse::from)
				.collect(Collectors.toList());

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 선물 배송지 입력 요청 조회
	 *
	 * GET /api/gift-delivery-requests/{requestId}
	 *
	 * @param requestId 요청 ID
	 * @return 요청 정보
	 */
	@GetMapping("/{requestId}")
	public ResponseEntity<ApiResponse<GiftDeliveryRequestResponse>> getRequest(
			@PathVariable int requestId) {

		log.info("선물 배송지 입력 요청 조회 API: requestId={}", requestId);

		GiftDeliveryRequestDto request = giftDeliveryRequestService.getRequestById(requestId);
		GiftDeliveryRequestResponse response = GiftDeliveryRequestResponse.from(request);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 주문 ID로 선물 배송지 입력 요청 조회
	 *
	 * GET /api/gift-delivery-requests/order/{orderId}
	 *
	 * @param orderId 주문 ID
	 * @return 요청 정보
	 */
	@GetMapping("/order/{orderId}")
	public ResponseEntity<ApiResponse<GiftDeliveryRequestResponse>> getRequestByOrderId(
			@PathVariable String orderId) {

		log.info("주문 ID로 선물 배송지 입력 요청 조회 API: orderId={}", orderId);

		GiftDeliveryRequestDto request = giftDeliveryRequestService.getRequestByOrderId(orderId);
		GiftDeliveryRequestResponse response = GiftDeliveryRequestResponse.from(request);

		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
