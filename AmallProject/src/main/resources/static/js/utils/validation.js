/**
 * 폼 검증 유틸리티
 *
 * Backend DTO 검증과 동일한 규칙 적용
 */

/**
 * 검증 규칙
 */
const ValidationRules = {
    /**
     * 아이디 검증
     * - 4-20자
     * - 영문, 숫자, _, - 만 가능
     */
    memberId: (value) => {
        if (!value || value.trim().length === 0) {
            return '아이디는 필수입니다.';
        }
        if (value.length < 4 || value.length > 20) {
            return '아이디는 4-20자여야 합니다.';
        }
        if (!/^[a-zA-Z0-9_-]+$/.test(value)) {
            return '아이디는 영문, 숫자, _, -만 사용 가능합니다.';
        }
        return null;
    },

    /**
     * 비밀번호 검증
     * - 8-100자
     * - 영문, 숫자, 특수문자 포함
     */
    memberPwd: (value) => {
        if (!value || value.trim().length === 0) {
            return '비밀번호는 필수입니다.';
        }
        if (value.length < 8 || value.length > 100) {
            return '비밀번호는 8-100자여야 합니다.';
        }
        if (!/^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]+$/.test(value)) {
            return '비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.';
        }
        return null;
    },

    /**
     * 비밀번호 확인
     */
    memberPwdCheck: (value, originalPwd) => {
        if (!value || value.trim().length === 0) {
            return '비밀번호 확인은 필수입니다.';
        }
        if (value !== originalPwd) {
            return '비밀번호가 일치하지 않습니다.';
        }
        return null;
    },

    /**
     * 이메일 검증
     */
    memberEmail: (value) => {
        if (!value || value.trim().length === 0) {
            return '이메일은 필수입니다.';
        }
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(value)) {
            return '올바른 이메일 형식이 아닙니다.';
        }
        return null;
    },

    /**
     * 이름 검증
     */
    memberName: (value) => {
        if (!value || value.trim().length === 0) {
            return '이름은 필수입니다.';
        }
        if (value.length < 2 || value.length > 50) {
            return '이름은 2-50자여야 합니다.';
        }
        return null;
    },

    /**
     * 전화번호 검증
     */
    memberPhone: (value) => {
        if (!value || value.trim().length === 0) {
            return '전화번호는 필수입니다.';
        }
        // 010-1234-5678 또는 01012345678 형식
        const phoneRegex = /^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$/;
        if (!phoneRegex.test(value)) {
            return '올바른 전화번호 형식이 아닙니다. (예: 010-1234-5678)';
        }
        return null;
    },

    /**
     * 주소 검증
     */
    memberAddress: (value) => {
        if (!value || value.trim().length === 0) {
            return '주소는 필수입니다.';
        }
        return null;
    },

    /**
     * 우편번호 검증
     */
    memberPostCode: (value) => {
        if (!value || value.trim().length === 0) {
            return '우편번호는 필수입니다.';
        }
        return null;
    }
};

/**
 * 폼 검증
 *
 * @param {Object} formData - 검증할 폼 데이터
 * @param {Array<string>} fields - 검증할 필드 목록
 * @returns {Object} { isValid: boolean, errors: Object }
 */
function validateForm(formData, fields) {
    const errors = {};
    let isValid = true;

    fields.forEach(field => {
        const validator = ValidationRules[field];
        if (!validator) {
            console.warn(`검증 규칙이 없습니다: ${field}`);
            return;
        }

        let errorMessage;

        // 비밀번호 확인은 특별 처리
        if (field === 'memberPwdCheck') {
            errorMessage = validator(formData[field], formData.memberPwd);
        } else {
            errorMessage = validator(formData[field]);
        }

        if (errorMessage) {
            errors[field] = errorMessage;
            isValid = false;
        }
    });

    return { isValid, errors };
}

/**
 * 실시간 필드 검증
 *
 * @param {HTMLElement} field - 검증할 필드
 * @param {Function} callback - 검증 결과 콜백
 */
function setupFieldValidation(field, callback) {
    const fieldName = field.getAttribute('name');
    const validator = ValidationRules[fieldName];

    if (!validator) {
        console.warn(`검증 규칙이 없습니다: ${fieldName}`);
        return;
    }

    field.addEventListener('blur', () => {
        const errorMessage = validator(field.value);

        if (errorMessage) {
            showFieldError(fieldName, errorMessage);
            if (callback) callback(false, errorMessage);
        } else {
            clearFieldError(fieldName);
            if (callback) callback(true, null);
        }
    });

    field.addEventListener('input', () => {
        // 입력 중에는 에러만 제거
        clearFieldError(fieldName);
    });
}

/**
 * 필드 에러 표시 (ui-utils.js와 동일)
 */
function showFieldError(fieldName, errorMessage) {
    const field = document.querySelector(`[name="${fieldName}"]`);
    if (!field) return;

    clearFieldError(fieldName);

    const errorEl = document.createElement('div');
    errorEl.className = 'field-error';
    errorEl.textContent = errorMessage;

    Object.assign(errorEl.style, {
        color: '#ef4444',
        fontSize: '12px',
        marginTop: '4px'
    });

    field.style.borderColor = '#ef4444';
    field.parentElement.appendChild(errorEl);
}

/**
 * 필드 에러 제거
 */
function clearFieldError(fieldName) {
    const field = document.querySelector(`[name="${fieldName}"]`);
    if (!field) return;

    const existingError = field.parentElement.querySelector('.field-error');
    if (existingError) {
        existingError.remove();
    }

    field.style.borderColor = '';
}

/**
 * 모든 필드 에러 제거
 */
function clearAllFieldErrors() {
    document.querySelectorAll('.field-error').forEach(el => el.remove());
    document.querySelectorAll('input, textarea, select').forEach(el => {
        el.style.borderColor = '';
    });
}
