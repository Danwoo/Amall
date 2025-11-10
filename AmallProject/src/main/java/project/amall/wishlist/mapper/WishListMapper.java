package project.amall.wishlist.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import project.amall.product.dto.ProductDto;
import project.amall.wishlist.dto.WishListDto;

/**
 * 위시리스트 Mapper
 */
@Mapper
public interface WishListMapper {

	/**
	 * 회원의 위시리스트 조회
	 *
	 * @param memberId 회원 ID
	 * @return 위시리스트 상품 목록 (최대 4개)
	 */
	List<ProductDto> showThisIdWishList(String memberId);

	/**
	 * 위시리스트에 상품 추가
	 *
	 * @param wishListDto 위시리스트 정보
	 * @return 추가된 행 수
	 */
	int addWishList(WishListDto wishListDto);

	/**
	 * 위시리스트에서 상품 삭제
	 *
	 * @param wishlistId 위시리스트 ID
	 * @return 삭제된 행 수
	 */
	int removeWishList(int wishlistId);

	/**
	 * 회원의 위시리스트 전체 삭제
	 *
	 * @param memberId 회원 ID
	 * @return 삭제된 행 수
	 */
	int clearWishList(String memberId);

	/**
	 * 특정 회원의 특정 상품이 위시리스트에 있는지 확인
	 *
	 * @param memberId 회원 ID
	 * @param prodNum 상품 번호
	 * @return 위시리스트 정보 (없으면 null)
	 */
	WishListDto findByMemberAndProduct(@Param("memberId") String memberId, @Param("prodNum") int prodNum);
}
