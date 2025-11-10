package project.amall.product.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import project.amall.product.service.ProductService;

/**
 * 상품 뷰 컨트롤러 (레거시)
 *
 * Thymeleaf 뷰 렌더링을 담당하는 컨트롤러
 * 현재는 AmallController가 상품 관련 뷰를 처리하고 있음
 *
 * RESTful API는 ProductApiController 사용 권장
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;

	// AmallController에서 상품 관련 뷰 처리 중
	// - /amall.store.com: 전체 상품 목록
	// - /amall.storedetail.com: 상품 상세
	// - /amall.com: 메인 페이지 (GIFT, COUPLE 상품)

	// 필요시 여기에 추가 상품 뷰 메서드 구현
}
