/**
 * 위시리스트 관련 API (Phase 9-3: 선물 기능 포함)
 */

/**
 * 내 위시리스트 조회
 *
 * @param {string} memberId - 회원 ID
 * @returns {Promise<Array>} 위시리스트 상품 목록
 */
async function getMyWishList(memberId) {
    try {
        const response = await apiClient.get(`/api/wishlist/${memberId}`);

        if (response.success) {
            return response.data;
        }

        return [];
    } catch (error) {
        console.error('위시리스트 조회 실패:', error);
        showToast('위시리스트를 불러오지 못했습니다.', 'error');
        return [];
    }
}

/**
 * 커플 상대방의 위시리스트 조회 (선물 쇼핑용) - Phase 9-3
 *
 * @param {string} memberId - 내 회원 ID
 * @returns {Promise<Array>} 상대방의 위시리스트 상품 목록
 */
async function getPartnerWishList(memberId) {
    try {
        const response = await apiClient.get(`/api/wishlist/${memberId}/partner`);

        if (response.success) {
            return response.data;
        }

        return [];
    } catch (error) {
        console.error('커플 위시리스트 조회 실패:', error);
        showToast(error.message || '상대방의 위시리스트를 불러오지 못했습니다.', 'error');
        return [];
    }
}

/**
 * 위시리스트에 상품 추가
 *
 * @param {string} memberId - 회원 ID
 * @param {number} prodNum - 상품 번호
 * @returns {Promise<boolean>} 성공 여부
 */
async function addToWishList(memberId, prodNum) {
    showLoading();

    try {
        const response = await apiClient.post(`/api/wishlist/${memberId}/items`, {
            prodNum: prodNum
        });

        hideLoading();

        if (response.success) {
            showToast('위시리스트에 추가되었습니다. ❤️', 'success');
            return true;
        }

        return false;
    } catch (error) {
        hideLoading();
        showToast(error.message || '위시리스트 추가에 실패했습니다.', 'error');
        return false;
    }
}

/**
 * 위시리스트에서 상품 삭제
 *
 * @param {string} memberId - 회원 ID
 * @param {number} wishlistId - 위시리스트 ID
 * @returns {Promise<boolean>} 성공 여부
 */
async function removeFromWishList(memberId, wishlistId) {
    const confirmed = await confirm('위시리스트에서 삭제하시겠습니까?');

    if (!confirmed) {
        return false;
    }

    showLoading();

    try {
        const response = await apiClient.delete(
            `/api/wishlist/${memberId}/items/${wishlistId}`
        );

        hideLoading();

        if (response.success) {
            showToast('위시리스트에서 삭제되었습니다.', 'success');
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
 * 위시리스트 전체 삭제
 *
 * @param {string} memberId - 회원 ID
 * @returns {Promise<boolean>} 성공 여부
 */
async function clearWishList(memberId) {
    const confirmed = await confirm('위시리스트를 모두 비우시겠습니까?');

    if (!confirmed) {
        return false;
    }

    showLoading();

    try {
        const response = await apiClient.delete(`/api/wishlist/${memberId}`);

        hideLoading();

        if (response.success) {
            showToast('위시리스트가 비워졌습니다.', 'success');
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
 * 상품이 위시리스트에 있는지 확인
 *
 * @param {string} memberId - 회원 ID
 * @param {number} prodNum - 상품 번호
 * @returns {Promise<boolean>} 포함 여부
 */
async function isInWishList(memberId, prodNum) {
    try {
        const response = await apiClient.get(
            `/api/wishlist/${memberId}/check`,
            { prodNum: prodNum }
        );

        if (response.success) {
            return response.data;
        }

        return false;
    } catch (error) {
        console.error('위시리스트 확인 실패:', error);
        return false;
    }
}
