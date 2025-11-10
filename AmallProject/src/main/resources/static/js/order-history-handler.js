/**
 * 주문 내역 페이지 핸들러
 *
 * /order-history 페이지 전용
 */

document.addEventListener('DOMContentLoaded', async function() {
	// 주문 내역 페이지인지 확인
	if (!window.location.pathname.includes('/amall.order-history.com')) {
		return;
	}

	console.log('주문 내역 핸들러 초기화');

	// 현재 로그인된 사용자 ID
	const memberId = getMemberIdFromPage();

	if (!memberId) {
		console.warn('로그인 정보가 없습니다.');
		showToast('로그인이 필요합니다.', 'error');
		// Optionally redirect to login
		// window.location.href = '/login';
		return;
	}

	// 탭 전환 이벤트 설정
	setupTabs();

	// 주문 내역 로드
	await loadMyOrders(memberId);

	// 주문 상세 모달 설정
	setupOrderDetailModal();
});

/**
 * 페이지에서 회원 ID 추출
 */
function getMemberIdFromPage() {
	// Thymeleaf에서 주입한 memberId를 찾음
	const metaTag = document.querySelector('meta[name="member-id"]');
	if (metaTag) {
		return metaTag.getAttribute('content');
	}

	// 세션 스토리지에서 조회
	return sessionStorage.getItem('memberId');
}

/**
 * 탭 전환 설정
 */
function setupTabs() {
	const tabs = document.querySelectorAll('.order-tab');

	tabs.forEach(tab => {
		tab.addEventListener('click', async function() {
			const tabName = this.dataset.tab;

			// 모든 탭 비활성화
			tabs.forEach(t => t.classList.remove('active'));
			document.querySelectorAll('.tab-content').forEach(content => {
				content.classList.remove('active');
			});

			// 현재 탭 활성화
			this.classList.add('active');
			document.getElementById(tabName).classList.add('active');

			// 데이터 로드
			const memberId = getMemberIdFromPage();
			if (tabName === 'my-orders') {
				await loadMyOrders(memberId);
			} else if (tabName === 'received-gifts') {
				await loadReceivedGifts(memberId);
			}
		});
	});
}

/**
 * 내 주문 목록 로드
 */
async function loadMyOrders(memberId) {
	const container = document.querySelector('#my-orders .order-list');

	if (!container) {
		console.error('주문 목록 컨테이너를 찾을 수 없습니다.');
		return;
	}

	// 로딩 표시
	container.innerHTML = '<div class="loading-state"><p>주문 내역을 불러오는 중...</p></div>';

	try {
		// API 호출
		const orders = await getMemberOrders(memberId);

		// 컨테이너 초기화
		container.innerHTML = '';

		if (!orders || orders.length === 0) {
			container.innerHTML = `
				<div class="empty-state">
					<div class="empty-state-icon">📦</div>
					<div class="empty-state-title">주문 내역이 없습니다</div>
					<div class="empty-state-text">첫 주문을 시작해보세요!</div>
				</div>
			`;
			return;
		}

		// 주문 목록 렌더링
		orders.forEach(order => {
			container.appendChild(createOrderCard(order, memberId, false));
		});

	} catch (error) {
		console.error('주문 목록 로드 실패:', error);
		container.innerHTML = `
			<div class="empty-state">
				<div class="empty-state-icon">❌</div>
				<div class="empty-state-title">주문 내역을 불러올 수 없습니다</div>
				<div class="empty-state-text">잠시 후 다시 시도해주세요.</div>
			</div>
		`;
	}
}

/**
 * 받은 선물 목록 로드
 */
async function loadReceivedGifts(memberId) {
	const container = document.querySelector('#received-gifts .order-list');

	if (!container) {
		console.error('받은 선물 컨테이너를 찾을 수 없습니다.');
		return;
	}

	// 로딩 표시
	container.innerHTML = '<div class="loading-state"><p>받은 선물을 불러오는 중...</p></div>';

	try {
		// API 호출
		const gifts = await getReceivedGiftOrders(memberId);

		// 컨테이너 초기화
		container.innerHTML = '';

		if (!gifts || gifts.length === 0) {
			container.innerHTML = `
				<div class="empty-state">
					<div class="empty-state-icon">🎁</div>
					<div class="empty-state-title">받은 선물이 없습니다</div>
					<div class="empty-state-text">누군가 선물을 보내주길 기다려보세요!</div>
				</div>
			`;
			return;
		}

		// 선물 목록 렌더링
		gifts.forEach(gift => {
			container.appendChild(createOrderCard(gift, memberId, true));
		});

	} catch (error) {
		console.error('받은 선물 목록 로드 실패:', error);
		container.innerHTML = `
			<div class="empty-state">
				<div class="empty-state-icon">❌</div>
				<div class="empty-state-title">받은 선물을 불러올 수 없습니다</div>
				<div class="empty-state-text">잠시 후 다시 시도해주세요.</div>
			</div>
		`;
	}
}

/**
 * 주문 카드 생성
 */
function createOrderCard(order, memberId, isGift) {
	const card = document.createElement('div');
	card.className = 'order-card';

	// 선물 여부 확인 (isGift는 "Y" 또는 "N" 문자열)
	const isGiftOrder = order.isGift === 'Y' || isGift;

	// 상태 한글 변환
	const statusText = getOrderStatusText(order.orderStatus);
	const statusClass = order.orderStatus;

	// 취소 가능 여부 (PENDING 또는 PAID 상태)
	const isCancellable = order.orderStatus === 'PENDING' || order.orderStatus === 'PAID';

	// 주문 아이템 렌더링
	let itemsHtml = '';
	if (order.orderItems && order.orderItems.length > 0) {
		itemsHtml = order.orderItems.map(item => `
			<div class="order-item">
				<div>
					<span class="order-item-name">${escapeHtml(item.prodName)}</span>
					<span class="order-item-quantity">(x${item.quantity})</span>
				</div>
				<span class="order-item-price">${formatPrice(item.totalPrice || (item.prodPrice * item.quantity))}원</span>
			</div>
		`).join('');
	}

	// 배송비 포함 총액 계산
	const totalAmount = order.totalAmount || 0;
	const deliveryFee = order.deliveryFee || 0;
	const grandTotal = totalAmount + deliveryFee;

	// 배송지 정보
	let deliveryHtml = '';
	if (order.deliveryAddress) {
		deliveryHtml = `
			<div class="order-delivery">
				<div class="order-delivery-label">배송지</div>
				<div>${escapeHtml(order.deliveryName || '')} | ${escapeHtml(order.deliveryPhone || '')}</div>
				<div>${escapeHtml(order.deliveryAddress || '')} ${escapeHtml(order.deliveryDetailAddress || '')}</div>
				${order.deliveryMessage ? `<div style="margin-top: 8px; font-style: italic;">메모: ${escapeHtml(order.deliveryMessage)}</div>` : ''}
			</div>
		`;
	} else if (isGiftOrder && order.orderStatus === 'PENDING') {
		deliveryHtml = `
			<div class="order-delivery" style="background-color: #fef3c7; color: #f59e0b;">
				<div class="order-delivery-label">배송지 미입력</div>
				<div>선물을 받으려면 배송지 정보를 입력해주세요.</div>
			</div>
		`;
	}

	// 선물 메시지
	let giftMessageHtml = '';
	if (isGiftOrder && order.giftMessage) {
		giftMessageHtml = `
			<div class="order-delivery" style="background-color: #fef3c7;">
				<div class="order-delivery-label">🎁 선물 메시지</div>
				<div style="font-style: italic;">${escapeHtml(order.giftMessage)}</div>
			</div>
		`;
	}

	card.innerHTML = `
		<div class="order-header">
			<div>
				<span class="order-id">주문번호: ${escapeHtml(order.orderId)}</span>
				${isGiftOrder ? '<span class="gift-badge">🎁 선물</span>' : ''}
			</div>
			<div>
				<span class="order-date">${formatDate(order.orderDate)}</span>
			</div>
		</div>

		<div class="order-body">
			<div style="margin-bottom: 12px;">
				<span class="order-status ${statusClass}">${statusText}</span>
			</div>

			<div class="order-items">
				${itemsHtml}
			</div>

			<div class="order-summary">
				<span class="order-total">총 결제금액</span>
				<span class="order-total-amount">${formatPrice(grandTotal)}원</span>
			</div>

			${deliveryHtml}
			${giftMessageHtml}
		</div>

		<div class="order-footer">
			<button class="btn btn-outline" onclick="viewOrderDetail('${escapeAttribute(order.orderId)}')">
				상세보기
			</button>
			${isCancellable ? `
				<button class="btn btn-danger" onclick="handleCancelOrder('${escapeAttribute(memberId)}', '${escapeAttribute(order.orderId)}')">
					주문 취소
				</button>
			` : ''}
		</div>
	`;

	return card;
}

/**
 * 주문 상태 한글 변환
 */
function getOrderStatusText(status) {
	const statusMap = {
		'PENDING': '결제 대기',
		'PAID': '결제 완료',
		'SHIPPED': '배송 중',
		'DELIVERED': '배송 완료',
		'CANCELLED': '취소됨'
	};
	return statusMap[status] || status;
}

/**
 * 날짜 포맷팅
 */
function formatDate(dateString) {
	if (!dateString) return '';

	try {
		const date = new Date(dateString);
		const year = date.getFullYear();
		const month = String(date.getMonth() + 1).padStart(2, '0');
		const day = String(date.getDate()).padStart(2, '0');
		const hours = String(date.getHours()).padStart(2, '0');
		const minutes = String(date.getMinutes()).padStart(2, '0');

		return `${year}-${month}-${day} ${hours}:${minutes}`;
	} catch (error) {
		return dateString;
	}
}

/**
 * 가격 포맷팅
 */
function formatPrice(price) {
	if (!price && price !== 0) return '0';
	return Number(price).toLocaleString('ko-KR');
}

/**
 * 주문 취소 처리
 */
async function handleCancelOrder(memberId, orderId) {
	const success = await cancelOrder(memberId, orderId);

	if (success) {
		// 현재 활성화된 탭 확인
		const activeTab = document.querySelector('.order-tab.active').dataset.tab;

		// 목록 새로고침
		if (activeTab === 'my-orders') {
			await loadMyOrders(memberId);
		} else if (activeTab === 'received-gifts') {
			await loadReceivedGifts(memberId);
		}
	}
}

/**
 * 주문 상세 보기
 */
async function viewOrderDetail(orderId) {
	const modal = document.getElementById('orderDetailModal');
	const body = document.getElementById('orderDetailBody');

	if (!modal || !body) {
		console.error('주문 상세 모달을 찾을 수 없습니다.');
		return;
	}

	// 모달 표시
	modal.style.display = 'block';

	// 로딩 표시
	body.innerHTML = '<div class="loading-state"><p>주문 상세를 불러오는 중...</p></div>';

	try {
		// API 호출
		const order = await getOrder(orderId);

		if (!order) {
			body.innerHTML = '<div class="empty-state"><p>주문 정보를 찾을 수 없습니다.</p></div>';
			return;
		}

		// 주문 상세 렌더링
		let itemsHtml = '';
		if (order.orderItems && order.orderItems.length > 0) {
			itemsHtml = order.orderItems.map(item => `
				<div class="order-item">
					<div>
						<span class="order-item-name">${escapeHtml(item.prodName)}</span>
						<span class="order-item-quantity">(x${item.quantity})</span>
					</div>
					<span class="order-item-price">${formatPrice(item.totalPrice || (item.prodPrice * item.quantity))}원</span>
				</div>
			`).join('');
		}

		const totalAmount = order.totalAmount || 0;
		const deliveryFee = order.deliveryFee || 0;
		const grandTotal = totalAmount + deliveryFee;

		body.innerHTML = `
			<div class="order-detail-section">
				<div class="order-detail-section-title">주문 정보</div>
				<div style="margin-bottom: 8px;">
					<strong>주문번호:</strong> ${escapeHtml(order.orderId)}
				</div>
				<div style="margin-bottom: 8px;">
					<strong>주문일시:</strong> ${formatDate(order.orderDate)}
				</div>
				<div style="margin-bottom: 8px;">
					<strong>주문상태:</strong> <span class="order-status ${order.orderStatus}">${getOrderStatusText(order.orderStatus)}</span>
				</div>
				<div style="margin-bottom: 8px;">
					<strong>결제방법:</strong> ${escapeHtml(order.paymentMethod || '-')}
				</div>
				${order.isGift === 'Y' ? `
					<div style="margin-bottom: 8px;">
						<strong>선물 주문:</strong> <span class="gift-badge">🎁 선물</span>
					</div>
				` : ''}
			</div>

			<div class="order-detail-section">
				<div class="order-detail-section-title">주문 상품</div>
				<div class="order-items">
					${itemsHtml}
				</div>
			</div>

			<div class="order-detail-section">
				<div class="order-detail-section-title">결제 정보</div>
				<div style="margin-bottom: 8px;">
					<strong>상품 금액:</strong> ${formatPrice(totalAmount)}원
				</div>
				<div style="margin-bottom: 8px;">
					<strong>배송비:</strong> ${formatPrice(deliveryFee)}원
				</div>
				<div style="margin-top: 12px; padding-top: 12px; border-top: 1px solid #e5e7eb;">
					<strong style="font-size: 16px;">총 결제금액:</strong>
					<span style="font-size: 18px; font-weight: 700; color: #3b82f6;">${formatPrice(grandTotal)}원</span>
				</div>
			</div>

			${order.deliveryAddress ? `
				<div class="order-detail-section">
					<div class="order-detail-section-title">배송지 정보</div>
					<div style="margin-bottom: 8px;">
						<strong>수령인:</strong> ${escapeHtml(order.deliveryName || '')}
					</div>
					<div style="margin-bottom: 8px;">
						<strong>연락처:</strong> ${escapeHtml(order.deliveryPhone || '')}
					</div>
					<div style="margin-bottom: 8px;">
						<strong>주소:</strong> (${escapeHtml(order.deliveryPostCode || '')}) ${escapeHtml(order.deliveryAddress || '')} ${escapeHtml(order.deliveryDetailAddress || '')}
					</div>
					${order.deliveryMessage ? `
						<div style="margin-bottom: 8px;">
							<strong>배송 메모:</strong> ${escapeHtml(order.deliveryMessage)}
						</div>
					` : ''}
				</div>
			` : ''}

			${order.giftMessage ? `
				<div class="order-detail-section">
					<div class="order-detail-section-title">🎁 선물 메시지</div>
					<div style="padding: 12px; background-color: #fef3c7; border-radius: 8px; font-style: italic;">
						${escapeHtml(order.giftMessage)}
					</div>
				</div>
			` : ''}
		`;

	} catch (error) {
		console.error('주문 상세 로드 실패:', error);
		body.innerHTML = '<div class="empty-state"><p>주문 상세를 불러올 수 없습니다.</p></div>';
	}
}

/**
 * 주문 상세 모달 설정
 */
function setupOrderDetailModal() {
	const modal = document.getElementById('orderDetailModal');
	const closeBtn = document.querySelector('.order-detail-close');

	if (!modal) return;

	// 닫기 버튼
	if (closeBtn) {
		closeBtn.addEventListener('click', function() {
			modal.style.display = 'none';
		});
	}

	// 모달 외부 클릭 시 닫기
	window.addEventListener('click', function(event) {
		if (event.target === modal) {
			modal.style.display = 'none';
		}
	});
}
