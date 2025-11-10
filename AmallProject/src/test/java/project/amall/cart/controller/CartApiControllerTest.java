package project.amall.cart.controller;

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
import project.amall.cart.dto.CartDto;
import project.amall.cart.dto.request.AddToCartAsGiftRequest;
import project.amall.cart.dto.request.AddToCartRequest;
import project.amall.cart.dto.request.SetGiftRequest;
import project.amall.cart.dto.request.UpdateCartQuantityRequest;
import project.amall.cart.service.CartService;
import project.amall.common.exception.BusinessException;
import project.amall.common.exception.GlobalExceptionHandler;
import project.amall.common.exception.code.ErrorCode;
import project.amall.product.dto.ProductDto;

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
 * CartApiController 통합 테스트
 *
 * Spring Boot의 @WebMvcTest를 사용하여 REST API 엔드포인트의
 * 전체 요청/응답 사이클을 검증합니다.
 */
@WebMvcTest(CartApiController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("CartApiController 통합 테스트")
class CartApiControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private CartService cartService;

	@MockBean
	private SecurityContext securityContext;

	@MockBean
	private Authentication authentication;

	@MockBean
	private UserDetails userDetails;

	private static final String TEST_MEMBER_ID = "testUser";
	private static final String OTHER_MEMBER_ID = "otherUser";
	private static final int TEST_CART_ID = 1;
	private static final int TEST_PRODUCT_NUM = 100;

	@BeforeEach
	void setUp() {
		// Mock Security Context for authentication
		when(authentication.getPrincipal()).thenReturn(userDetails);
		when(userDetails.getUsername()).thenReturn(TEST_MEMBER_ID);
		when(authentication.isAuthenticated()).thenReturn(true);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
	}

	// ========== GET /api/cart/{memberId} - 장바구니 조회 ==========

	@Test
	@DisplayName("장바구니 조회 - 성공 (200 OK)")
	void getCart_Success() throws Exception {
		// given
		List<CartDto> cartList = Arrays.asList(
				createCartDto(1, TEST_MEMBER_ID, 100, 2, false),
				createCartDto(2, TEST_MEMBER_ID, 200, 1, false)
		);

		given(cartService.showMyCart(TEST_MEMBER_ID)).willReturn(cartList);

		// when & then
		mockMvc.perform(get("/api/cart/{memberId}", TEST_MEMBER_ID))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data", hasSize(2)))
				.andExpect(jsonPath("$.data[0].cartId").value(1))
				.andExpect(jsonPath("$.data[0].quantity").value(2))
				.andExpect(jsonPath("$.data[1].cartId").value(2))
				.andExpect(jsonPath("$.data[1].quantity").value(1));

		then(cartService).should(times(1)).showMyCart(TEST_MEMBER_ID);
	}

	@Test
	@DisplayName("장바구니 조회 - 빈 장바구니 (200 OK)")
	void getCart_EmptyCart() throws Exception {
		// given
		given(cartService.showMyCart(TEST_MEMBER_ID)).willReturn(Collections.emptyList());

		// when & then
		mockMvc.perform(get("/api/cart/{memberId}", TEST_MEMBER_ID))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data", hasSize(0)));
	}

	// ========== POST /api/cart/{memberId}/items - 장바구니에 상품 추가 ==========

	@Test
	@DisplayName("장바구니에 상품 추가 - 성공 (201 CREATED)")
	void addToCart_Success() throws Exception {
		// given
		AddToCartRequest request = new AddToCartRequest();
		request.setProdNum(TEST_PRODUCT_NUM);
		request.setQuantity(3);

		willDoNothing().given(cartService).addToCart(TEST_MEMBER_ID, TEST_PRODUCT_NUM, 3);

		// when & then
		mockMvc.perform(post("/api/cart/{memberId}/items", TEST_MEMBER_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("장바구니에 추가되었습니다."));

		then(cartService).should(times(1)).addToCart(TEST_MEMBER_ID, TEST_PRODUCT_NUM, 3);
	}

	@Test
	@DisplayName("장바구니에 상품 추가 - 유효성 검증 실패: 수량 0 이하 (400 BAD_REQUEST)")
	void addToCart_ValidationFail_InvalidQuantity() throws Exception {
		// given
		AddToCartRequest request = new AddToCartRequest();
		request.setProdNum(TEST_PRODUCT_NUM);
		request.setQuantity(0); // Invalid: must be >= 1

		// when & then
		mockMvc.perform(post("/api/cart/{memberId}/items", TEST_MEMBER_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.code").value("C002"));

		then(cartService).should(never()).addToCart(anyString(), anyInt(), anyInt());
	}

	@Test
	@DisplayName("장바구니에 상품 추가 - 유효성 검증 실패: 필수 필드 누락 (400 BAD_REQUEST)")
	void addToCart_ValidationFail_MissingFields() throws Exception {
		// given
		AddToCartRequest request = new AddToCartRequest();
		// Missing prodNum and quantity

		// when & then
		mockMvc.perform(post("/api/cart/{memberId}/items", TEST_MEMBER_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.code").value("C002"));
	}

	@Test
	@DisplayName("장바구니에 상품 추가 - 비즈니스 예외: 상품 없음 (404 NOT_FOUND)")
	void addToCart_BusinessException_ProductNotFound() throws Exception {
		// given
		AddToCartRequest request = new AddToCartRequest();
		request.setProdNum(999); // Non-existent product
		request.setQuantity(1);

		willThrow(new BusinessException(ErrorCode.PRODUCT_NOT_FOUND))
				.given(cartService).addToCart(anyString(), anyInt(), anyInt());

		// when & then
		mockMvc.perform(post("/api/cart/{memberId}/items", TEST_MEMBER_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.code").value("P001"));
	}

	// ========== POST /api/cart/{memberId}/gift-items - 선물로 장바구니 추가 ==========

	@Test
	@DisplayName("선물로 장바구니 추가 - 성공 (201 CREATED)")
	void addToCartAsGift_Success() throws Exception {
		// given
		AddToCartAsGiftRequest request = new AddToCartAsGiftRequest();
		request.setProdNum(TEST_PRODUCT_NUM);
		request.setQuantity(1);
		request.setGiftToMemberId(OTHER_MEMBER_ID);
		request.setGiftMessage("생일 축하해요!");

		willDoNothing().given(cartService).addToCartAsGift(
				TEST_MEMBER_ID, TEST_PRODUCT_NUM, 1, OTHER_MEMBER_ID, "생일 축하해요!"
		);

		// when & then
		mockMvc.perform(post("/api/cart/{memberId}/gift-items", TEST_MEMBER_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("선물이 장바구니에 추가되었습니다."));

		then(cartService).should(times(1)).addToCartAsGift(
				TEST_MEMBER_ID, TEST_PRODUCT_NUM, 1, OTHER_MEMBER_ID, "생일 축하해요!"
		);
	}

	@Test
	@DisplayName("선물로 장바구니 추가 - 유효성 검증 실패 (400 BAD_REQUEST)")
	void addToCartAsGift_ValidationFail() throws Exception {
		// given
		AddToCartAsGiftRequest request = new AddToCartAsGiftRequest();
		request.setProdNum(TEST_PRODUCT_NUM);
		request.setQuantity(1);
		// Missing giftToMemberId

		// when & then
		mockMvc.perform(post("/api/cart/{memberId}/gift-items", TEST_MEMBER_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.code").value("C002"));
	}

	// ========== PATCH /api/cart/{memberId}/items/{cartId}/quantity - 수량 수정 ==========

	@Test
	@DisplayName("장바구니 수량 수정 - 성공 (200 OK)")
	void updateQuantity_Success() throws Exception {
		// given
		UpdateCartQuantityRequest request = new UpdateCartQuantityRequest();
		request.setQuantity(5);

		willDoNothing().given(cartService).updateCartQuantity(TEST_CART_ID, 5);

		// when & then
		mockMvc.perform(patch("/api/cart/{memberId}/items/{cartId}/quantity", TEST_MEMBER_ID, TEST_CART_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("수량이 수정되었습니다."));

		then(cartService).should(times(1)).updateCartQuantity(TEST_CART_ID, 5);
	}

	@Test
	@DisplayName("장바구니 수량 수정 - 유효성 검증 실패: 수량 0 이하 (400 BAD_REQUEST)")
	void updateQuantity_ValidationFail() throws Exception {
		// given
		UpdateCartQuantityRequest request = new UpdateCartQuantityRequest();
		request.setQuantity(0); // Invalid

		// when & then
		mockMvc.perform(patch("/api/cart/{memberId}/items/{cartId}/quantity", TEST_MEMBER_ID, TEST_CART_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.code").value("C002"));
	}

	@Test
	@DisplayName("장바구니 수량 수정 - 장바구니 항목 없음 (404 NOT_FOUND)")
	void updateQuantity_NotFound() throws Exception {
		// given
		UpdateCartQuantityRequest request = new UpdateCartQuantityRequest();
		request.setQuantity(3);

		willThrow(new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND))
				.given(cartService).updateCartQuantity(anyInt(), anyInt());

		// when & then
		mockMvc.perform(patch("/api/cart/{memberId}/items/{cartId}/quantity", TEST_MEMBER_ID, 999)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.code").value("CR002"));
	}

	// ========== PUT /api/cart/{memberId}/items/{cartId}/gift - 선물로 설정 ==========

	@Test
	@DisplayName("장바구니 아이템을 선물로 설정 - 성공 (200 OK)")
	void setAsGift_Success() throws Exception {
		// given
		SetGiftRequest request = new SetGiftRequest();
		request.setGiftToMemberId(OTHER_MEMBER_ID);
		request.setGiftMessage("축하해요!");

		willDoNothing().given(cartService).setAsGift(TEST_CART_ID, OTHER_MEMBER_ID, "축하해요!");

		// when & then
		mockMvc.perform(put("/api/cart/{memberId}/items/{cartId}/gift", TEST_MEMBER_ID, TEST_CART_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("선물로 설정되었습니다."));

		then(cartService).should(times(1)).setAsGift(TEST_CART_ID, OTHER_MEMBER_ID, "축하해요!");
	}

	// ========== DELETE /api/cart/{memberId}/items/{cartId}/gift - 일반 구매로 변경 ==========

	@Test
	@DisplayName("장바구니 아이템을 일반 구매로 변경 - 성공 (200 OK)")
	void setAsNormalPurchase_Success() throws Exception {
		// given
		willDoNothing().given(cartService).setAsNormalPurchase(TEST_CART_ID);

		// when & then
		mockMvc.perform(delete("/api/cart/{memberId}/items/{cartId}/gift", TEST_MEMBER_ID, TEST_CART_ID))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("일반 구매로 변경되었습니다."));

		then(cartService).should(times(1)).setAsNormalPurchase(TEST_CART_ID);
	}

	// ========== DELETE /api/cart/{memberId}/items/{cartId} - 장바구니 아이템 삭제 ==========

	@Test
	@DisplayName("장바구니 아이템 삭제 - 성공 (200 OK)")
	void removeItem_Success() throws Exception {
		// given
		willDoNothing().given(cartService).removeFromCart(TEST_CART_ID);

		// when & then
		mockMvc.perform(delete("/api/cart/{memberId}/items/{cartId}", TEST_MEMBER_ID, TEST_CART_ID))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("장바구니에서 삭제되었습니다."));

		then(cartService).should(times(1)).removeFromCart(TEST_CART_ID);
	}

	// ========== DELETE /api/cart/{memberId} - 장바구니 전체 삭제 ==========

	@Test
	@DisplayName("장바구니 전체 삭제 - 성공 (200 OK)")
	void clearCart_Success() throws Exception {
		// given
		willDoNothing().given(cartService).clearCart(TEST_MEMBER_ID);

		// when & then
		mockMvc.perform(delete("/api/cart/{memberId}", TEST_MEMBER_ID))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("장바구니가 비워졌습니다."));

		then(cartService).should(times(1)).clearCart(TEST_MEMBER_ID);
	}

	// ========== Helper Methods ==========

	/**
	 * 테스트용 CartDto 생성
	 */
	private CartDto createCartDto(int cartId, String memberId, int prodNum, int quantity, boolean isGift) {
		CartDto cartDto = new CartDto();
		cartDto.setCartId(cartId);
		cartDto.setMemberId(memberId);
		cartDto.setProdNum(prodNum);
		cartDto.setQuantity(quantity);
		cartDto.setIsGift(isGift);

		// 상품 정보 설정
		ProductDto productDto = new ProductDto();
		productDto.setProdNum(prodNum);
		productDto.setProdName("테스트 상품");
		productDto.setProdPrice(10000);
		productDto.setProdStock(100);
		cartDto.setProductDto(productDto);

		return cartDto;
	}
}
