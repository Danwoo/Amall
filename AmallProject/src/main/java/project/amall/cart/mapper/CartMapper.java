package project.amall.cart.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import project.amall.cart.dto.CartDto;

/**
 * 장바구니 Mapper
 */
@Mapper
public interface CartMapper {

	/**
	 * 회원의 장바구니 조회
	 *
	 * @param memberId 회원 ID
	 * @return 장바구니 목록 (상품 정보 포함)
	 */
	List<CartDto> showMyCart(String memberId);

	/**
	 * 장바구니에 상품 추가
	 *
	 * @param cartDto 장바구니 정보
	 * @return 추가된 행 수
	 */
	int addToCart(CartDto cartDto);

	/**
	 * 장바구니 상품 수량 수정
	 *
	 * @param cartId 장바구니 ID
	 * @param quantity 수량
	 * @return 수정된 행 수
	 */
	int updateCartQuantity(@Param("cartId") int cartId, @Param("quantity") int quantity);

	/**
	 * 장바구니 상품 삭제
	 *
	 * @param cartId 장바구니 ID
	 * @return 삭제된 행 수
	 */
	int removeFromCart(int cartId);

	/**
	 * 회원의 장바구니 전체 삭제
	 *
	 * @param memberId 회원 ID
	 * @return 삭제된 행 수
	 */
	int clearCart(String memberId);

	/**
	 * 특정 회원의 특정 상품이 장바구니에 있는지 확인
	 *
	 * @param memberId 회원 ID
	 * @param prodNum 상품 번호
	 * @return 장바구니 정보 (없으면 null)
	 */
	CartDto findByMemberAndProduct(@Param("memberId") String memberId, @Param("prodNum") int prodNum);
}
