package project.amall.cart.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import project.amall.cart.dto.CartDto;
import project.amall.cart.mapper.CartMapper;
import project.amall.common.exception.BusinessException;
import project.amall.common.exception.code.ErrorCode;
import project.amall.product.dto.ProductDto;
import project.amall.product.mapper.ProductMapper;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * CartService 테스트
 *
 * Phase 6-2에서 추가된 비즈니스 로직 검증
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CartService 테스트")
class CartServiceTest {

	@InjectMocks
	private CartService cartService;

	@Mock
	private CartMapper cartMapper;

	@Mock
	private ProductMapper productMapper;

	// ========== addToCart 테스트 ==========

	@Test
	@DisplayName("장바구니 추가 - 정상")
	void addToCart_Success() {
		// given
		String memberId = "testUser";
		int prodNum = 1;
		int quantity = 2;

		ProductDto product = createProduct(prodNum, 10); // 재고 10개
		given(productMapper.getProductByNum(prodNum)).willReturn(product);
		given(cartMapper.findByMemberAndProduct(memberId, prodNum)).willReturn(null);
		given(cartMapper.addToCart(any(CartDto.class))).willReturn(1);

		// when
		cartService.addToCart(memberId, prodNum, quantity);

		// then
		then(productMapper).should(times(1)).getProductByNum(prodNum);
		then(cartMapper).should(times(1)).findByMemberAndProduct(memberId, prodNum);
		then(cartMapper).should(times(1)).addToCart(any(CartDto.class));
	}

	@Test
	@DisplayName("장바구니 추가 - 수량이 0 이하일 때 예외")
	void addToCart_InvalidQuantity() {
		// given
		String memberId = "testUser";
		int prodNum = 1;
		int invalidQuantity = 0;

		// when & then
		assertThatThrownBy(() -> cartService.addToCart(memberId, prodNum, invalidQuantity))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("수량은 1 이상이어야 합니다");
	}

	@Test
	@DisplayName("장바구니 추가 - 존재하지 않는 상품일 때 예외")
	void addToCart_ProductNotFound() {
		// given
		String memberId = "testUser";
		int prodNum = 999;
		int quantity = 2;

		given(productMapper.getProductByNum(prodNum)).willReturn(null);

		// when & then
		assertThatThrownBy(() -> cartService.addToCart(memberId, prodNum, quantity))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> {
					BusinessException be = (BusinessException) e;
					assertThat(be.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
				});
	}

	@Test
	@DisplayName("장바구니 추가 - 재고보다 많은 수량 요청 시 예외")
	void addToCart_ExceedStock() {
		// given
		String memberId = "testUser";
		int prodNum = 1;
		int quantity = 20; // 재고는 10개

		ProductDto product = createProduct(prodNum, 10); // 재고 10개
		given(productMapper.getProductByNum(prodNum)).willReturn(product);

		// when & then
		assertThatThrownBy(() -> cartService.addToCart(memberId, prodNum, quantity))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("재고 부족");
	}

	@Test
	@DisplayName("장바구니 추가 - 이미 있는 상품은 수량 증가")
	void addToCart_ExistingProduct_IncreaseQuantity() {
		// given
		String memberId = "testUser";
		int prodNum = 1;
		int quantity = 3;

		ProductDto product = createProduct(prodNum, 20);
		CartDto existingCart = createCart(1, memberId, prodNum, 5); // 기존 수량 5개

		given(productMapper.getProductByNum(prodNum)).willReturn(product);
		given(cartMapper.findByMemberAndProduct(memberId, prodNum)).willReturn(existingCart);
		given(cartMapper.updateCartQuantity(existingCart.getCartId(), 8)).willReturn(1); // 5 + 3 = 8

		// when
		cartService.addToCart(memberId, prodNum, quantity);

		// then
		then(cartMapper).should(times(1)).updateCartQuantity(existingCart.getCartId(), 8);
		then(cartMapper).should(never()).addToCart(any(CartDto.class)); // 새로 추가하지 않음
	}

	@Test
	@DisplayName("장바구니 추가 - 기존 수량 + 추가 수량이 재고 초과 시 예외")
	void addToCart_ExistingProduct_ExceedStockAfterIncrease() {
		// given
		String memberId = "testUser";
		int prodNum = 1;
		int quantity = 10;

		ProductDto product = createProduct(prodNum, 12); // 재고 12개
		CartDto existingCart = createCart(1, memberId, prodNum, 5); // 기존 5개

		given(productMapper.getProductByNum(prodNum)).willReturn(product);
		given(cartMapper.findByMemberAndProduct(memberId, prodNum)).willReturn(existingCart);

		// when & then (5 + 10 = 15 > 12 재고)
		assertThatThrownBy(() -> cartService.addToCart(memberId, prodNum, quantity))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("재고 부족");
	}

	// ========== updateCartQuantity 테스트 ==========

	@Test
	@DisplayName("장바구니 수량 수정 - 정상")
	void updateCartQuantity_Success() {
		// given
		int cartId = 1;
		int newQuantity = 5;

		given(cartMapper.updateCartQuantity(cartId, newQuantity)).willReturn(1);

		// when
		cartService.updateCartQuantity(cartId, newQuantity);

		// then
		then(cartMapper).should(times(1)).updateCartQuantity(cartId, newQuantity);
	}

	@Test
	@DisplayName("장바구니 수량 수정 - 수량이 0 이하일 때 예외")
	void updateCartQuantity_InvalidQuantity() {
		// given
		int cartId = 1;
		int invalidQuantity = 0;

		// when & then
		assertThatThrownBy(() -> cartService.updateCartQuantity(cartId, invalidQuantity))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("수량은 1 이상이어야 합니다");
	}

	@Test
	@DisplayName("장바구니 수량 수정 - 존재하지 않는 항목일 때 예외")
	void updateCartQuantity_NotFound() {
		// given
		int cartId = 999;
		int quantity = 5;

		given(cartMapper.updateCartQuantity(cartId, quantity)).willReturn(0); // 업데이트 실패

		// when & then
		assertThatThrownBy(() -> cartService.updateCartQuantity(cartId, quantity))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> {
					BusinessException be = (BusinessException) e;
					assertThat(be.getErrorCode()).isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
				});
	}

	// ========== removeFromCart 테스트 ==========

	@Test
	@DisplayName("장바구니 삭제 - 정상")
	void removeFromCart_Success() {
		// given
		int cartId = 1;

		given(cartMapper.removeFromCart(cartId)).willReturn(1);

		// when
		cartService.removeFromCart(cartId);

		// then
		then(cartMapper).should(times(1)).removeFromCart(cartId);
	}

	@Test
	@DisplayName("장바구니 삭제 - 존재하지 않는 항목일 때 예외")
	void removeFromCart_NotFound() {
		// given
		int cartId = 999;

		given(cartMapper.removeFromCart(cartId)).willReturn(0);

		// when & then
		assertThatThrownBy(() -> cartService.removeFromCart(cartId))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> {
					BusinessException be = (BusinessException) e;
					assertThat(be.getErrorCode()).isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
				});
	}

	// ========== clearCart 테스트 ==========

	@Test
	@DisplayName("장바구니 전체 삭제 - 정상")
	void clearCart_Success() {
		// given
		String memberId = "testUser";

		given(cartMapper.clearCart(memberId)).willReturn(5); // 5개 삭제

		// when
		cartService.clearCart(memberId);

		// then
		then(cartMapper).should(times(1)).clearCart(memberId);
	}

	// ========== showMyCart 테스트 ==========

	@Test
	@DisplayName("장바구니 조회 - 정상")
	void showMyCart_Success() {
		// given
		String memberId = "testUser";
		List<CartDto> expectedCarts = Arrays.asList(
				createCart(1, memberId, 1, 2),
				createCart(2, memberId, 2, 3)
		);

		given(cartMapper.showMyCart(memberId)).willReturn(expectedCarts);

		// when
		List<CartDto> result = cartService.showMyCart(memberId);

		// then
		assertThat(result).hasSize(2);
		assertThat(result).isEqualTo(expectedCarts);
		then(cartMapper).should(times(1)).showMyCart(memberId);
	}

	// ========== Helper Methods ==========

	private ProductDto createProduct(int prodNum, int stock) {
		ProductDto product = new ProductDto();
		product.setProdNum(prodNum);
		product.setProdName("Test Product " + prodNum);
		product.setProdStock(stock);
		product.setProdPrice(10000);
		return product;
	}

	private CartDto createCart(int cartId, String memberId, int prodNum, int quantity) {
		CartDto cart = new CartDto();
		cart.setCartId(cartId);
		cart.setMemberId(memberId);
		cart.setProdNum(prodNum);
		cart.setCartQuantity(quantity);
		return cart;
	}
}
