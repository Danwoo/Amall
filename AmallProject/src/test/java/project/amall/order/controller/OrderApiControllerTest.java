package project.amall.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import project.amall.common.exception.BusinessException;
import project.amall.common.exception.GlobalExceptionHandler;
import project.amall.common.exception.code.ErrorCode;
import project.amall.order.dto.OrderDto;
import project.amall.order.dto.OrderItemDto;
import project.amall.order.dto.request.CreateGiftOrderRequest;
import project.amall.order.dto.request.CreateOrderRequest;
import project.amall.order.dto.request.UpdateDeliveryAddressRequest;
import project.amall.order.service.OrderService;

import java.util.ArrayList;
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
 * OrderApiController 통합 테스트
 *
 * Spring Boot의 @WebMvcTest를 사용하여 REST API 엔드포인트의
 * 전체 요청/응답 사이클을 검증합니다.
 */
@WebMvcTest(OrderApiController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("OrderApiController 통합 테스트")
class OrderApiControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private OrderService orderService;

	@MockBean
	private SecurityContext securityContext;

	@MockBean
	private Authentication authentication;

	@MockBean
	private UserDetails userDetails;

	private static final String TEST_MEMBER_ID = "testUser";
	private static final String OTHER_MEMBER_ID = "otherUser";
	private static final String TEST_ORDER_ID = "ORD-20250101-001";

	@BeforeEach
	void setUp() {
		// Mock Security Context for authentication
		when(authentication.getPrincipal()).thenReturn(userDetails);
		when(userDetails.getUsername()).thenReturn(TEST_MEMBER_ID);
		when(authentication.isAuthenticated()).thenReturn(true);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
	}

	// ========== POST /api/orders/{memberId} - 일반 주문 생성 ==========

	@Test
	@DisplayName("일반 주문 생성 - 성공 (201 CREATED)")
	void createOrder_Success() throws Exception {
		// given
		CreateOrderRequest request = new CreateOrderRequest();
		request.setCartIds(Arrays.asList(1, 2, 3));
		request.setDeliveryName("홍길동");
		request.setDeliveryPhone("010-1234-5678");
		request.setDeliveryPostCode("12345");
		request.setDeliveryAddress("서울시 강남구");
		request.setDeliveryDetailAddress("101호");
		request.setDeliveryMessage("문 앞에 놓아주세요");
		request.setPaymentMethod("CARD");

		given(orderService.createOrder(
				eq(TEST_MEMBER_ID),
				anyList(),
				anyString(), anyString(), anyString(),
				anyString(), anyString(), anyString(), anyString()
		)).willReturn(TEST_ORDER_ID);

		// when & then
		mockMvc.perform(post("/api/orders/{memberId}", TEST_MEMBER_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("주문이 생성되었습니다."))
				.andExpect(jsonPath("$.data").value(TEST_ORDER_ID))
				.andExpect(jsonPath("$.timestamp").exists());

		then(orderService).should(times(1)).createOrder(
				eq(TEST_MEMBER_ID),
				eq(Arrays.asList(1, 2, 3)),
				eq("홍길동"),
				eq("010-1234-5678"),
				eq("12345"),
				eq("서울시 강남구"),
				eq("101호"),
				eq("문 앞에 놓아주세요"),
				eq("CARD")
		);
	}

	@Test
	@DisplayName("일반 주문 생성 - 유효성 검증 실패: 빈 cartIds (400 BAD_REQUEST)")
	void createOrder_ValidationFail_EmptyCartIds() throws Exception {
		// given
		CreateOrderRequest request = new CreateOrderRequest();
		request.setCartIds(Collections.emptyList()); // Empty list
		request.setDeliveryName("홍길동");
		request.setDeliveryPhone("010-1234-5678");
		request.setDeliveryPostCode("12345");
		request.setDeliveryAddress("서울시 강남구");
		request.setPaymentMethod("CARD");

		// when & then
		mockMvc.perform(post("/api/orders/{memberId}", TEST_MEMBER_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.code").value("C002"))
				.andExpect(jsonPath("$.message").value("잘못된 입력값입니다."));

		then(orderService).should(never()).createOrder(
				anyString(), anyList(), anyString(), anyString(), anyString(),
				anyString(), anyString(), anyString(), anyString()
		);
	}

	@Test
	@DisplayName("일반 주문 생성 - 유효성 검증 실패: 필수 필드 누락 (400 BAD_REQUEST)")
	void createOrder_ValidationFail_MissingRequiredFields() throws Exception {
		// given
		CreateOrderRequest request = new CreateOrderRequest();
		request.setCartIds(Arrays.asList(1, 2));
		// Missing deliveryName, deliveryPhone, etc.

		// when & then
		mockMvc.perform(post("/api/orders/{memberId}", TEST_MEMBER_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.code").value("C002"));
	}

	@Test
	@DisplayName("일반 주문 생성 - 비즈니스 예외: 재고 부족 (400 BAD_REQUEST)")
	void createOrder_BusinessException_OutOfStock() throws Exception {
		// given
		CreateOrderRequest request = new CreateOrderRequest();
		request.setCartIds(Arrays.asList(1, 2));
		request.setDeliveryName("홍길동");
		request.setDeliveryPhone("010-1234-5678");
		request.setDeliveryPostCode("12345");
		request.setDeliveryAddress("서울시 강남구");
		request.setPaymentMethod("CARD");

		given(orderService.createOrder(
				anyString(), anyList(), anyString(), anyString(), anyString(),
				anyString(), anyString(), anyString(), anyString()
		)).willThrow(new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "재고가 부족합니다."));

		// when & then
		mockMvc.perform(post("/api/orders/{memberId}", TEST_MEMBER_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.code").value("C002"))
				.andExpect(jsonPath("$.detail").value("재고가 부족합니다."));
	}

	// ========== POST /api/orders/{memberId}/gift - 선물 주문 생성 ==========

	@Test
	@DisplayName("선물 주문 생성 - 성공 (201 CREATED)")
	void createGiftOrder_Success() throws Exception {
		// given
		CreateGiftOrderRequest request = new CreateGiftOrderRequest();
		request.setCartId(1);
		request.setGiftMessage("생일 축하해요!");
		request.setPaymentMethod("CARD");

		given(orderService.createGiftOrder(
				eq(TEST_MEMBER_ID), eq(1), anyString(), anyString()
		)).willReturn(TEST_ORDER_ID);

		// when & then
		mockMvc.perform(post("/api/orders/{memberId}/gift", TEST_MEMBER_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("선물 주문이 생성되었습니다."))
				.andExpect(jsonPath("$.data").value(TEST_ORDER_ID));

		then(orderService).should(times(1)).createGiftOrder(
				eq(TEST_MEMBER_ID),
				eq(1),
				eq("생일 축하해요!"),
				eq("CARD")
		);
	}

	@Test
	@DisplayName("선물 주문 생성 - 유효성 검증 실패: 필수 필드 누락 (400 BAD_REQUEST)")
	void createGiftOrder_ValidationFail_MissingFields() throws Exception {
		// given
		CreateGiftOrderRequest request = new CreateGiftOrderRequest();
		request.setCartId(1);
		// Missing giftMessage and paymentMethod

		// when & then
		mockMvc.perform(post("/api/orders/{memberId}/gift", TEST_MEMBER_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.code").value("C002"));
	}

	// ========== GET /api/orders/{orderId} - 주문 조회 ==========

	@Test
	@DisplayName("주문 조회 - 성공 (200 OK)")
	void getOrder_Success() throws Exception {
		// given
		OrderDto orderDto = createOrderDto(TEST_ORDER_ID, TEST_MEMBER_ID);

		given(orderService.getOrderWithItems(TEST_ORDER_ID)).willReturn(orderDto);

		// when & then
		mockMvc.perform(get("/api/orders/{orderId}", TEST_ORDER_ID))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.orderId").value(TEST_ORDER_ID))
				.andExpect(jsonPath("$.data.memberId").value(TEST_MEMBER_ID))
				.andExpect(jsonPath("$.data.orderStatus").value("PENDING"));

		then(orderService).should(times(1)).getOrderWithItems(TEST_ORDER_ID);
	}

	@Test
	@DisplayName("주문 조회 - 주문 없음 (404 NOT_FOUND)")
	void getOrder_NotFound() throws Exception {
		// given
		given(orderService.getOrderWithItems(anyString()))
				.willThrow(new BusinessException(ErrorCode.ORDER_NOT_FOUND));

		// when & then
		mockMvc.perform(get("/api/orders/{orderId}", "INVALID-ORDER-ID"))
				.andDo(print())
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.code").value("O001"))
				.andExpect(jsonPath("$.message").value("주문을 찾을 수 없습니다."));
	}

	// ========== GET /api/orders/member/{memberId} - 회원 주문 목록 조회 ==========

	@Test
	@DisplayName("회원 주문 목록 조회 - 성공 (200 OK)")
	void getMemberOrders_Success() throws Exception {
		// given
		List<OrderDto> orders = Arrays.asList(
				createOrderDto("ORD-001", TEST_MEMBER_ID),
				createOrderDto("ORD-002", TEST_MEMBER_ID)
		);

		given(orderService.getMemberOrders(TEST_MEMBER_ID)).willReturn(orders);

		// when & then
		mockMvc.perform(get("/api/orders/member/{memberId}", TEST_MEMBER_ID))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data", hasSize(2)))
				.andExpect(jsonPath("$.data[0].orderId").value("ORD-001"))
				.andExpect(jsonPath("$.data[1].orderId").value("ORD-002"));

		then(orderService).should(times(1)).getMemberOrders(TEST_MEMBER_ID);
	}

	@Test
	@DisplayName("회원 주문 목록 조회 - 빈 목록 (200 OK)")
	void getMemberOrders_EmptyList() throws Exception {
		// given
		given(orderService.getMemberOrders(TEST_MEMBER_ID)).willReturn(Collections.emptyList());

		// when & then
		mockMvc.perform(get("/api/orders/member/{memberId}", TEST_MEMBER_ID))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data", hasSize(0)));
	}

	// ========== DELETE /api/orders/{orderId} - 주문 취소 ==========

	@Test
	@DisplayName("주문 취소 - 성공 (200 OK)")
	void cancelOrder_Success() throws Exception {
		// given
		willDoNothing().given(orderService).cancelOrder(TEST_MEMBER_ID, TEST_ORDER_ID);

		// when & then
		mockMvc.perform(delete("/api/orders/{orderId}", TEST_ORDER_ID)
						.param("memberId", TEST_MEMBER_ID))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("주문이 취소되었습니다."));

		then(orderService).should(times(1)).cancelOrder(TEST_MEMBER_ID, TEST_ORDER_ID);
	}

	@Test
	@DisplayName("주문 취소 - 주문 없음 (404 NOT_FOUND)")
	void cancelOrder_NotFound() throws Exception {
		// given
		willThrow(new BusinessException(ErrorCode.ORDER_NOT_FOUND))
				.given(orderService).cancelOrder(anyString(), anyString());

		// when & then
		mockMvc.perform(delete("/api/orders/{orderId}", "INVALID-ORDER-ID")
						.param("memberId", TEST_MEMBER_ID))
				.andDo(print())
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.code").value("O001"));
	}

	@Test
	@DisplayName("주문 취소 - 권한 없음 (403 FORBIDDEN)")
	void cancelOrder_Forbidden() throws Exception {
		// given
		willThrow(new BusinessException(ErrorCode.FORBIDDEN, "본인의 주문만 취소할 수 있습니다."))
				.given(orderService).cancelOrder(anyString(), anyString());

		// when & then
		mockMvc.perform(delete("/api/orders/{orderId}", TEST_ORDER_ID)
						.param("memberId", TEST_MEMBER_ID))
				.andDo(print())
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.code").value("AU005"))
				.andExpect(jsonPath("$.detail").value("본인의 주문만 취소할 수 있습니다."));
	}

	// ========== PUT /api/orders/{orderId}/delivery-address - 배송지 정보 업데이트 ==========

	@Test
	@DisplayName("배송지 정보 업데이트 - 성공 (200 OK)")
	void updateDeliveryAddress_Success() throws Exception {
		// given
		UpdateDeliveryAddressRequest request = new UpdateDeliveryAddressRequest();
		request.setDeliveryName("김철수");
		request.setDeliveryPhone("010-9876-5432");
		request.setDeliveryPostCode("54321");
		request.setDeliveryAddress("부산시 해운대구");
		request.setDeliveryDetailAddress("202호");
		request.setDeliveryMessage("경비실에 맡겨주세요");

		willDoNothing().given(orderService).updateDeliveryAddress(
				eq(TEST_MEMBER_ID), eq(TEST_ORDER_ID),
				anyString(), anyString(), anyString(), anyString(), anyString(), anyString()
		);

		// when & then
		mockMvc.perform(put("/api/orders/{orderId}/delivery-address", TEST_ORDER_ID)
						.param("memberId", TEST_MEMBER_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("배송지 정보가 업데이트되었습니다."));

		then(orderService).should(times(1)).updateDeliveryAddress(
				eq(TEST_MEMBER_ID), eq(TEST_ORDER_ID),
				eq("김철수"),
				eq("010-9876-5432"),
				eq("54321"),
				eq("부산시 해운대구"),
				eq("202호"),
				eq("경비실에 맡겨주세요")
		);
	}

	@Test
	@DisplayName("배송지 정보 업데이트 - 유효성 검증 실패 (400 BAD_REQUEST)")
	void updateDeliveryAddress_ValidationFail() throws Exception {
		// given
		UpdateDeliveryAddressRequest request = new UpdateDeliveryAddressRequest();
		// Missing required fields

		// when & then
		mockMvc.perform(put("/api/orders/{orderId}/delivery-address", TEST_ORDER_ID)
						.param("memberId", TEST_MEMBER_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.code").value("C002"));
	}

	// ========== Helper Methods ==========

	/**
	 * 테스트용 OrderDto 생성
	 */
	private OrderDto createOrderDto(String orderId, String memberId) {
		OrderDto orderDto = new OrderDto();
		orderDto.setOrderId(orderId);
		orderDto.setMemberId(memberId);
		orderDto.setOrderStatus("PENDING");
		orderDto.setTotalAmount(50000);
		orderDto.setDeliveryName("홍길동");
		orderDto.setDeliveryPhone("010-1234-5678");
		orderDto.setDeliveryPostCode("12345");
		orderDto.setDeliveryAddress("서울시 강남구");
		orderDto.setPaymentMethod("CARD");
		orderDto.setOrderItems(new ArrayList<>());

		return orderDto;
	}
}
