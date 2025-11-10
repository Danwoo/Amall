/**
 * 상품 관련 API (Phase 9-4)
 */

/**
 * 전체 상품 목록 조회
 *
 * @returns {Promise<Array>} 전체 상품 목록
 */
async function getAllProducts() {
	try {
		const response = await apiClient.get('/api/products');

		if (response.success) {
			return response.data;
		}

		return [];
	} catch (error) {
		console.error('상품 목록 조회 실패:', error);
		showToast('상품 목록을 불러오지 못했습니다.', 'error');
		return [];
	}
}

/**
 * 상품 번호로 상품 조회
 *
 * @param {number} prodNum - 상품 번호
 * @returns {Promise<Object|null>} 상품 정보
 */
async function getProduct(prodNum) {
	try {
		const response = await apiClient.get(`/api/products/${prodNum}`);

		if (response.success) {
			return response.data;
		}

		return null;
	} catch (error) {
		console.error('상품 조회 실패:', error);
		showToast(error.message || '상품을 찾을 수 없습니다.', 'error');
		return null;
	}
}

/**
 * 상품 코드로 상품 상세 조회 (옵션 포함)
 *
 * @param {string} prodCode - 상품 코드
 * @returns {Promise<Array>} 상품 상세 목록 (옵션별)
 */
async function getProductDetail(prodCode) {
	try {
		const response = await apiClient.get(`/api/products/code/${prodCode}`);

		if (response.success) {
			return response.data;
		}

		return [];
	} catch (error) {
		console.error('상품 상세 조회 실패:', error);
		showToast(error.message || '상품 상세 정보를 불러오지 못했습니다.', 'error');
		return [];
	}
}

/**
 * 카테고리별 상품 조회
 *
 * @param {string} category - 카테고리 (GIFT, COUPLE)
 * @returns {Promise<Array>} 카테고리별 상품 목록
 */
async function getProductsByCategory(category) {
	try {
		const response = await apiClient.get(`/api/products/category/${category}`);

		if (response.success) {
			return response.data;
		}

		return [];
	} catch (error) {
		console.error('카테고리별 상품 조회 실패:', error);
		showToast(error.message || '상품을 불러오지 못했습니다.', 'error');
		return [];
	}
}

/**
 * 메인 페이지 상품 조회 (GIFT + COUPLE)
 *
 * @returns {Promise<Object>} { giftProducts: [], coupleProducts: [] }
 */
async function getMainProducts() {
	try {
		const response = await apiClient.get('/api/products/main');

		if (response.success) {
			return response.data;
		}

		return { giftProducts: [], coupleProducts: [] };
	} catch (error) {
		console.error('메인 페이지 상품 조회 실패:', error);
		showToast('상품을 불러오지 못했습니다.', 'error');
		return { giftProducts: [], coupleProducts: [] };
	}
}

/**
 * 상품 HTML 렌더링 (공통 함수)
 *
 * @param {Object} product - 상품 객체
 * @param {Object} options - 렌더링 옵션
 * @returns {string} HTML 문자열
 */
function renderProductCard(product, options = {}) {
	const {
		showPrice = true,
		showStock = false,
		showAddToCart = false,
		showAddToWishList = false,
		clickable = true
	} = options;

	const priceHtml = showPrice
		? `<p class="product-price">${product.prodPrice ? product.prodPrice.toLocaleString() + '원' : '가격 미정'}</p>`
		: '';

	const stockHtml = showStock && product.prodStock !== undefined
		? `<p class="product-stock">재고: ${product.prodStock}개</p>`
		: '';

	const addToCartBtn = showAddToCart
		? `<button class="btn-add-to-cart" onclick="handleAddToCart(${product.prodNum})">장바구니</button>`
		: '';

	const addToWishListBtn = showAddToWishList
		? `<button class="btn-add-to-wishlist" onclick="handleAddToWishList(${product.prodNum})">❤️</button>`
		: '';

	const clickHandler = clickable
		? `onclick="goToProductDetail('${escapeAttribute(product.prodCode)}')"`
		: '';

	return `
		<div class="product-card" data-prod-num="${product.prodNum}" ${clickHandler}>
			<div class="product-image">
				<img src="${escapeAttribute(product.prodImage1 || '/img/default-product.png')}" alt="${escapeAttribute(product.prodName)}">
			</div>
			<div class="product-info">
				<h5 class="product-name">${escapeHtml(product.prodName)}</h5>
				<p class="product-seller">${escapeHtml(product.sellerId || '')}</p>
				${priceHtml}
				${stockHtml}
				<div class="product-actions">
					${addToCartBtn}
					${addToWishListBtn}
				</div>
			</div>
		</div>
	`;
}

/**
 * 상품 목록 렌더링
 *
 * @param {Array} products - 상품 배열
 * @param {HTMLElement} container - 컨테이너 DOM 요소
 * @param {Object} options - 렌더링 옵션
 */
function renderProducts(products, container, options = {}) {
	if (!container) {
		console.error('컨테이너를 찾을 수 없습니다.');
		return;
	}

	// 기존 내용 제거
	container.innerHTML = '';

	if (products.length === 0) {
		container.innerHTML = '<p class="no-products">상품이 없습니다.</p>';
		return;
	}

	// 상품 렌더링
	products.forEach(product => {
		const productHtml = renderProductCard(product, options);
		container.innerHTML += productHtml;
	});
}

/**
 * 상품 상세 페이지로 이동
 *
 * @param {string} prodCode - 상품 코드
 */
function goToProductDetail(prodCode) {
	window.location.href = `/storeDetail/amall.com?prodCode=${encodeURIComponent(prodCode)}`;
}

/**
 * 장바구니 추가 핸들러
 *
 * @param {number} prodNum - 상품 번호
 */
async function handleAddToCart(prodNum) {
	const memberId = getMemberIdFromPage();

	if (!memberId) {
		showToast('로그인이 필요합니다.', 'error');
		return;
	}

	// 수량 입력 받기 (간단한 prompt 사용, 추후 개선 가능)
	const quantity = prompt('수량을 입력하세요:', '1');

	if (quantity === null || quantity === '') {
		return; // 취소
	}

	const qty = parseInt(quantity);

	if (isNaN(qty) || qty <= 0) {
		showToast('올바른 수량을 입력해주세요.', 'error');
		return;
	}

	// 장바구니 추가
	const success = await addToCart(memberId, prodNum, qty);

	if (success) {
		const goToCart = await confirm('장바구니로 이동하시겠습니까?');
		if (goToCart) {
			window.location.href = '/bag/amall.com';
		}
	}
}

/**
 * 위시리스트 추가 핸들러
 *
 * @param {number} prodNum - 상품 번호
 */
async function handleAddToWishList(prodNum) {
	const memberId = getMemberIdFromPage();

	if (!memberId) {
		showToast('로그인이 필요합니다.', 'error');
		return;
	}

	const success = await addToWishList(memberId, prodNum);

	if (success) {
		// 위시리스트 아이콘 변경 등 UI 업데이트 가능
	}
}

// getMemberIdFromPage() is now in common-utils.js
