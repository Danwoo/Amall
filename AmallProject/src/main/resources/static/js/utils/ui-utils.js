/**
 * UI 유틸리티
 *
 * Toast 메시지, 로딩 스피너, 확인 다이얼로그 등
 */

/**
 * Toast 메시지 표시
 *
 * @param {string} message - 표시할 메시지
 * @param {string} type - 'success' | 'error' | 'warning' | 'info'
 * @param {number} duration - 표시 시간 (ms)
 */
function showToast(message, type = 'info', duration = 3000) {
    // 기존 toast 제거
    const existingToast = document.querySelector('.toast-message');
    if (existingToast) {
        existingToast.remove();
    }

    // Toast 생성
    const toast = document.createElement('div');
    toast.className = `toast-message toast-${type}`;
    toast.textContent = message;

    // 스타일
    Object.assign(toast.style, {
        position: 'fixed',
        top: '20px',
        right: '20px',
        padding: '16px 24px',
        borderRadius: '8px',
        backgroundColor: getToastColor(type),
        color: 'white',
        fontSize: '14px',
        fontWeight: '500',
        boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)',
        zIndex: '10000',
        animation: 'slideInRight 0.3s ease-out',
        maxWidth: '400px',
        wordBreak: 'keep-all'
    });

    document.body.appendChild(toast);

    // 자동 제거
    setTimeout(() => {
        toast.style.animation = 'slideOutRight 0.3s ease-in';
        setTimeout(() => toast.remove(), 300);
    }, duration);
}

/**
 * Toast 색상 결정
 */
function getToastColor(type) {
    const colors = {
        success: '#10b981',
        error: '#ef4444',
        warning: '#f59e0b',
        info: '#3b82f6'
    };
    return colors[type] || colors.info;
}

/**
 * 로딩 스피너 표시
 */
function showLoading() {
    // 이미 있으면 무시
    if (document.querySelector('.loading-overlay')) {
        return;
    }

    const overlay = document.createElement('div');
    overlay.className = 'loading-overlay';

    const spinner = document.createElement('div');
    spinner.className = 'loading-spinner';

    // 스타일
    Object.assign(overlay.style, {
        position: 'fixed',
        top: '0',
        left: '0',
        width: '100%',
        height: '100%',
        backgroundColor: 'rgba(0, 0, 0, 0.5)',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        zIndex: '9999'
    });

    Object.assign(spinner.style, {
        width: '50px',
        height: '50px',
        border: '4px solid #f3f3f3',
        borderTop: '4px solid #3b82f6',
        borderRadius: '50%',
        animation: 'spin 1s linear infinite'
    });

    overlay.appendChild(spinner);
    document.body.appendChild(overlay);

    // 애니메이션 추가
    addSpinAnimation();
}

/**
 * 로딩 스피너 숨기기
 */
function hideLoading() {
    const overlay = document.querySelector('.loading-overlay');
    if (overlay) {
        overlay.remove();
    }
}

/**
 * 확인 다이얼로그
 *
 * @param {string} message - 확인 메시지
 * @returns {Promise<boolean>} - 확인: true, 취소: false
 */
function confirm(message) {
    return new Promise((resolve) => {
        const overlay = document.createElement('div');
        overlay.className = 'confirm-overlay';

        const dialog = document.createElement('div');
        dialog.className = 'confirm-dialog';

        const messageEl = document.createElement('p');
        messageEl.textContent = message;
        messageEl.style.marginBottom = '20px';

        const buttonContainer = document.createElement('div');
        buttonContainer.style.display = 'flex';
        buttonContainer.style.gap = '10px';
        buttonContainer.style.justifyContent = 'flex-end';

        const cancelBtn = document.createElement('button');
        cancelBtn.textContent = '취소';
        cancelBtn.className = 'confirm-btn confirm-btn-cancel';
        cancelBtn.onclick = () => {
            overlay.remove();
            resolve(false);
        };

        const confirmBtn = document.createElement('button');
        confirmBtn.textContent = '확인';
        confirmBtn.className = 'confirm-btn confirm-btn-confirm';
        confirmBtn.onclick = () => {
            overlay.remove();
            resolve(true);
        };

        // 스타일
        Object.assign(overlay.style, {
            position: 'fixed',
            top: '0',
            left: '0',
            width: '100%',
            height: '100%',
            backgroundColor: 'rgba(0, 0, 0, 0.5)',
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            zIndex: '9999'
        });

        Object.assign(dialog.style, {
            backgroundColor: 'white',
            padding: '24px',
            borderRadius: '8px',
            minWidth: '300px',
            maxWidth: '500px',
            boxShadow: '0 4px 20px rgba(0, 0, 0, 0.15)'
        });

        const btnStyle = {
            padding: '8px 16px',
            borderRadius: '4px',
            border: 'none',
            cursor: 'pointer',
            fontSize: '14px',
            fontWeight: '500'
        };

        Object.assign(cancelBtn.style, {
            ...btnStyle,
            backgroundColor: '#e5e7eb',
            color: '#374151'
        });

        Object.assign(confirmBtn.style, {
            ...btnStyle,
            backgroundColor: '#3b82f6',
            color: 'white'
        });

        buttonContainer.appendChild(cancelBtn);
        buttonContainer.appendChild(confirmBtn);

        dialog.appendChild(messageEl);
        dialog.appendChild(buttonContainer);
        overlay.appendChild(dialog);
        document.body.appendChild(overlay);
    });
}

/**
 * 필드 에러 표시
 *
 * @param {string} fieldName - 필드 이름
 * @param {string} errorMessage - 에러 메시지
 */
function showFieldError(fieldName, errorMessage) {
    const field = document.querySelector(`[name="${fieldName}"]`);
    if (!field) return;

    // 기존 에러 메시지 제거
    const existingError = field.parentElement.querySelector('.field-error');
    if (existingError) {
        existingError.remove();
    }

    // 에러 메시지 생성
    const errorEl = document.createElement('div');
    errorEl.className = 'field-error';
    errorEl.textContent = errorMessage;

    Object.assign(errorEl.style, {
        color: '#ef4444',
        fontSize: '12px',
        marginTop: '4px'
    });

    // 필드 스타일
    field.style.borderColor = '#ef4444';

    field.parentElement.appendChild(errorEl);
}

/**
 * 모든 필드 에러 제거
 */
function clearFieldErrors() {
    document.querySelectorAll('.field-error').forEach(el => el.remove());
    document.querySelectorAll('input, textarea, select').forEach(el => {
        el.style.borderColor = '';
    });
}

/**
 * Spin 애니메이션 추가 (한 번만)
 */
function addSpinAnimation() {
    if (document.querySelector('#spin-animation-style')) {
        return;
    }

    const style = document.createElement('style');
    style.id = 'spin-animation-style';
    style.textContent = `
        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }

        @keyframes slideInRight {
            from {
                transform: translateX(100%);
                opacity: 0;
            }
            to {
                transform: translateX(0);
                opacity: 1;
            }
        }

        @keyframes slideOutRight {
            from {
                transform: translateX(0);
                opacity: 1;
            }
            to {
                transform: translateX(100%);
                opacity: 0;
            }
        }
    `;
    document.head.appendChild(style);
}

// 초기화
addSpinAnimation();
