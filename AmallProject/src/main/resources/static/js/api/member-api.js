/**
 * 회원 관련 API
 */

/**
 * 회원가입
 *
 * @param {Object} memberData - 회원가입 데이터
 * @returns {Promise<Object>} API 응답
 */
async function signUp(memberData) {
    showLoading();
    clearAllFieldErrors();

    try {
        // 폼 검증
        const { isValid, errors } = validateForm(memberData, [
            'memberId',
            'memberPwd',
            'memberPwdCheck',
            'memberName',
            'memberEmail',
            'memberPhone',
            'memberPostCode',
            'memberAddress'
        ]);

        if (!isValid) {
            // 필드별 에러 표시
            Object.keys(errors).forEach(field => {
                showFieldError(field, errors[field]);
            });
            hideLoading();
            showToast('입력 정보를 확인해주세요.', 'error');
            return null;
        }

        // API 호출
        const response = await apiClient.post('/api/members', {
            memberId: memberData.memberId,
            memberPwd: memberData.memberPwd,
            memberName: memberData.memberName,
            memberEmail: memberData.memberEmail,
            memberPhone: memberData.memberPhone,
            memberGender: memberData.memberGender || 'M',
            memberBirth: memberData.memberBirth,
            memberPostCode: memberData.memberPostCode,
            memberAddress: memberData.memberAddress,
            memberDetailAddress: memberData.memberDetailAddress,
            memberExtraAddress: memberData.memberExtraAddress
        });

        hideLoading();

        if (response.success) {
            showToast('회원가입이 완료되었습니다! 로그인해주세요.', 'success');
            return response.data;
        } else {
            showToast(response.message || '회원가입에 실패했습니다.', 'error');
            return null;
        }

    } catch (error) {
        hideLoading();

        // Backend 필드 에러 처리
        if (error.errors && Array.isArray(error.errors)) {
            error.errors.forEach(fieldError => {
                showFieldError(fieldError.field, fieldError.message);
            });
            showToast('입력 정보를 확인해주세요.', 'error');
        } else {
            showToast(error.message || '회원가입에 실패했습니다.', 'error');
        }

        return null;
    }
}

/**
 * 아이디 중복 확인
 *
 * @param {string} memberId - 확인할 아이디
 * @returns {Promise<boolean>} 사용 가능하면 true
 */
async function checkIdAvailable(memberId) {
    try {
        const response = await apiClient.get('/api/members/check-id', { memberId });

        if (response.success) {
            return response.data; // true or false
        }

        return false;
    } catch (error) {
        console.error('아이디 중복 확인 실패:', error);
        return false;
    }
}

/**
 * 로그인 (Session 기반)
 *
 * Note: 현재는 Spring Security의 form login 사용
 * 향후 JWT로 전환 시 이 함수 사용
 *
 * @param {string} memberId - 아이디
 * @param {string} memberPwd - 비밀번호
 */
async function login(memberId, memberPwd) {
    showLoading();

    try {
        // Spring Security form login 사용
        // POST /login/amall.com

        const formData = new FormData();
        formData.append('memberId', memberId);
        formData.append('memberPwd', memberPwd);

        const response = await fetch('/login/amall.com', {
            method: 'POST',
            body: formData,
            redirect: 'manual' // 리다이렉트 수동 처리
        });

        hideLoading();

        // 성공 시 리다이렉트됨
        if (response.type === 'opaqueredirect' || response.redirected) {
            showToast('로그인 성공!', 'success');
            // 페이지 새로고침
            window.location.href = '/amall.com';
            return true;
        }

        // 실패
        showToast('아이디 또는 비밀번호가 올바르지 않습니다.', 'error');
        return false;

    } catch (error) {
        hideLoading();
        showToast('로그인에 실패했습니다.', 'error');
        return false;
    }
}

/**
 * 로그아웃
 */
async function logout() {
    const confirmed = await confirm('정말 로그아웃 하시겠습니까?');

    if (!confirmed) {
        return;
    }

    showLoading();

    try {
        // Spring Security logout
        window.location.href = '/logout/amall.com';
    } catch (error) {
        hideLoading();
        showToast('로그아웃에 실패했습니다.', 'error');
    }
}

/**
 * 회원 정보 조회
 *
 * @param {string} memberId - 회원 ID
 * @returns {Promise<Object>} 회원 정보
 */
async function getMember(memberId) {
    try {
        const response = await apiClient.get(`/api/members/${memberId}`);

        if (response.success) {
            return response.data;
        }

        return null;
    } catch (error) {
        console.error('회원 정보 조회 실패:', error);
        showToast('회원 정보를 불러오지 못했습니다.', 'error');
        return null;
    }
}

/**
 * 회원 정보 수정
 *
 * @param {string} memberId - 회원 ID
 * @param {Object} updateData - 수정할 정보
 * @returns {Promise<Object>} 수정된 회원 정보
 */
async function updateMember(memberId, updateData) {
    showLoading();

    try {
        const response = await apiClient.put(`/api/members/${memberId}`, updateData);

        hideLoading();

        if (response.success) {
            showToast('회원 정보가 수정되었습니다.', 'success');
            return response.data;
        }

        return null;
    } catch (error) {
        hideLoading();
        showToast(error.message || '회원 정보 수정에 실패했습니다.', 'error');
        return null;
    }
}

/**
 * 회원 탈퇴
 *
 * @param {string} memberId - 회원 ID
 * @returns {Promise<boolean>} 성공 여부
 */
async function deleteMember(memberId) {
    const confirmed = await confirm('정말 탈퇴하시겠습니까? 이 작업은 되돌릴 수 없습니다.');

    if (!confirmed) {
        return false;
    }

    showLoading();

    try {
        const response = await apiClient.delete(`/api/members/${memberId}`);

        hideLoading();

        if (response.success) {
            showToast('계정이 삭제되었습니다.', 'success');
            setTimeout(() => {
                window.location.href = '/amall.com';
            }, 1500);
            return true;
        }

        return false;
    } catch (error) {
        hideLoading();
        showToast(error.message || '회원 탈퇴에 실패했습니다.', 'error');
        return false;
    }
}
