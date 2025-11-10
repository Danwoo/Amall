package project.amall.gift.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import project.amall.common.exception.BusinessException;
import project.amall.common.exception.code.ErrorCode;
import project.amall.gift.dto.GiftDeliveryRequestDto;
import project.amall.gift.mapper.GiftDeliveryRequestMapper;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * GiftDeliveryRequestService 테스트
 *
 * Phase 9: Order System Implementation
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GiftDeliveryRequestService 테스트")
class GiftDeliveryRequestServiceTest {

	@InjectMocks
	private GiftDeliveryRequestService giftDeliveryRequestService;

	@Mock
	private GiftDeliveryRequestMapper giftDeliveryRequestMapper;

	// ========== getPendingRequests 테스트 ==========

	@Test
	@DisplayName("대기 중인 요청 조회 - 여러 건 정상")
	void getPendingRequests_Success_MultipleRequests() {
		// given
		String memberId = "recipient";
		List<GiftDeliveryRequestDto> expectedRequests = Arrays.asList(
				createGiftDeliveryRequest(1, "ORD-001", memberId, "sender1", GiftDeliveryRequestDto.RequestStatus.PENDING.getCode()),
				createGiftDeliveryRequest(2, "ORD-002", memberId, "sender2", GiftDeliveryRequestDto.RequestStatus.PENDING.getCode()),
				createGiftDeliveryRequest(3, "ORD-003", memberId, "sender3", GiftDeliveryRequestDto.RequestStatus.PENDING.getCode())
		);

		given(giftDeliveryRequestMapper.findPendingRequestsByMemberId(memberId)).willReturn(expectedRequests);

		// when
		List<GiftDeliveryRequestDto> result = giftDeliveryRequestService.getPendingRequests(memberId);

		// then
		assertThat(result).hasSize(3);
		assertThat(result).isEqualTo(expectedRequests);
		assertThat(result.get(0).getRequestId()).isEqualTo(1);
		assertThat(result.get(1).getRequestId()).isEqualTo(2);
		assertThat(result.get(2).getRequestId()).isEqualTo(3);
		then(giftDeliveryRequestMapper).should(times(1)).findPendingRequestsByMemberId(memberId);
	}

	@Test
	@DisplayName("대기 중인 요청 조회 - 빈 목록 정상")
	void getPendingRequests_Success_EmptyList() {
		// given
		String memberId = "recipient";

		given(giftDeliveryRequestMapper.findPendingRequestsByMemberId(memberId)).willReturn(Collections.emptyList());

		// when
		List<GiftDeliveryRequestDto> result = giftDeliveryRequestService.getPendingRequests(memberId);

		// then
		assertThat(result).isEmpty();
		assertThat(result).hasSize(0);
		then(giftDeliveryRequestMapper).should(times(1)).findPendingRequestsByMemberId(memberId);
	}

	@Test
	@DisplayName("대기 중인 요청 조회 - 단일 요청 정상")
	void getPendingRequests_Success_SingleRequest() {
		// given
		String memberId = "recipient";
		List<GiftDeliveryRequestDto> expectedRequests = Arrays.asList(
				createGiftDeliveryRequest(1, "ORD-001", memberId, "sender1", GiftDeliveryRequestDto.RequestStatus.PENDING.getCode())
		);

		given(giftDeliveryRequestMapper.findPendingRequestsByMemberId(memberId)).willReturn(expectedRequests);

		// when
		List<GiftDeliveryRequestDto> result = giftDeliveryRequestService.getPendingRequests(memberId);

		// then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getRequestId()).isEqualTo(1);
		assertThat(result.get(0).getOrderId()).isEqualTo("ORD-001");
		assertThat(result.get(0).getToMemberId()).isEqualTo(memberId);
		assertThat(result.get(0).getFromMemberId()).isEqualTo("sender1");
		then(giftDeliveryRequestMapper).should(times(1)).findPendingRequestsByMemberId(memberId);
	}

	// ========== getRequestById 테스트 ==========

	@Test
	@DisplayName("요청 ID로 조회 - 정상")
	void getRequestById_Success() {
		// given
		int requestId = 1;
		GiftDeliveryRequestDto expectedRequest = createGiftDeliveryRequest(
				requestId, "ORD-001", "recipient", "sender", GiftDeliveryRequestDto.RequestStatus.PENDING.getCode()
		);

		given(giftDeliveryRequestMapper.findRequestById(requestId)).willReturn(expectedRequest);

		// when
		GiftDeliveryRequestDto result = giftDeliveryRequestService.getRequestById(requestId);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getRequestId()).isEqualTo(requestId);
		assertThat(result.getOrderId()).isEqualTo("ORD-001");
		assertThat(result.getToMemberId()).isEqualTo("recipient");
		assertThat(result.getFromMemberId()).isEqualTo("sender");
		assertThat(result.getRequestStatus()).isEqualTo(GiftDeliveryRequestDto.RequestStatus.PENDING.getCode());
		then(giftDeliveryRequestMapper).should(times(1)).findRequestById(requestId);
	}

	@Test
	@DisplayName("요청 ID로 조회 - 요청을 찾을 수 없을 때 예외")
	void getRequestById_NotFound() {
		// given
		int requestId = 999;

		given(giftDeliveryRequestMapper.findRequestById(requestId)).willReturn(null);

		// when & then
		assertThatThrownBy(() -> giftDeliveryRequestService.getRequestById(requestId))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> {
					BusinessException be = (BusinessException) e;
					assertThat(be.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
					assertThat(be.getMessage()).contains("선물 배송지 입력 요청을 찾을 수 없습니다");
				});
	}

	// ========== getRequestByOrderId 테스트 ==========

	@Test
	@DisplayName("주문 ID로 조회 - 정상")
	void getRequestByOrderId_Success() {
		// given
		String orderId = "ORD-001";
		GiftDeliveryRequestDto expectedRequest = createGiftDeliveryRequest(
				1, orderId, "recipient", "sender", GiftDeliveryRequestDto.RequestStatus.COMPLETED.getCode()
		);

		given(giftDeliveryRequestMapper.findRequestByOrderId(orderId)).willReturn(expectedRequest);

		// when
		GiftDeliveryRequestDto result = giftDeliveryRequestService.getRequestByOrderId(orderId);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getOrderId()).isEqualTo(orderId);
		assertThat(result.getRequestId()).isEqualTo(1);
		assertThat(result.getToMemberId()).isEqualTo("recipient");
		assertThat(result.getFromMemberId()).isEqualTo("sender");
		assertThat(result.getRequestStatus()).isEqualTo(GiftDeliveryRequestDto.RequestStatus.COMPLETED.getCode());
		assertThat(result.isCompleted()).isTrue();
		then(giftDeliveryRequestMapper).should(times(1)).findRequestByOrderId(orderId);
	}

	@Test
	@DisplayName("주문 ID로 조회 - 요청을 찾을 수 없을 때 예외")
	void getRequestByOrderId_NotFound() {
		// given
		String orderId = "ORD-999";

		given(giftDeliveryRequestMapper.findRequestByOrderId(orderId)).willReturn(null);

		// when & then
		assertThatThrownBy(() -> giftDeliveryRequestService.getRequestByOrderId(orderId))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> {
					BusinessException be = (BusinessException) e;
					assertThat(be.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
					assertThat(be.getMessage()).contains("선물 배송지 입력 요청을 찾을 수 없습니다");
				});
	}

	// ========== Helper Methods ==========

	/**
	 * 테스트용 선물 배송지 입력 요청 생성
	 */
	private GiftDeliveryRequestDto createGiftDeliveryRequest(
			int requestId, String orderId, String toMemberId, String fromMemberId, String status) {
		GiftDeliveryRequestDto request = new GiftDeliveryRequestDto();
		request.setRequestId(requestId);
		request.setOrderId(orderId);
		request.setToMemberId(toMemberId);
		request.setFromMemberId(fromMemberId);
		request.setGiftMessage("축하합니다!");
		request.setRequestStatus(status);
		request.setCreatedDate(LocalDateTime.now());
		if (GiftDeliveryRequestDto.RequestStatus.COMPLETED.getCode().equals(status)) {
			request.setCompletedDate(LocalDateTime.now());
		}
		return request;
	}
}
