package project.amall.product.service;

import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.amall.common.constant.ProductCategory;
import project.amall.common.exception.BusinessException;
import project.amall.common.exception.code.ErrorCode;
import project.amall.product.dto.ProductDto;
import project.amall.product.mapper.ProductMapper;

/**
 * 상품 관련 비즈니스 로직 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

	private final ProductMapper productMapper;

	/**
	 * 전체 상품 목록 조회
	 */
	public List<ProductDto> showAllProductStore() {
		log.debug("전체 상품 목록 조회");
		return productMapper.showAllProductStore();
	}

	/**
	 * 상품 상세 조회
	 */
	public List<ProductDto> showProductDetail(String prodCode) {
		log.debug("상품 상세 조회: prodCode={}", prodCode);

		List<ProductDto> products = productMapper.showProductDetail(prodCode);

		if (products == null || products.isEmpty()) {
			throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "prodCode=" + prodCode);
		}

		return products;
	}

	/**
	 * 메인 페이지 상품 조회
	 * @deprecated ProductCategory Enum 사용 권장
	 */
	@Deprecated
	public List<ProductDto> showMain(String needCategory) {
		log.debug("메인 상품 조회: category={}", needCategory);
		return productMapper.showMain(needCategory);
	}

	/**
	 * 메인 페이지 상품 조회 (Enum 사용)
	 */
	public List<ProductDto> showMainByCategory(ProductCategory category) {
		log.debug("메인 상품 조회: category={}", category.getCode());
		return productMapper.showMain(category.getCode());
	}

	/**
	 * 상품 번호로 조회
	 */
	public ProductDto getProductByNum(int prodNum) {
		log.debug("상품 조회: prodNum={}", prodNum);

		ProductDto product = productMapper.getProductByNum(prodNum);

		if (product == null) {
			throw new BusinessException(
					ErrorCode.PRODUCT_NOT_FOUND,
					"prodNum=" + prodNum
			);
		}

		return product;
	}
}
