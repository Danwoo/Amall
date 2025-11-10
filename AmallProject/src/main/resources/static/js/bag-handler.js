/**
 * Bag (장바구니) 페이지 핸들러 (Phase 9: API 통합)
 *
 * /bag/amall.com 페이지 전용
 */

document.addEventListener('DOMContentLoaded', async function() {
	// Bag 페이지인지 확인
	if (!window.location.pathname.includes('/bag')) {
		return;
	}

	console.log('Bag 핸들러 초기화');

	// 장바구니 로드
	await loadCart();

	// 전체 선택/해제 이벤트
	setupSelectAll();

	// 버튼 이벤트
	setup ButtonEvents();
});

/**
 * 장바구니 로드
 */
async function loadCart() {
	const memberId = getMemberIdFromPage();

	if (!memberId) {
		console.warn('로그인 정보가 없습니다.');
		return;
	}

	showLoading();

	const cartList = await getCart(memberId);

	hideLoading();

	// 장바구니 렌더링
	renderCart(cartList);
}

/**
 * 장바구니 렌더링
 *
 * @param {Array} cartList - 장바구니 목록
 */
function renderCart(cartList) {
	const tbody = document.querySelector('.bagTbody');

	if (!tbody) {
		console.error('장바구니 테이블을 찾을 수 없습니다.');
		return;
	}

	// 기존 내용 제거
	tbody.innerHTML = '';

	if (cartList.length === 0) {
		tbody.innerHTML = `
			<tr>
				<td colspan="6" style="text-align: center; padding: 40px;">
					장바구니가 비어있습니다.
				</td>
			</tr>
		`;
		return;
	}

	// 장바구니 아이템 렌더링
	cartList.forEach(cart => {
		const itemHtml = renderCartItem(cart);
		tbody.innerHTML += itemHtml;
	});

	// 각 아이템에 이벤트 리스너 추가
	attachCartItemEvents();
}

/**
 * 장바구니 아이템 HTML 생성
 *
 * @param {Object} cart - 장바구니 아이템
 * @returns {string} HTML 문자열
 */
function renderCartItem(cart) {
	const product = cart.product || {};
	const isGift = cart.isGift === 'Y';
	const giftBadge = isGift
		? `<span style="background: #ff6b6b; color: white; padding: 2px 6px; border-radius: 3px; font-size: 12px;">🎁 선물</span>`
		: '';

	const giftInfo = isGift && cart.giftMessage
		? `<p style="color: #666; font-size: 12px; margin-top: 5px;">메시지: ${cart.giftMessage}</p>`
		: '';

	return `
		<tr class="cartListDetail" data-cart-id="${cart.cartId}">
			<td class="bagTd" style="width: 2%;">
				<input type="checkbox" class="chk" name="chk" data-cart-id="${cart.cartId}">
			</td>
			<td class="bagTd" style="width: 13%;">
				<img class="bagImgTag" src="${product.prodImage1 || '/img/default-product.png'}" alt="${product.prodName || ''}">
			</td>
			<td class="bagTd" style="width: 27%;">
				${giftBadge}
				<a class="bagATag" href="#">${product.sellerId || ''}</a>
				<p class="bagPTag">${product.prodName || ''}</p>
				<span class="bagSpanTag price">${product.prodPrice ? product.prodPrice.toLocaleString() + '원' : ''}</span>
				${giftInfo}
			</td>
			<td class="bagTd cartListOption" style="width: 27%;">
				<p class="bagPTag">
					상품 주문 수량:
					<input type="number" class="quantity-input" value="${cart.cartQuantity}" min="1"
					       data-cart-id="${cart.cartId}" style="width: 60px; text-align: center;">개
				</p>
				${isGift ? '<p style="color: #ff6b6b; font-size: 12px;">선물 상품</p>' : ''}
			</td>
			<td class="bagTd" style="width: 15%;">
				<span class="bagSpanTag price">${(product.prodPrice * cart.cartQuantity).toLocaleString()}원</span><br>
				<button class="cart__list__orderbtn" style="background-color: #f247b8"
				        onclick="orderSingleItem(${cart.cartId})">주문하기</button>
			</td>
			<td class="bagTd" style="width: 15%;">
				${product.prodDeliveryfee ? product.prodDeliveryfee.toLocaleString() + '원' : '0원'}
			</td>
		</tr>
	`;
}

/**
 * 장바구니 아이템 이벤트 리스너 추가
 */
function attachCartItemEvents() {
	// 수량 변경 이벤트
	document.querySelectorAll('.quantity-input').forEach(input => {
		input.addEventListener('change', async function() {
			const cartId = parseInt(this.getAttribute('data-cart-id'));
			const newQuantity = parseInt(this.value);

			if (newQuantity < 1) {
				showToast('수량은 1 이상이어야 합니다.', 'error');
				this.value = 1;
				return;
			}

			await updateQuantity(cartId, newQuantity);
		});
	});

	// 개별 체크박스 이벤트
	document.querySelectorAll('.chk').forEach(checkbox => {
		checkbox.addEventListener('click', updateSelectAll);
	});
}

/**
 * 수량 변경
 *
 * @param {number} cartId - 장바구니 ID
 * @param {number} newQuantity - 새 수량
 */
async function updateQuantity(cartId, newQuantity) {
	const memberId = getMemberIdFromPage();

	if (!memberId) {
		showToast('로그인이 필요합니다.', 'error');
		return;
	}

	const success = await updateCartQuantity(memberId, cartId, newQuantity);

	if (success) {
		// 장바구니 새로고침
		await loadCart();
	}
}

/**
 * 전체 선택/해제 설정
 */
function setupSelectAll() {
	const chkAll = document.getElementById('chkAll');

	if (chkAll) {
		chkAll.addEventListener('click', function() {
			const checkboxes = document.querySelectorAll('.chk');
			checkboxes.forEach(cb => {
				cb.checked = this.checked;
			});
		});
	}
}

/**
 * 전체 선택 상태 업데이트
 */
function updateSelectAll() {
	const chkAll = document.getElementById('chkAll');
	const checkboxes = document.querySelectorAll('.chk');
	const checkedCount = document.querySelectorAll('.chk:checked').length;

	if (chkAll) {
		chkAll.checked = (checkboxes.length === checkedCount && checkboxes.length > 0);
	}
}

/**
 * 버튼 이벤트 설정
 */
function setupButtonEvents() {
	// 선택 상품 삭제 (첫 번째 cartListOptionbtn)
	const deleteBtn = document.querySelectorAll('.cartListOptionbtn')[0];
	if (deleteBtn) {
		deleteBtn.addEventListener('click', async function(e) {
			e.preventDefault();
			await deleteSelectedItems();
		});
	}

	// 쇼핑 계속하기
	const continueBtn = document.querySelector('.cartBigorderBtn.left');
	if (continueBtn) {
		continueBtn.addEventListener('click', function() {
			window.location.href = '/store/amall.com';
		});
	}

	// 전체 주문하기
	const orderAllBtn = document.querySelector('.cartBigorderBtn.right');
	if (orderAllBtn) {
		orderAllBtn.addEventListener('click', function() {
			orderSelectedItems();
		});
	}
}

/**
 * 선택한 아이템 삭제
 */
async function deleteSelectedItems() {
	const memberId = getMemberIdFromPage();

	if (!memberId) {
		showToast('로그인이 필요합니다.', 'error');
		return;
	}

	const selectedCheckboxes = document.querySelectorAll('.chk:checked');

	if (selectedCheckboxes.length === 0) {
		showToast('삭제할 상품을 선택해주세요.', 'error');
		return;
	}

	const confirmed = await confirm(`선택한 ${selectedCheckboxes.length}개 상품을 삭제하시겠습니까?`);

	if (!confirmed) {
		return;
	}

	showLoading();

	// 선택된 아이템들 삭제
	for (const checkbox of selectedCheckboxes) {
		const cartId = parseInt(checkbox.getAttribute('data-cart-id'));
		await removeCartItem(memberId, cartId);
	}

	hideLoading();

	// 장바구니 새로고침
	await loadCart();
}

/**
 * 단일 아이템 주문
 *
 * @param {number} cartId - 장바구니 ID
 */
async function orderSingleItem(cartId) {
	const memberId = getMemberIdFromPage();

	if (!memberId) {
		showToast('로그인이 필요합니다.', 'error');
		return;
	}

	// 장바구니 아이템 정보 조회
	const cartList = await getCart(memberId);
	const cartItem = cartList.find(item => item.cartId === cartId);

	if (!cartItem) {
		showToast('장바구니 정보를 찾을 수 없습니다.', 'error');
		return;
	}

	// 선물 상품인 경우
	if (cartItem.isGift === 'Y') {
		await orderGiftItem(memberId, cartId);
	} else {
		// 일반 상품인 경우 - 배송지 입력 모달 표시
		showDeliveryInfoModal([cartId]);
	}
}

/**
 * 선택한 아이템 주문
 */
async function orderSelectedItems() {
	const selectedCheckboxes = document.querySelectorAll('.chk:checked');

	if (selectedCheckboxes.length === 0) {
		showToast('주문할 상품을 선택해주세요.', 'error');
		return;
	}

	const memberId = getMemberIdFromPage();

	if (!memberId) {
		showToast('로그인이 필요합니다.', 'error');
		return;
	}

	const cartIds = Array.from(selectedCheckboxes).map(cb =>
		parseInt(cb.getAttribute('data-cart-id'))
	);

	// 선물 상품이 포함되어 있는지 확인
	const cartList = await getCart(memberId);
	const selectedItems = cartList.filter(item => cartIds.includes(item.cartId));
	const hasGiftItem = selectedItems.some(item => item.isGift === 'Y');

	if (hasGiftItem) {
		showToast('선물 상품은 개별로만 주문 가능합니다.', 'error');
		return;
	}

	// 일반 상품들 주문 - 배송지 입력 모달 표시
	showDeliveryInfoModal(cartIds);
}

/**
 * 선물 상품 주문
 *
 * @param {string} memberId - 회원 ID
 * @param {number} cartId - 장바구니 ID
 */
async function orderGiftItem(memberId, cartId) {
	const confirmed = await confirm('선물을 주문하시겠습니까?\n선물 받는 사람이 배송지를 입력해야 배송이 시작됩니다.');

	if (!confirmed) {
		return;
	}

	// 간단한 결제 방법 선택 (실제로는 결제 모달 필요)
	const paymentMethod = 'CARD'; // 임시 하드코딩

	const orderId = await createGiftOrder(memberId, cartId, '선물입니다!', paymentMethod);

	if (orderId) {
		showToast('선물 주문이 완료되었습니다! 선물 받는 사람이 배송지를 입력하면 배송이 시작됩니다.', 'success');

		// 장바구니 새로고침
		await loadCart();
	}
}

/**
 * 배송지 입력 모달 표시
 *
 * @param {Array<number>} cartIds - 장바구니 ID 목록
 */
function showDeliveryInfoModal(cartIds) {
	// 간단한 프롬프트로 임시 구현 (실제로는 모달 필요)
	const deliveryName = prompt('받는 사람 이름을 입력해주세요:');
	if (!deliveryName) return;

	const deliveryPhone = prompt('받는 사람 전화번호를 입력해주세요:');
	if (!deliveryPhone) return;

	const deliveryPostCode = prompt('우편번호를 입력해주세요:');
	if (!deliveryPostCode) return;

	const deliveryAddress = prompt('주소를 입력해주세요:');
	if (!deliveryAddress) return;

	const deliveryDetailAddress = prompt('상세주소를 입력해주세요 (선택):') || '';
	const deliveryMessage = prompt('배송 메시지를 입력해주세요 (선택):') || '';

	const deliveryInfo = {
		name: deliveryName,
		phone: deliveryPhone,
		postCode: deliveryPostCode,
		address: deliveryAddress,
		detailAddress: deliveryDetailAddress,
		message: deliveryMessage
	};

	// 주문 생성
	processOrder(cartIds, deliveryInfo);
}

/**
 * 주문 처리
 *
 * @param {Array<number>} cartIds - 장바구니 ID 목록
 * @param {Object} deliveryInfo - 배송 정보
 */
async function processOrder(cartIds, deliveryInfo) {
	const memberId = getMemberIdFromPage();

	if (!memberId) {
		showToast('로그인이 필요합니다.', 'error');
		return;
	}

	// 간단한 결제 방법 선택 (실제로는 결제 모달 필요)
	const paymentMethod = 'CARD'; // 임시 하드코딩

	const orderId = await createOrder(memberId, cartIds, deliveryInfo, paymentMethod);

	if (orderId) {
		showToast('주문이 완료되었습니다!', 'success');

		// 장바구니 새로고침
		await loadCart();
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
