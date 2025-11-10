/**
 * 선물 관리 페이지 핸들러
 *
 * /amall.gift-management.com 페이지 전용
 *
 * 기능:
 * - 배송지 입력이 필요한 선물 조회 (PENDING)
 * - 완료된 선물 조회 (COMPLETED)
 * - 배송지 정보 입력 (delivery-modal.js 통합)
 * - 선물 상세 정보 표시
 */

document.addEventListener('DOMContentLoaded', async function() {
	// 선물 관리 페이지인지 확인
	if (!window.location.pathname.includes('/amall.gift-management.com')) {
		return;
	}

	console.log('선물 관리 핸들러 초기화');

	// 현재 로그인된 사용자 ID
	const memberId = getMemberIdFromPage();

	if (!memberId) {
		console.warn('로그인 정보가 없습니다.');
		showToast('로그인이 필요합니다.', 'error');
		return;
	}

	// 탭 전환 이벤트 설정
	setupGiftTabs(memberId);

	// 배송지 입력 필요 선물 로드 (기본 탭)
	await loadPendingGifts(memberId);
});

// getMemberIdFromPage() is now in common-utils.js

/**
 * 탭 전환 설정
 */
function setupGiftTabs(memberId) {
	const tabs = document.querySelectorAll('.gift-tab');

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
			if (tabName === 'pending-gifts') {
				await loadPendingGifts(memberId);
			} else if (tabName === 'completed-gifts') {
				await loadCompletedGifts(memberId);
			}
		});
	});
}

/**
 * 배송지 입력 필요한 선물 목록 로드 (PENDING)
 */
async function loadPendingGifts(memberId) {
	const container = document.querySelector('#pending-gifts .gift-list');

	if (!container) {
		console.error('배송지 입력 필요 선물 컨테이너를 찾을 수 없습니다.');
		return;
	}

	// 로딩 표시
	container.innerHTML = `
		<div class="loading-state">
			<div class="loading-spinner"></div>
			<p>배송지 입력이 필요한 선물을 불러오는 중...</p>
		</div>
	`;

	try {
		// API 호출: 대기 중인 선물 배송지 입력 요청 조회
		const requests = await getPendingGiftDeliveryRequests(memberId);

		// 컨테이너 초기화
		container.innerHTML = '';

		if (!requests || requests.length === 0) {
			container.innerHTML = `
				<div class="empty-state">
					<div class="empty-state-icon">🎁</div>
					<div class="empty-state-title">배송지 입력이 필요한 선물이 없습니다</div>
					<div class="empty-state-text">모든 선물의 배송지 정보가 입력되었습니다!</div>
				</div>
			`;
			return;
		}

		// 각 요청에 대해 주문 정보를 가져와서 카드 생성
		for (const request of requests) {
			try {
				// 주문 상세 정보 조회
				const order = await getOrder(request.orderId);
				if (order) {
					// 요청 정보와 주문 정보를 합쳐서 카드 생성
					const giftData = {
						...order,
						fromMemberId: request.fromMemberId,
						giftMessage: request.giftMessage || order.giftMessage,
						requestStatus: request.requestStatus
					};
					container.appendChild(createPendingGiftCard(giftData, memberId));
				}
			} catch (error) {
				console.error(`주문 ${request.orderId} 조회 실패:`, error);
			}
		}

	} catch (error) {
		console.error('배송지 입력 필요 선물 목록 로드 실패:', error);
		container.innerHTML = `
			<div class="empty-state">
				<div class="empty-state-icon">❌</div>
				<div class="empty-state-title">선물 목록을 불러올 수 없습니다</div>
				<div class="empty-state-text">잠시 후 다시 시도해주세요.</div>
			</div>
		`;
	}
}

/**
 * 완료된 선물 목록 로드
 */
async function loadCompletedGifts(memberId) {
	const container = document.querySelector('#completed-gifts .gift-list');

	if (!container) {
		console.error('완료된 선물 컨테이너를 찾을 수 없습니다.');
		return;
	}

	// 로딩 표시
	container.innerHTML = `
		<div class="loading-state">
			<div class="loading-spinner"></div>
			<p>완료된 선물을 불러오는 중...</p>
		</div>
	`;

	try {
		// API 호출: 받은 선물 목록 조회
		const gifts = await getReceivedGiftOrders(memberId);

		// 컨테이너 초기화
		container.innerHTML = '';

		// 배송지 정보가 입력된 선물만 필터링 (COMPLETED)
		const completedGifts = gifts.filter(gift =>
			gift.deliveryAddress && gift.deliveryAddress.trim() !== ''
		);

		if (!completedGifts || completedGifts.length === 0) {
			container.innerHTML = `
				<div class="empty-state">
					<div class="empty-state-icon">🎁</div>
					<div class="empty-state-title">완료된 선물이 없습니다</div>
					<div class="empty-state-text">배송지를 입력한 선물이 여기에 표시됩니다.</div>
				</div>
			`;
			return;
		}

		// 선물 목록 렌더링
		completedGifts.forEach(gift => {
			container.appendChild(createCompletedGiftCard(gift, memberId));
		});

	} catch (error) {
		console.error('완료된 선물 목록 로드 실패:', error);
		container.innerHTML = `
			<div class="empty-state">
				<div class="empty-state-icon">❌</div>
				<div class="empty-state-title">선물 목록을 불러올 수 없습니다</div>
				<div class="empty-state-text">잠시 후 다시 시도해주세요.</div>
			</div>
		`;
	}
}

/**
 * 배송지 입력 필요 선물 카드 생성
 */
function createPendingGiftCard(giftData, memberId) {
	const card = document.createElement('div');
	card.className = 'gift-card';

	// 상품 정보
	let productHtml = '';
	if (giftData.orderItems && giftData.orderItems.length > 0) {
		const item = giftData.orderItems[0]; // 선물은 보통 1개 상품
		productHtml = `
			<div class="gift-product-info">
				<div class="gift-product-name">${escapeHtml(item.prodName)}</div>
				<div class="gift-product-quantity">수량: ${item.quantity}개</div>
			</div>
		`;
	}

	// 선물 메시지
	let messageHtml = '';
	if (giftData.giftMessage) {
		messageHtml = `
			<div class="gift-message-box">
				<div class="gift-message-label">💌 선물 메시지</div>
				<div class="gift-message-text">${escapeHtml(giftData.giftMessage)}</div>
			</div>
		`;
	}

	card.innerHTML = `
		<div class="gift-card-header">
			<div class="gift-card-title">
				<span>🎁 선물이 도착했어요!</span>
			</div>
			<span class="gift-status-badge PENDING">배송지 입력 필요</span>
		</div>

		<div class="gift-card-body">
			<div class="gift-info">
				<span class="gift-info-label">From:</span>
				<span class="gift-info-value">${escapeHtml(giftData.fromMemberId || '익명')}</span>
			</div>

			${messageHtml}

			<div class="gift-info">
				<span class="gift-info-label">Order ID:</span>
				<span class="gift-info-value">${escapeHtml(giftData.orderId)}</span>
			</div>

			${productHtml}
		</div>

		<div class="gift-card-footer">
			<button class="btn btn-gift-primary" onclick="handleDeliveryAddressInput('${escapeAttribute(memberId)}', '${escapeAttribute(giftData.orderId)}')">
				배송지 입력하기
			</button>
		</div>
	`;

	return card;
}

/**
 * 완료된 선물 카드 생성
 */
function createCompletedGiftCard(giftData, memberId) {
	const card = document.createElement('div');
	card.className = 'gift-card';

	// 주문 상태 텍스트
	const statusText = getOrderStatusText(giftData.orderStatus);
	const statusClass = giftData.orderStatus;

	// 상품 정보
	let productHtml = '';
	if (giftData.orderItems && giftData.orderItems.length > 0) {
		const item = giftData.orderItems[0];
		productHtml = `
			<div class="gift-product-info">
				<div class="gift-product-name">${escapeHtml(item.prodName)}</div>
				<div class="gift-product-quantity">수량: ${item.quantity}개</div>
			</div>
		`;
	}

	// 선물 메시지
	let messageHtml = '';
	if (giftData.giftMessage) {
		messageHtml = `
			<div class="gift-message-box">
				<div class="gift-message-label">💌 선물 메시지</div>
				<div class="gift-message-text">${escapeHtml(giftData.giftMessage)}</div>
			</div>
		`;
	}

	// 배송지 정보
	let deliveryHtml = '';
	if (giftData.deliveryAddress) {
		deliveryHtml = `
			<div class="gift-delivery-info">
				<div class="gift-delivery-label">📦 배송지 정보</div>
				<div class="gift-delivery-text">
					<strong>${escapeHtml(giftData.deliveryName || '')}</strong> | ${escapeHtml(giftData.deliveryPhone || '')}<br>
					(${escapeHtml(giftData.deliveryPostCode || '')}) ${escapeHtml(giftData.deliveryAddress || '')} ${escapeHtml(giftData.deliveryDetailAddress || '')}
					${giftData.deliveryMessage ? `<br><em style="color: #059669;">메모: ${escapeHtml(giftData.deliveryMessage)}</em>` : ''}
				</div>
			</div>
		`;
	}

	// 상태 아이콘
	let statusIcon = '📦';
	if (statusClass === 'SHIPPED') statusIcon = '🚚';
	if (statusClass === 'DELIVERED') statusIcon = '✅';

	card.innerHTML = `
		<div class="gift-card-header">
			<div class="gift-card-title">
				<span>${statusIcon} ${statusText}</span>
			</div>
			<span class="gift-status-badge COMPLETED">배송지 입력 완료</span>
		</div>

		<div class="gift-card-body">
			<div class="gift-info">
				<span class="gift-info-label">From:</span>
				<span class="gift-info-value">${escapeHtml(giftData.senderMemberId || giftData.memberId || '익명')}</span>
			</div>

			${messageHtml}

			<div class="gift-info">
				<span class="gift-info-label">Order ID:</span>
				<span class="gift-info-value">${escapeHtml(giftData.orderId)}</span>
			</div>

			<div class="gift-info">
				<span class="gift-info-label">주문일:</span>
				<span class="gift-info-value">${formatDate(giftData.orderDate)}</span>
			</div>

			${productHtml}
			${deliveryHtml}
		</div>

		<div class="gift-card-footer">
			<button class="btn btn-outline" onclick="viewGiftOrderDetail('${escapeAttribute(giftData.orderId)}')">
				주문 상세 보기
			</button>
		</div>
	`;

	return card;
}

/**
 * 배송지 입력 처리
 */
async function handleDeliveryAddressInput(memberId, orderId) {
	try {
		// delivery-modal.js 사용
		const deliveryInfo = await showDeliveryModal();

		if (!deliveryInfo) {
			console.log('배송지 입력이 취소되었습니다.');
			return;
		}

		// 배송지 정보 업데이트 API 호출
		const success = await updateDeliveryAddress(memberId, orderId, deliveryInfo);

		if (success) {
			showToast('배송지 정보가 등록되었습니다! 곧 배송이 시작됩니다. 🎁', 'success', 4000);

			// 목록 새로고침
			await loadPendingGifts(memberId);
		}

	} catch (error) {
		console.error('배송지 입력 오류:', error);
		// 모달이 취소된 경우는 에러가 아니므로 조용히 처리
	}
}

/**
 * 선물 주문 상세 보기
 */
async function viewGiftOrderDetail(orderId) {
	try {
		const order = await getOrder(orderId);

		if (!order) {
			showToast('주문 정보를 찾을 수 없습니다.', 'error');
			return;
		}

		// 간단한 알림으로 표시 (또는 별도 모달 구현 가능)
		let detailMessage = `주문번호: ${order.orderId}\n`;
		detailMessage += `주문상태: ${getOrderStatusText(order.orderStatus)}\n`;
		detailMessage += `주문일시: ${formatDate(order.orderDate)}\n\n`;

		if (order.orderItems && order.orderItems.length > 0) {
			detailMessage += '상품 정보:\n';
			order.orderItems.forEach(item => {
				detailMessage += `- ${item.prodName} (x${item.quantity})\n`;
			});
		}

		if (order.deliveryAddress) {
			detailMessage += `\n배송지: ${order.deliveryAddress} ${order.deliveryDetailAddress || ''}`;
		}

		alert(detailMessage);

	} catch (error) {
		console.error('주문 상세 조회 실패:', error);
		showToast('주문 상세 정보를 불러올 수 없습니다.', 'error');
	}
}

// getOrderStatusText() and formatDate() are now in common-utils.js
