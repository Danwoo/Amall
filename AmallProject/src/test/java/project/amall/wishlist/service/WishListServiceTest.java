package project.amall.wishlist.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import project.amall.common.exception.BusinessException;
import project.amall.common.exception.code.ErrorCode;
import project.amall.product.dto.ProductDto;
import project.amall.product.mapper.ProductMapper;
import project.amall.wishlist.dto.WishListDto;
import project.amall.wishlist.mapper.WishListMapper;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * WishListService 테스트
 *
 * Phase 6-2에서 추가된 비즈니스 로직 검증
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WishListService 테스트")
class WishListServiceTest {

	@InjectMocks
	private WishListService wishListService;

	@Mock
	private WishListMapper wishListMapper;

	@Mock
	private ProductMapper productMapper;

	// ========== addWishList 테스트 ==========

	@Test
	@DisplayName("위시리스트 추가 - 정상")
	void addWishList_Success() {
		// given
		String memberId = "testUser";
		int prodNum = 1;

		ProductDto product = createProduct(prodNum);
		given(productMapper.getProductByNum(prodNum)).willReturn(product);
		given(wishListMapper.findByMemberAndProduct(memberId, prodNum)).willReturn(null);
		given(wishListMapper.addWishList(any(WishListDto.class))).willReturn(1);

		// when
		wishListService.addWishList(memberId, prodNum);

		// then
		then(productMapper).should(times(1)).getProductByNum(prodNum);
		then(wishListMapper).should(times(1)).findByMemberAndProduct(memberId, prodNum);
		then(wishListMapper).should(times(1)).addWishList(any(WishListDto.class));
	}

	@Test
	@DisplayName("위시리스트 추가 - 존재하지 않는 상품일 때 예외")
	void addWishList_ProductNotFound() {
		// given
		String memberId = "testUser";
		int prodNum = 999;

		given(productMapper.getProductByNum(prodNum)).willReturn(null);

		// when & then
		assertThatThrownBy(() -> wishListService.addWishList(memberId, prodNum))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> {
					BusinessException be = (BusinessException) e;
					assertThat(be.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
				});
	}

	@Test
	@DisplayName("위시리스트 추가 - 이미 있는 상품일 때 예외")
	void addWishList_DuplicateProduct() {
		// given
		String memberId = "testUser";
		int prodNum = 1;

		ProductDto product = createProduct(prodNum);
		WishListDto existingWishList = createWishList(1, memberId, prodNum);

		given(productMapper.getProductByNum(prodNum)).willReturn(product);
		given(wishListMapper.findByMemberAndProduct(memberId, prodNum)).willReturn(existingWishList);

		// when & then
		assertThatThrownBy(() -> wishListService.addWishList(memberId, prodNum))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> {
					BusinessException be = (BusinessException) e;
					assertThat(be.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
					assertThat(be.getMessage()).contains("이미 위시리스트에 추가된 상품입니다");
				});

		then(wishListMapper).should(never()).addWishList(any(WishListDto.class));
	}

	@Test
	@DisplayName("위시리스트 추가 - DB 저장 실패 시 예외")
	void addWishList_InsertFailed() {
		// given
		String memberId = "testUser";
		int prodNum = 1;

		ProductDto product = createProduct(prodNum);
		given(productMapper.getProductByNum(prodNum)).willReturn(product);
		given(wishListMapper.findByMemberAndProduct(memberId, prodNum)).willReturn(null);
		given(wishListMapper.addWishList(any(WishListDto.class))).willReturn(0); // 실패

		// when & then
		assertThatThrownBy(() -> wishListService.addWishList(memberId, prodNum))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> {
					BusinessException be = (BusinessException) e;
					assertThat(be.getErrorCode()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
				});
	}

	// ========== removeWishList 테스트 ==========

	@Test
	@DisplayName("위시리스트 삭제 - 정상")
	void removeWishList_Success() {
		// given
		int wishlistId = 1;

		given(wishListMapper.removeWishList(wishlistId)).willReturn(1);

		// when
		wishListService.removeWishList(wishlistId);

		// then
		then(wishListMapper).should(times(1)).removeWishList(wishlistId);
	}

	@Test
	@DisplayName("위시리스트 삭제 - 존재하지 않는 항목일 때 예외")
	void removeWishList_NotFound() {
		// given
		int wishlistId = 999;

		given(wishListMapper.removeWishList(wishlistId)).willReturn(0);

		// when & then
		assertThatThrownBy(() -> wishListService.removeWishList(wishlistId))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> {
					BusinessException be = (BusinessException) e;
					assertThat(be.getErrorCode()).isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
				});
	}

	// ========== clearWishList 테스트 ==========

	@Test
	@DisplayName("위시리스트 전체 삭제 - 정상")
	void clearWishList_Success() {
		// given
		String memberId = "testUser";

		given(wishListMapper.clearWishList(memberId)).willReturn(3); // 3개 삭제

		// when
		wishListService.clearWishList(memberId);

		// then
		then(wishListMapper).should(times(1)).clearWishList(memberId);
	}

	@Test
	@DisplayName("위시리스트 전체 삭제 - 항목이 없어도 예외 발생 안 함")
	void clearWishList_EmptyList() {
		// given
		String memberId = "testUser";

		given(wishListMapper.clearWishList(memberId)).willReturn(0); // 삭제할 항목 없음

		// when & then (예외 발생하지 않아야 함)
		assertThatCode(() -> wishListService.clearWishList(memberId))
				.doesNotThrowAnyException();

		then(wishListMapper).should(times(1)).clearWishList(memberId);
	}

	// ========== showThisIdWishList 테스트 ==========

	@Test
	@DisplayName("위시리스트 조회 - 정상")
	void showThisIdWishList_Success() {
		// given
		String memberId = "testUser";
		List<ProductDto> expectedProducts = Arrays.asList(
				createProduct(1),
				createProduct(2),
				createProduct(3)
		);

		given(wishListMapper.showThisIdWishList(memberId)).willReturn(expectedProducts);

		// when
		List<ProductDto> result = wishListService.showThisIdWishList(memberId);

		// then
		assertThat(result).hasSize(3);
		assertThat(result).isEqualTo(expectedProducts);
		then(wishListMapper).should(times(1)).showThisIdWishList(memberId);
	}

	@Test
	@DisplayName("위시리스트 조회 - 빈 목록")
	void showThisIdWishList_EmptyList() {
		// given
		String memberId = "testUser";

		given(wishListMapper.showThisIdWishList(memberId)).willReturn(Arrays.asList());

		// when
		List<ProductDto> result = wishListService.showThisIdWishList(memberId);

		// then
		assertThat(result).isEmpty();
		then(wishListMapper).should(times(1)).showThisIdWishList(memberId);
	}

	// ========== isInWishList 테스트 ==========

	@Test
	@DisplayName("위시리스트 포함 여부 확인 - 있을 때 true")
	void isInWishList_True() {
		// given
		String memberId = "testUser";
		int prodNum = 1;

		WishListDto wishList = createWishList(1, memberId, prodNum);
		given(wishListMapper.findByMemberAndProduct(memberId, prodNum)).willReturn(wishList);

		// when
		boolean result = wishListService.isInWishList(memberId, prodNum);

		// then
		assertThat(result).isTrue();
		then(wishListMapper).should(times(1)).findByMemberAndProduct(memberId, prodNum);
	}

	@Test
	@DisplayName("위시리스트 포함 여부 확인 - 없을 때 false")
	void isInWishList_False() {
		// given
		String memberId = "testUser";
		int prodNum = 1;

		given(wishListMapper.findByMemberAndProduct(memberId, prodNum)).willReturn(null);

		// when
		boolean result = wishListService.isInWishList(memberId, prodNum);

		// then
		assertThat(result).isFalse();
		then(wishListMapper).should(times(1)).findByMemberAndProduct(memberId, prodNum);
	}

	// ========== Helper Methods ==========

	private ProductDto createProduct(int prodNum) {
		ProductDto product = new ProductDto();
		product.setProdNum(prodNum);
		product.setProdName("Test Product " + prodNum);
		product.setProdPrice(10000);
		product.setProdStock(100);
		return product;
	}

	private WishListDto createWishList(int wishlistId, String memberId, int prodNum) {
		WishListDto wishList = new WishListDto();
		wishList.setWishlistId(wishlistId);
		wishList.setMemberId(memberId);
		wishList.setProdNum(prodNum);
		return wishList;
	}
}
