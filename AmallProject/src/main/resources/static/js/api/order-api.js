/**
 * 주문 관련 API (Phase 9: Order System Implementation)
 */

/**
 * 일반 주문 생성
 *
 * @param {string} memberId - 회원 ID
 * @param {Array<number>} cartIds - 장바구니 ID 목록
 * @param {Object} deliveryInfo - 배송 정보
 * @param {string} paymentMethod - 결제 방법
 * @returns {Promise<string|null>} 주문 ID 또는 null
 */
async function createOrder(memberId, cartIds, deliveryInfo, paymentMethod) {
	showLoading();

	try {
		const response = await apiClient.post(`/api/orders/${memberId}`, {
			cartIds: cartIds,
			deliveryName: deliveryInfo.name,
			deliveryPhone: deliveryInfo.phone,
			deliveryPostCode: deliveryInfo.postCode,
			deliveryAddress: deliveryInfo.address,
			deliveryDetailAddress: deliveryInfo.detailAddress || '',
			deliveryMessage: deliveryInfo.message || '',
			paymentMethod: paymentMethod
		});

		hideLoading();

		if (response.success) {
			showToast('주문이 완료되었습니다.', 'success');
			return response.data; // orderId
		}

		return null;
	} catch (error) {
		hideLoading();
		showToast(error.message || '주문에 실패했습니다.', 'error');
		return null;
	}
}

/**
 * 선물 주문 생성
 *
 * @param {string} memberId - 회원 ID (선물 보내는 사람)
 * @param {number} cartId - 장바구니 ID (선물 상품 1개)
 * @param {string} giftMessage - 선물 메시지
 * @param {string} paymentMethod - 결제 방법
 * @returns {Promise<string|null>} 주문 ID 또는 null
 */
async function createGiftOrder(memberId, cartId, giftMessage, paymentMethod) {
	showLoading();

	try {
		const response = await apiClient.post(`/api/orders/${memberId}/gift`, {
			cartId: cartId,
			giftMessage: giftMessage,
			paymentMethod: paymentMethod
		});

		hideLoading();

		if (response.success) {
			showToast('선물 주문이 완료되었습니다. 🎁', 'success');
			return response.data; // orderId
		}

		return null;
	} catch (error) {
		hideLoading();
		showToast(error.message || '선물 주문에 실패했습니다.', 'error');
		return null;
	}
}

/**
 * 주문 조회
 *
 * @param {string} orderId - 주문 ID
 * @returns {Promise<Object|null>} 주문 정보 또는 null
 */
async function getOrder(orderId) {
	try {
		const response = await apiClient.get(`/api/orders/${orderId}`);

		if (response.success) {
			return response.data;
		}

		return null;
	} catch (error) {
		console.error('주문 조회 실패:', error);
		showToast('주문 정보를 불러오지 못했습니다.', 'error');
		return null;
	}
}

/**
 * 회원의 주문 목록 조회
 *
 * @param {string} memberId - 회원 ID
 * @returns {Promise<Array>} 주문 목록
 */
async function getMemberOrders(memberId) {
	try {
		const response = await apiClient.get(`/api/orders/member/${memberId}`);

		if (response.success) {
			return response.data;
		}

		return [];
	} catch (error) {
		console.error('주문 목록 조회 실패:', error);
		showToast('주문 목록을 불러오지 못했습니다.', 'error');
		return [];
	}
}

/**
 * 회원이 선물 받은 주문 목록 조회
 *
 * @param {string} memberId - 회원 ID (선물 받은 사람)
 * @returns {Promise<Array>} 선물 받은 주문 목록
 */
async function getReceivedGiftOrders(memberId) {
	try {
		const response = await apiClient.get(`/api/orders/member/${memberId}/received-gifts`);

		if (response.success) {
			return response.data;
		}

		return [];
	} catch (error) {
		console.error('선물 받은 주문 목록 조회 실패:', error);
		showToast('선물 받은 주문 목록을 불러오지 못했습니다.', 'error');
		return [];
	}
}

/**
 * 배송지 정보 업데이트 (선물 받는 사람)
 *
 * @param {string} memberId - 회원 ID (선물 받는 사람)
 * @param {string} orderId - 주문 ID
 * @param {Object} deliveryInfo - 배송 정보
 * @returns {Promise<boolean>} 성공 여부
 */
async function updateDeliveryAddress(memberId, orderId, deliveryInfo) {
	showLoading();

	try {
		const response = await apiClient.put(
			`/api/orders/${orderId}/delivery-address?memberId=${memberId}`,
			{
				deliveryName: deliveryInfo.name,
				deliveryPhone: deliveryInfo.phone,
				deliveryPostCode: deliveryInfo.postCode,
				deliveryAddress: deliveryInfo.address,
				deliveryDetailAddress: deliveryInfo.detailAddress || '',
				deliveryMessage: deliveryInfo.message || ''
			}
		);

		hideLoading();

		if (response.success) {
			showToast('배송지 정보가 업데이트되었습니다.', 'success');
			return true;
		}

		return false;
	} catch (error) {
		hideLoading();
		showToast(error.message || '배송지 업데이트에 실패했습니다.', 'error');
		return false;
	}
}

/**
 * 회원의 대기 중인 선물 배송지 입력 요청 목록 조회
 *
 * @param {string} memberId - 회원 ID (선물 받는 사람)
 * @returns {Promise<Array>} 대기 중인 요청 목록
 */
async function getPendingGiftDeliveryRequests(memberId) {
	try {
		const response = await apiClient.get(`/api/gift-delivery-requests/member/${memberId}/pending`);

		if (response.success) {
			return response.data;
		}

		return [];
	} catch (error) {
		console.error('대기 중인 선물 배송지 입력 요청 조회 실패:', error);
		return [];
	}
}

/**
 * 주문 취소
 *
 * @param {string} memberId - 회원 ID
 * @param {string} orderId - 주문 ID
 * @returns {Promise<boolean>} 성공 여부
 */
async function cancelOrder(memberId, orderId) {
	const confirmed = await confirm('정말 주문을 취소하시겠습니까?\n재고가 복원되며 이 작업은 되돌릴 수 없습니다.');

	if (!confirmed) {
		return false;
	}

	showLoading();

	try {
		const response = await apiClient.delete(`/api/orders/${orderId}?memberId=${memberId}`);

		hideLoading();

		if (response.success) {
			showToast('주문이 취소되었습니다.', 'success');
			return true;
		}

		return false;
	} catch (error) {
		hideLoading();
		showToast(error.message || '주문 취소에 실패했습니다.', 'error');
		return false;
	}
}
