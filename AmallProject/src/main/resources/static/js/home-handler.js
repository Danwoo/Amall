/**
 * Home 페이지 핸들러 (Phase 9: API 통합)
 *
 * / 또는 /home/amall.com 페이지 전용
 */

document.addEventListener('DOMContentLoaded', async function() {
	// Home 페이지인지 확인
	const isHomePage = window.location.pathname === '/' ||
	                   window.location.pathname.includes('/home');

	if (!isHomePage) {
		return;
	}

	console.log('Home 핸들러 초기화');

	// 메인 상품 로드
	await loadMainProducts();
});

/**
 * 메인 페이지 상품 로드 (GIFT + COUPLE)
 */
async function loadMainProducts() {
	showLoading();

	const mainProducts = await getMainProducts();

	hideLoading();

	// Gift 상품 렌더링
	if (mainProducts.giftProducts && mainProducts.giftProducts.length > 0) {
		renderMainGiftProducts(mainProducts.giftProducts);
	}

	// Couple 상품 렌더링
	if (mainProducts.coupleProducts && mainProducts.coupleProducts.length > 0) {
		renderMainCoupleProducts(mainProducts.coupleProducts);
	}
}

/**
 * 메인 Gift 상품 렌더링
 *
 * @param {Array} products - Gift 상품 배열
 */
function renderMainGiftProducts(products) {
	const container = document.querySelector('.miniContainer:nth-of-type(1) .showItem');

	if (!container) {
		console.error('Gift 상품 컨테이너를 찾을 수 없습니다.');
		return;
	}

	// 기존 내용 제거
	container.innerHTML = '';

	// 최대 4개만 표시
	const displayProducts = products.slice(0, 4);

	if (displayProducts.length === 0) {
		container.innerHTML = '<p class="no-products">선물 상품이 없습니다.</p>';
		return;
	}

	// 상품 렌더링
	displayProducts.forEach(product => {
		const productHtml = renderHomeProductCard(product);
		container.innerHTML += productHtml;
	});
}

/**
 * 메인 Couple 상품 렌더링
 *
 * @param {Array} products - Couple 상품 배열
 */
function renderMainCoupleProducts(products) {
	const container = document.querySelector('.miniContainer:nth-of-type(2) .showItem');

	if (!container) {
		console.error('Couple 상품 컨테이너를 찾을 수 없습니다.');
		return;
	}

	// 기존 내용 제거
	container.innerHTML = '';

	// 최대 4개만 표시
	const displayProducts = products.slice(0, 4);

	if (displayProducts.length === 0) {
		container.innerHTML = '<p class="no-products">커플 상품이 없습니다.</p>';
		return;
	}

	// 상품 렌더링
	displayProducts.forEach(product => {
		const productHtml = renderHomeProductCard(product);
		container.innerHTML += productHtml;
	});
}

/**
 * Home 페이지 상품 카드 렌더링
 *
 * @param {Object} product - 상품 객체
 * @returns {string} HTML 문자열
 */
function renderHomeProductCard(product) {
	return `
		<div class="itemInfor">
			<a href="#" onclick="goToProductDetail('${product.prodCode}'); return false;">
				<div class="itemImage">
					<img src="${product.prodImage1 || '/img/default-product.png'}" alt="${product.prodName}">
				</div>
				<div class="itemExplanation">
					<h4>${product.prodName}</h4>
					<h5>${product.prodPrice ? product.prodPrice.toLocaleString() + '원' : '가격 미정'}</h5>
				</div>
			</a>
		</div>
	`;
}
