package project.amall.product.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.amall.common.constant.ProductCategory;
import project.amall.common.response.ApiResponse;
import project.amall.product.dto.ProductDto;
import project.amall.product.dto.response.ProductResponse;
import project.amall.product.service.ProductService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 상품 관리 RESTful API Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductApiController {

    private final ProductService productService;

    /**
     * 전체 상품 목록 조회
     *
     * GET /api/products
     *
     * @return 전체 상품 목록
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts() {
        log.info("전체 상품 목록 조회 요청");

        List<ProductDto> products = productService.showAllProductStore();
        List<ProductResponse> response = products.stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 상품 상세 조회 (상품 코드)
     *
     * GET /api/products/code/{prodCode}
     *
     * @param prodCode 상품 코드
     * @return 상품 상세 정보
     */
    @GetMapping("/code/{prodCode}")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductByCode(
            @PathVariable String prodCode) {

        log.info("상품 상세 조회: prodCode={}", prodCode);

        List<ProductDto> products = productService.showProductDetail(prodCode);
        List<ProductResponse> response = products.stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 상품 조회 (상품 번호)
     *
     * GET /api/products/{prodNum}
     *
     * @param prodNum 상품 번호
     * @return 상품 정보
     */
    @GetMapping("/{prodNum}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(
            @PathVariable int prodNum) {

        log.info("상품 조회: prodNum={}", prodNum);

        ProductDto product = productService.getProductByNum(prodNum);
        ProductResponse response = ProductResponse.from(product);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 카테고리별 상품 조회
     *
     * GET /api/products/category/{category}
     *
     * @param category 카테고리 (GIFT, COUPLE)
     * @return 해당 카테고리 상품 목록
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductsByCategory(
            @PathVariable String category) {

        log.info("카테고리별 상품 조회: category={}", category);

        // Enum으로 변환
        ProductCategory productCategory = ProductCategory.fromCode(category);

        List<ProductDto> products = productService.showMainByCategory(productCategory);
        List<ProductResponse> response = products.stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 메인 페이지 상품 조회 (GIFT, COUPLE 모두)
     *
     * GET /api/products/main
     *
     * @return GIFT와 COUPLE 카테고리 상품
     */
    @GetMapping("/main")
    public ResponseEntity<ApiResponse<MainProductsResponse>> getMainProducts() {
        log.info("메인 페이지 상품 조회");

        List<ProductDto> giftProducts = productService.showMainByCategory(ProductCategory.GIFT);
        List<ProductDto> coupleProducts = productService.showMainByCategory(ProductCategory.COUPLE);

        List<ProductResponse> giftResponse = giftProducts.stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());

        List<ProductResponse> coupleResponse = coupleProducts.stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());

        MainProductsResponse response = new MainProductsResponse(giftResponse, coupleResponse);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 메인 페이지 상품 응답 DTO
     */
    public record MainProductsResponse(
            List<ProductResponse> giftProducts,
            List<ProductResponse> coupleProducts
    ) {}
}
