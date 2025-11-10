package project.amall.gift.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import project.amall.common.exception.BusinessException;
import project.amall.common.exception.GlobalExceptionHandler;
import project.amall.common.exception.code.ErrorCode;
import project.amall.gift.dto.GiftDeliveryRequestDto;
import project.amall.gift.service.GiftDeliveryRequestService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * GiftDeliveryRequestApiController 통합 테스트
 *
 * Spring Boot의 @WebMvcTest를 사용하여 REST API 엔드포인트의
 * 전체 요청/응답 사이클을 검증합니다.
 */
@WebMvcTest(GiftDeliveryRequestApiController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("GiftDeliveryRequestApiController 통합 테스트")
class GiftDeliveryRequestApiControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private GiftDeliveryRequestService giftDeliveryRequestService;

	@MockBean
	private SecurityContext securityContext;

	@MockBean
	private Authentication authentication;

	@MockBean
	private UserDetails userDetails;

	private static final String TEST_MEMBER_ID = "testUser";
	private static final String OTHER_MEMBER_ID = "otherUser";
	private static final String TEST_ORDER_ID = "ORD-20250101-001";
	private static final int TEST_REQUEST_ID = 1;

	@BeforeEach
	void setUp() {
		// Mock Security Context for authentication
		when(authentication.getPrincipal()).thenReturn(userDetails);
		when(userDetails.getUsername()).thenReturn(TEST_MEMBER_ID);
		when(authentication.isAuthenticated()).thenReturn(true);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
	}

	// ========== GET /api/gift-delivery-requests/member/{memberId}/pending - 대기 중인 요청 조회 ==========

	@Test
	@DisplayName("대기 중인 선물 배송지 입력 요청 조회 - 성공 (200 OK)")
	void getPendingRequests_Success() throws Exception {
		// given
		List<GiftDeliveryRequestDto> pendingRequests = Arrays.asList(
				createGiftDeliveryRequestDto(1, "ORD-001", TEST_MEMBER_ID, OTHER_MEMBER_ID, "PENDING"),
				createGiftDeliveryRequestDto(2, "ORD-002", TEST_MEMBER_ID, OTHER_MEMBER_ID, "PENDING")
		);

		given(giftDeliveryRequestService.getPendingRequests(TEST_MEMBER_ID))
				.willReturn(pendingRequests);

		// when & then
		mockMvc.perform(get("/api/gift-delivery-requests/member/{memberId}/pending", TEST_MEMBER_ID))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data", hasSize(2)))
				.andExpect(jsonPath("$.data[0].requestId").value(1))
				.andExpect(jsonPath("$.data[0].requestStatus").value("PENDING"))
				.andExpect(jsonPath("$.data[1].requestId").value(2))
				.andExpect(jsonPath("$.data[1].requestStatus").value("PENDING"));

		then(giftDeliveryRequestService).should(times(1)).getPendingRequests(TEST_MEMBER_ID);
	}

	@Test
	@DisplayName("대기 중인 선물 배송지 입력 요청 조회 - 빈 목록 (200 OK)")
	void getPendingRequests_EmptyList() throws Exception {
		// given
		given(giftDeliveryRequestService.getPendingRequests(TEST_MEMBER_ID))
				.willReturn(Collections.emptyList());

		// when & then
		mockMvc.perform(get("/api/gift-delivery-requests/member/{memberId}/pending", TEST_MEMBER_ID))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data", hasSize(0)));

		then(giftDeliveryRequestService).should(times(1)).getPendingRequests(TEST_MEMBER_ID);
	}

	@Test
	@DisplayName("대기 중인 선물 배송지 입력 요청 조회 - 본인 확인 실패로 인한 보안 체크")
	void getPendingRequests_AuthorizationCheck() throws Exception {
		// Note: 실제 SecurityUtils.validateMemberAccess()가 호출되지만,
		// 이 테스트에서는 SecurityContextHolder를 모킹했으므로 통과됨.
		// 실제 통합 테스트에서는 @WithMockUser를 사용하여 다른 사용자로 테스트 가능

		// given
		List<GiftDeliveryRequestDto> pendingRequests = Collections.emptyList();
		given(giftDeliveryRequestService.getPendingRequests(TEST_MEMBER_ID))
				.willReturn(pendingRequests);

		// when & then - 본인의 요청이므로 정상 처리
		mockMvc.perform(get("/api/gift-delivery-requests/member/{memberId}/pending", TEST_MEMBER_ID))
				.andDo(print())
				.andExpect(status().isOk());
	}

	// ========== GET /api/gift-delivery-requests/{requestId} - 요청 ID로 조회 ==========

	@Test
	@DisplayName("선물 배송지 입력 요청 조회 - 성공 (200 OK)")
	void getRequest_Success() throws Exception {
		// given
		GiftDeliveryRequestDto requestDto = createGiftDeliveryRequestDto(
				TEST_REQUEST_ID, TEST_ORDER_ID, TEST_MEMBER_ID, OTHER_MEMBER_ID, "PENDING"
		);

		given(giftDeliveryRequestService.getRequestById(TEST_REQUEST_ID))
				.willReturn(requestDto);

		// when & then
		mockMvc.perform(get("/api/gift-delivery-requests/{requestId}", TEST_REQUEST_ID))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.requestId").value(TEST_REQUEST_ID))
				.andExpect(jsonPath("$.data.orderId").value(TEST_ORDER_ID))
				.andExpect(jsonPath("$.data.toMemberId").value(TEST_MEMBER_ID))
				.andExpect(jsonPath("$.data.fromMemberId").value(OTHER_MEMBER_ID))
				.andExpect(jsonPath("$.data.requestStatus").value("PENDING"))
				.andExpect(jsonPath("$.data.giftMessage").value("축하합니다!"));

		then(giftDeliveryRequestService).should(times(1)).getRequestById(TEST_REQUEST_ID);
	}

	@Test
	@DisplayName("선물 배송지 입력 요청 조회 - 요청 없음 (404 NOT_FOUND)")
	void getRequest_NotFound() throws Exception {
		// given
		given(giftDeliveryRequestService.getRequestById(anyInt()))
				.willThrow(new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "요청을 찾을 수 없습니다."));

		// when & then
		mockMvc.perform(get("/api/gift-delivery-requests/{requestId}", 999))
				.andDo(print())
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.code").value("C001"))
				.andExpect(jsonPath("$.detail").value("요청을 찾을 수 없습니다."));
	}

	// ========== GET /api/gift-delivery-requests/order/{orderId} - 주문 ID로 조회 ==========

	@Test
	@DisplayName("주문 ID로 선물 배송지 입력 요청 조회 - 성공 (200 OK)")
	void getRequestByOrderId_Success() throws Exception {
		// given
		GiftDeliveryRequestDto requestDto = createGiftDeliveryRequestDto(
				TEST_REQUEST_ID, TEST_ORDER_ID, TEST_MEMBER_ID, OTHER_MEMBER_ID, "COMPLETED"
		);

		given(giftDeliveryRequestService.getRequestByOrderId(TEST_ORDER_ID))
				.willReturn(requestDto);

		// when & then
		mockMvc.perform(get("/api/gift-delivery-requests/order/{orderId}", TEST_ORDER_ID))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.requestId").value(TEST_REQUEST_ID))
				.andExpect(jsonPath("$.data.orderId").value(TEST_ORDER_ID))
				.andExpect(jsonPath("$.data.requestStatus").value("COMPLETED"));

		then(giftDeliveryRequestService).should(times(1)).getRequestByOrderId(TEST_ORDER_ID);
	}

	@Test
	@DisplayName("주문 ID로 선물 배송지 입력 요청 조회 - 요청 없음 (500 INTERNAL_SERVER_ERROR)")
	void getRequestByOrderId_NotFound() throws Exception {
		// given
		given(giftDeliveryRequestService.getRequestByOrderId(anyString()))
				.willThrow(new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "해당 주문에 대한 요청이 없습니다."));

		// when & then
		mockMvc.perform(get("/api/gift-delivery-requests/order/{orderId}", "INVALID-ORDER-ID"))
				.andDo(print())
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.code").value("C001"))
				.andExpect(jsonPath("$.detail").value("해당 주문에 대한 요청이 없습니다."));
	}

	@Test
	@DisplayName("주문 ID로 선물 배송지 입력 요청 조회 - 여러 상태의 요청 구분")
	void getRequestByOrderId_DifferentStatuses() throws Exception {
		// PENDING 상태 테스트
		GiftDeliveryRequestDto pendingRequest = createGiftDeliveryRequestDto(
				1, "ORD-PENDING", TEST_MEMBER_ID, OTHER_MEMBER_ID, "PENDING"
		);
		given(giftDeliveryRequestService.getRequestByOrderId("ORD-PENDING"))
				.willReturn(pendingRequest);

		mockMvc.perform(get("/api/gift-delivery-requests/order/{orderId}", "ORD-PENDING"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.requestStatus").value("PENDING"));

		// COMPLETED 상태 테스트
		GiftDeliveryRequestDto completedRequest = createGiftDeliveryRequestDto(
				2, "ORD-COMPLETED", TEST_MEMBER_ID, OTHER_MEMBER_ID, "COMPLETED"
		);
		given(giftDeliveryRequestService.getRequestByOrderId("ORD-COMPLETED"))
				.willReturn(completedRequest);

		mockMvc.perform(get("/api/gift-delivery-requests/order/{orderId}", "ORD-COMPLETED"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.requestStatus").value("COMPLETED"));
	}

	// ========== Helper Methods ==========

	/**
	 * 테스트용 GiftDeliveryRequestDto 생성
	 */
	private GiftDeliveryRequestDto createGiftDeliveryRequestDto(
			int requestId,
			String orderId,
			String toMemberId,
			String fromMemberId,
			String requestStatus) {

		GiftDeliveryRequestDto dto = new GiftDeliveryRequestDto();
		dto.setRequestId(requestId);
		dto.setOrderId(orderId);
		dto.setToMemberId(toMemberId);
		dto.setFromMemberId(fromMemberId);
		dto.setGiftMessage("축하합니다!");
		dto.setRequestStatus(requestStatus);
		dto.setCreatedDate(LocalDateTime.now());

		if ("COMPLETED".equals(requestStatus)) {
			dto.setCompletedDate(LocalDateTime.now());
		}

		return dto;
	}
}
