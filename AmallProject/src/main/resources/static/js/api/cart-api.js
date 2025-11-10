/**
 * 장바구니 관련 API (Phase 9-3: 선물 기능 포함)
 */

/**
 * 장바구니 조회
 *
 * @param {string} memberId - 회원 ID
 * @returns {Promise<Array>} 장바구니 목록
 */
async function getCart(memberId) {
    try {
        const response = await apiClient.get(`/api/cart/${memberId}`);

        if (response.success) {
            return response.data;
        }

        return [];
    } catch (error) {
        console.error('장바구니 조회 실패:', error);
        showToast('장바구니를 불러오지 못했습니다.', 'error');
        return [];
    }
}

/**
 * 장바구니에 일반 상품 추가
 *
 * @param {string} memberId - 회원 ID
 * @param {number} prodNum - 상품 번호
 * @param {number} quantity - 수량
 * @returns {Promise<boolean>} 성공 여부
 */
async function addToCart(memberId, prodNum, quantity) {
    showLoading();

    try {
        const response = await apiClient.post(`/api/cart/${memberId}/items`, {
            prodNum: prodNum,
            quantity: quantity
        });

        hideLoading();

        if (response.success) {
            showToast('장바구니에 추가되었습니다.', 'success');
            return true;
        }

        return false;
    } catch (error) {
        hideLoading();
        showToast(error.message || '장바구니 추가에 실패했습니다.', 'error');
        return false;
    }
}

/**
 * 장바구니에 선물로 상품 추가 (Phase 9-3)
 *
 * @param {string} memberId - 회원 ID (구매자)
 * @param {number} prodNum - 상품 번호
 * @param {number} quantity - 수량
 * @param {string} giftToMemberId - 선물 받는 사람 ID
 * @param {string} giftMessage - 선물 메시지
 * @returns {Promise<boolean>} 성공 여부
 */
async function addToCartAsGift(memberId, prodNum, quantity, giftToMemberId, giftMessage) {
    showLoading();

    try {
        const response = await apiClient.post(`/api/cart/${memberId}/gift-items`, {
            prodNum: prodNum,
            quantity: quantity,
            giftToMemberId: giftToMemberId,
            giftMessage: giftMessage || ''
        });

        hideLoading();

        if (response.success) {
            showToast('선물이 장바구니에 추가되었습니다. 🎁', 'success');
            return true;
        }

        return false;
    } catch (error) {
        hideLoading();
        showToast(error.message || '선물 추가에 실패했습니다.', 'error');
        return false;
    }
}

/**
 * 장바구니 수량 수정
 *
 * @param {string} memberId - 회원 ID
 * @param {number} cartId - 장바구니 ID
 * @param {number} quantity - 수량
 * @returns {Promise<boolean>} 성공 여부
 */
async function updateCartQuantity(memberId, cartId, quantity) {
    try {
        const response = await apiClient.patch(
            `/api/cart/${memberId}/items/${cartId}/quantity`,
            { quantity: quantity }
        );

        if (response.success) {
            showToast('수량이 수정되었습니다.', 'success');
            return true;
        }

        return false;
    } catch (error) {
        showToast(error.message || '수량 수정에 실패했습니다.', 'error');
        return false;
    }
}

/**
 * 장바구니 아이템을 선물로 설정 (Phase 9-3)
 *
 * @param {string} memberId - 회원 ID
 * @param {number} cartId - 장바구니 ID
 * @param {string} giftToMemberId - 선물 받는 사람 ID
 * @param {string} giftMessage - 선물 메시지
 * @returns {Promise<boolean>} 성공 여부
 */
async function setCartItemAsGift(memberId, cartId, giftToMemberId, giftMessage) {
    showLoading();

    try {
        const response = await apiClient.put(
            `/api/cart/${memberId}/items/${cartId}/gift`,
            {
                giftToMemberId: giftToMemberId,
                giftMessage: giftMessage || ''
            }
        );

        hideLoading();

        if (response.success) {
            showToast('선물로 설정되었습니다. 🎁', 'success');
            return true;
        }

        return false;
    } catch (error) {
        hideLoading();
        showToast(error.message || '선물 설정에 실패했습니다.', 'error');
        return false;
    }
}

/**
 * 장바구니 아이템을 일반 구매로 변경
 *
 * @param {string} memberId - 회원 ID
 * @param {number} cartId - 장바구니 ID
 * @returns {Promise<boolean>} 성공 여부
 */
async function setCartItemAsNormalPurchase(memberId, cartId) {
    try {
        const response = await apiClient.delete(
            `/api/cart/${memberId}/items/${cartId}/gift`
        );

        if (response.success) {
            showToast('일반 구매로 변경되었습니다.', 'success');
            return true;
        }

        return false;
    } catch (error) {
        showToast(error.message || '변경에 실패했습니다.', 'error');
        return false;
    }
}

/**
 * 장바구니 아이템 삭제
 *
 * @param {string} memberId - 회원 ID
 * @param {number} cartId - 장바구니 ID
 * @returns {Promise<boolean>} 성공 여부
 */
async function removeCartItem(memberId, cartId) {
    const confirmed = await confirm('정말 삭제하시겠습니까?');

    if (!confirmed) {
        return false;
    }

    showLoading();

    try {
        const response = await apiClient.delete(
            `/api/cart/${memberId}/items/${cartId}`
        );

        hideLoading();

        if (response.success) {
            showToast('장바구니에서 삭제되었습니다.', 'success');
            return true;
        }

        return false;
    } catch (error) {
        hideLoading();
        showToast(error.message || '삭제에 실패했습니다.', 'error');
        return false;
    }
}

/**
 * 장바구니 전체 삭제
 *
 * @param {string} memberId - 회원 ID
 * @returns {Promise<boolean>} 성공 여부
 */
async function clearCart(memberId) {
    const confirmed = await confirm('장바구니를 모두 비우시겠습니까?');

    if (!confirmed) {
        return false;
    }

    showLoading();

    try {
        const response = await apiClient.delete(`/api/cart/${memberId}`);

        hideLoading();

        if (response.success) {
            showToast('장바구니가 비워졌습니다.', 'success');
            return true;
        }

        return false;
    } catch (error) {
        hideLoading();
        showToast(error.message || '삭제에 실패했습니다.', 'error');
        return false;
    }
}
