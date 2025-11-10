/**
 * Store 페이지 핸들러 (Phase 9-4: Product API 통합)
 *
 * /store/amall.com 페이지 전용
 */

// 현재 선택된 카테고리
let currentCategory = 'GIFT';

document.addEventListener('DOMContentLoaded', async function() {
	// Store 페이지인지 확인
	if (!window.location.pathname.includes('/store')) {
		return;
	}

	console.log('Store 핸들러 초기화');

	// 초기 상품 로드 (GIFT 카테고리)
	await loadProducts('GIFT');

	// 탭 버튼 이벤트 리스너 설정
	setupCategoryTabs();
});

/**
 * 카테고리별 상품 로드
 *
 * @param {string} category - 카테고리 (GIFT, COUPLE)
 */
async function loadProducts(category) {
	showLoading();

	currentCategory = category;

	const products = await getProductsByCategory(category);

	hideLoading();

	// 상품 렌더링
	renderStoreProducts(products);

	// 탭 활성화 상태 변경
	updateActiveTab(category);
}

/**
 * Store 페이지 상품 렌더링
 *
 * @param {Array} products - 상품 배열
 */
function renderStoreProducts(products) {
	const container = document.querySelector('.showItem');

	if (!container) {
		console.error('상품 컨테이너를 찾을 수 없습니다.');
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
		const productHtml = renderStoreProductCard(product);
		container.innerHTML += productHtml;
	});
}

/**
 * Store 페이지 상품 카드 렌더링
 *
 * @param {Object} product - 상품 객체
 * @returns {string} HTML 문자열
 */
function renderStoreProductCard(product) {
	const memberId = getMemberIdFromPage();

	return `
		<div class="itemInfor">
			<a href="#" onclick="goToProductDetail('${escapeAttribute(product.prodCode)}'); return false;">
				<div class="${currentCategory.toLowerCase()}">
					<div class="itemImage">
						<img src="${escapeAttribute(product.prodImage1 || '/img/default-product.png')}" alt="${escapeAttribute(product.prodName)}">
					</div>
					<div class="itemExplanation">
						<h6>${escapeHtml(product.sellerId || '')}</h6>
						<h5>${escapeHtml(product.prodName)}</h5>
						<p class="product-price">${product.prodPrice ? product.prodPrice.toLocaleString() + '원' : ''}</p>
						<br>
						<nav>
							<a class="bakset" onclick="event.preventDefault(); event.stopPropagation(); handleAddToCartFromStore(${product.prodNum});">
								<img src="/img/imgCommon/basket.png" alt="장바구니">
							</a>
							<a class="like" onclick="event.preventDefault(); event.stopPropagation(); handleAddToWishListFromStore(${product.prodNum});">
								<img src="/img/imgCommon/emptyGood.png" alt="위시리스트">
							</a>
						</nav>
					</div>
				</div>
			</a>
		</div>
	`;
}

/**
 * 카테고리 탭 설정
 */
function setupCategoryTabs() {
	// Gift 탭
	const giftTab = document.querySelector('a[onclick="imgGift();"]');
	if (giftTab) {
		giftTab.onclick = function(e) {
			e.preventDefault();
			loadProducts('GIFT');
		};
	}

	// Couple 탭
	const coupleTab = document.querySelector('a[onclick="imgCouple();"]');
	if (coupleTab) {
		coupleTab.onclick = function(e) {
			e.preventDefault();
			loadProducts('COUPLE');
		};
	}

	// Party 탭 (추후 구현 또는 제거)
	const partyTab = document.querySelector('a[onclick="imgParty();"]');
	if (partyTab) {
		partyTab.onclick = function(e) {
			e.preventDefault();
			showToast('Party 카테고리는 준비 중입니다.', 'info');
		};
	}
}

/**
 * 활성 탭 업데이트
 *
 * @param {string} category - 카테고리
 */
function updateActiveTab(category) {
	// 모든 탭의 active 클래스 제거
	document.querySelectorAll('.tablinks').forEach(tab => {
		tab.classList.remove('active');
	});

	// 선택된 탭에 active 클래스 추가
	if (category === 'GIFT') {
		const giftTab = document.querySelector('a[onclick*="imgGift"]');
		if (giftTab) giftTab.classList.add('active');
	} else if (category === 'COUPLE') {
		const coupleTab = document.querySelector('a[onclick*="imgCouple"]');
		if (coupleTab) coupleTab.classList.add('active');
	}
}

/**
 * Store 페이지에서 장바구니 추가
 *
 * @param {number} prodNum - 상품 번호
 */
async function handleAddToCartFromStore(prodNum) {
	const memberId = getMemberIdFromPage();

	if (!memberId) {
		showToast('로그인이 필요합니다.', 'error');
		// 로그인 모달 열기 (legacy 함수)
		if (typeof onClickLogin === 'function') {
			onClickLogin();
		}
		return;
	}

	// 수량 입력 받기
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
 * Store 페이지에서 위시리스트 추가
 *
 * @param {number} prodNum - 상품 번호
 */
async function handleAddToWishListFromStore(prodNum) {
	const memberId = getMemberIdFromPage();

	if (!memberId) {
		showToast('로그인이 필요합니다.', 'error');
		if (typeof onClickLogin === 'function') {
			onClickLogin();
		}
		return;
	}

	const success = await addToWishList(memberId, prodNum);

	if (success) {
		// 아이콘 변경 등 UI 업데이트 가능
		const img = event.target;
		if (img && img.tagName === 'IMG') {
			img.src = '/img/imgCommon/Good.png'; // 채워진 하트로 변경
		}
	}
}

/**
 * 페이지에서 회원 ID 추출
 */
function getMemberIdFromPage() {
	const metaTag = document.querySelector('meta[name="member-id"]');
	if (metaTag) {
		return metaTag.getAttribute('content');
	}
	return sessionStorage.getItem('memberId');
}
