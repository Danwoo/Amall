package project.amall.product.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import project.amall.product.dto.ProductDto;

/**
 * 상품 Mapper
 */
@Mapper
public interface ProductMapper {

	/**
	 * 전체 상품 목록 조회
	 */
	List<ProductDto> showAllProductStore();

	/**
	 * 상품 상세 조회 (상품 코드로 조회)
	 *
	 * @param prodCode 상품 코드
	 */
	List<ProductDto> showProductDetail(String prodCode);

	/**
	 * 메인 페이지 상품 조회
	 *
	 * @param needCategory 카테고리 (GIFT, COUPLE)
	 */
	List<ProductDto> showMain(String needCategory);

	/**
	 * 상품 번호로 상품 조회
	 *
	 * @param prodNum 상품 번호
	 * @return 상품 정보 (없으면 null)
	 */
	ProductDto getProductByNum(int prodNum);
}