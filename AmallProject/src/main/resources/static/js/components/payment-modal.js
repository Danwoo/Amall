/**
 * 결제 수단 선택 모달 컴포넌트
 *
 * 사용법:
 * const paymentMethod = await showPaymentModal();
 * if (paymentMethod) {
 *   // 결제 수단 사용 (예: 'CARD', 'BANK_TRANSFER', 'VIRTUAL_ACCOUNT', 'PHONE', 'KAKAOPAY', 'NAVERPAY')
 * }
 *
 * 필수사항:
 * - ui-utils.js (showToast 함수)
 */

/**
 * 결제 수단 선택 모달 표시
 *
 * @returns {Promise<string|null>} - 선택된 결제 수단 코드 또는 null (취소 시)
 */
function showPaymentModal() {
    return new Promise((resolve, reject) => {
        const modal = createPaymentModal(resolve, reject);
        document.body.appendChild(modal);

        // 첫 번째 라디오 버튼에 포커스
        setTimeout(() => {
            const firstRadio = modal.querySelector('input[name="paymentMethod"]');
            if (firstRadio) firstRadio.focus();
        }, 100);
    });
}

/**
 * 결제 수단 선택 모달 생성
 *
 * @param {Function} resolve - Promise resolve 함수
 * @param {Function} reject - Promise reject 함수
 * @returns {HTMLElement} 모달 요소
 */
function createPaymentModal(resolve, reject) {
    const overlay = document.createElement('div');
    overlay.className = 'payment-modal-overlay';
    overlay.setAttribute('role', 'dialog');
    overlay.setAttribute('aria-modal', 'true');
    overlay.setAttribute('aria-labelledby', 'payment-modal-title');

    const dialog = document.createElement('div');
    dialog.className = 'payment-modal-dialog';

    // 모달 헤더
    const header = createPaymentModalHeader();
    dialog.appendChild(header);

    // 모달 본문 (결제 수단 옵션)
    const body = createPaymentModalBody();
    dialog.appendChild(body);

    // 모달 푸터 (버튼)
    const footer = createPaymentModalFooter(overlay, body, resolve, reject);
    dialog.appendChild(footer);

    // 스타일 적용
    applyPaymentModalStyles(overlay, dialog);

    // 키보드 이벤트 (ESC로 닫기)
    overlay.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            closePaymentModal(overlay, reject);
        }
    });

    // 오버레이 클릭 시 닫기
    overlay.addEventListener('click', (e) => {
        if (e.target === overlay) {
            closePaymentModal(overlay, reject);
        }
    });

    overlay.appendChild(dialog);
    return overlay;
}

/**
 * 모달 헤더 생성
 */
function createPaymentModalHeader() {
    const header = document.createElement('div');
    header.className = 'payment-modal-header';

    const title = document.createElement('h2');
    title.id = 'payment-modal-title';
    title.textContent = '결제 수단 선택';
    title.style.margin = '0';
    title.style.fontSize = '20px';
    title.style.fontWeight = '600';
    title.style.color = '#111';

    header.appendChild(title);

    Object.assign(header.style, {
        padding: '20px 24px',
        borderBottom: '1px solid #e5e7eb'
    });

    return header;
}

/**
 * 결제 수단 옵션 정의
 */
const PAYMENT_METHODS = [
    {
        code: 'CARD',
        label: '신용/체크카드',
        description: '카드 결제'
    },
    {
        code: 'BANK_TRANSFER',
        label: '계좌이체',
        description: '실시간 계좌이체'
    },
    {
        code: 'VIRTUAL_ACCOUNT',
        label: '가상계좌',
        description: '가상계좌 입금'
    },
    {
        code: 'PHONE',
        label: '휴대폰 결제',
        description: '휴대폰 소액결제'
    },
    {
        code: 'KAKAOPAY',
        label: '카카오페이',
        description: '카카오페이 간편결제'
    },
    {
        code: 'NAVERPAY',
        label: '네이버페이',
        description: '네이버페이 간편결제'
    }
];

/**
 * 모달 본문 생성 (결제 수단 옵션)
 */
function createPaymentModalBody() {
    const body = document.createElement('div');
    body.className = 'payment-modal-body';

    Object.assign(body.style, {
        padding: '24px',
        maxHeight: '60vh',
        overflowY: 'auto'
    });

    // 결제 수단 옵션 컨테이너
    const optionsContainer = document.createElement('div');
    optionsContainer.className = 'payment-options-container';
    optionsContainer.setAttribute('role', 'radiogroup');
    optionsContainer.setAttribute('aria-labelledby', 'payment-modal-title');

    // 각 결제 수단 옵션 생성
    PAYMENT_METHODS.forEach((method, index) => {
        const option = createPaymentOption(method, index === 0);
        optionsContainer.appendChild(option);
    });

    body.appendChild(optionsContainer);

    // 키보드 네비게이션 설정
    setupKeyboardNavigation(optionsContainer);

    return body;
}

/**
 * 결제 수단 옵션 생성
 *
 * @param {Object} method - 결제 수단 정보
 * @param {boolean} isDefault - 기본 선택 여부
 */
function createPaymentOption(method, isDefault) {
    const option = document.createElement('label');
    option.className = 'payment-option';
    option.setAttribute('data-payment-code', method.code);

    Object.assign(option.style, {
        display: 'flex',
        alignItems: 'center',
        padding: '16px',
        marginBottom: '12px',
        border: '2px solid #e5e7eb',
        borderRadius: '8px',
        cursor: 'pointer',
        transition: 'all 0.2s',
        backgroundColor: 'white'
    });

    // 라디오 버튼
    const radio = document.createElement('input');
    radio.type = 'radio';
    radio.name = 'paymentMethod';
    radio.value = method.code;
    radio.checked = isDefault;
    radio.setAttribute('aria-label', method.label);

    Object.assign(radio.style, {
        width: '20px',
        height: '20px',
        marginRight: '12px',
        cursor: 'pointer',
        accentColor: '#3b82f6'
    });

    // 라벨 정보 컨테이너
    const labelContainer = document.createElement('div');
    labelContainer.style.flex = '1';

    // 결제 수단 이름
    const labelText = document.createElement('div');
    labelText.textContent = method.label;
    labelText.style.fontSize = '16px';
    labelText.style.fontWeight = '500';
    labelText.style.color = '#111';
    labelText.style.marginBottom = '4px';

    // 결제 수단 설명
    const description = document.createElement('div');
    description.textContent = method.description;
    description.style.fontSize = '14px';
    description.style.color = '#6b7280';

    labelContainer.appendChild(labelText);
    labelContainer.appendChild(description);

    option.appendChild(radio);
    option.appendChild(labelContainer);

    // 호버 효과
    option.addEventListener('mouseenter', () => {
        if (!radio.checked) {
            option.style.borderColor = '#3b82f6';
            option.style.backgroundColor = '#f0f9ff';
        }
    });

    option.addEventListener('mouseleave', () => {
        if (!radio.checked) {
            option.style.borderColor = '#e5e7eb';
            option.style.backgroundColor = 'white';
        }
    });

    // 선택 시 시각적 피드백
    radio.addEventListener('change', () => {
        updatePaymentOptionStyles(option.parentElement);
    });

    // 기본 선택 항목 스타일
    if (isDefault) {
        option.style.borderColor = '#3b82f6';
        option.style.backgroundColor = '#eff6ff';
    }

    return option;
}

/**
 * 결제 수단 옵션 스타일 업데이트
 */
function updatePaymentOptionStyles(container) {
    const options = container.querySelectorAll('.payment-option');
    options.forEach(option => {
        const radio = option.querySelector('input[type="radio"]');
        if (radio.checked) {
            option.style.borderColor = '#3b82f6';
            option.style.backgroundColor = '#eff6ff';
        } else {
            option.style.borderColor = '#e5e7eb';
            option.style.backgroundColor = 'white';
        }
    });
}

/**
 * 키보드 네비게이션 설정
 */
function setupKeyboardNavigation(container) {
    const radios = container.querySelectorAll('input[name="paymentMethod"]');

    radios.forEach((radio, index) => {
        radio.addEventListener('keydown', (e) => {
            let targetIndex = index;

            switch(e.key) {
                case 'ArrowDown':
                case 'ArrowRight':
                    e.preventDefault();
                    targetIndex = (index + 1) % radios.length;
                    radios[targetIndex].focus();
                    radios[targetIndex].checked = true;
                    updatePaymentOptionStyles(container);
                    break;

                case 'ArrowUp':
                case 'ArrowLeft':
                    e.preventDefault();
                    targetIndex = (index - 1 + radios.length) % radios.length;
                    radios[targetIndex].focus();
                    radios[targetIndex].checked = true;
                    updatePaymentOptionStyles(container);
                    break;

                case 'Enter':
                case ' ':
                    e.preventDefault();
                    radio.checked = true;
                    updatePaymentOptionStyles(container);
                    // Enter 키로 확인 버튼 클릭
                    if (e.key === 'Enter') {
                        const confirmBtn = container.closest('.payment-modal-dialog').querySelector('.payment-modal-btn-submit');
                        if (confirmBtn) {
                            confirmBtn.click();
                        }
                    }
                    break;
            }
        });
    });
}

/**
 * 모달 푸터 생성 (버튼)
 */
function createPaymentModalFooter(overlay, body, resolve, reject) {
    const footer = document.createElement('div');
    footer.className = 'payment-modal-footer';

    Object.assign(footer.style, {
        padding: '16px 24px',
        borderTop: '1px solid #e5e7eb',
        display: 'flex',
        gap: '10px',
        justifyContent: 'flex-end'
    });

    // 취소 버튼
    const cancelBtn = document.createElement('button');
    cancelBtn.type = 'button';
    cancelBtn.textContent = '취소';
    cancelBtn.className = 'payment-modal-btn-cancel';

    Object.assign(cancelBtn.style, {
        padding: '10px 20px',
        backgroundColor: '#e5e7eb',
        color: '#374151',
        border: 'none',
        borderRadius: '6px',
        fontSize: '14px',
        fontWeight: '500',
        cursor: 'pointer',
        transition: 'background-color 0.2s'
    });

    cancelBtn.addEventListener('mouseenter', () => {
        cancelBtn.style.backgroundColor = '#d1d5db';
    });

    cancelBtn.addEventListener('mouseleave', () => {
        cancelBtn.style.backgroundColor = '#e5e7eb';
    });

    cancelBtn.addEventListener('click', () => {
        closePaymentModal(overlay, reject);
    });

    // 확인 버튼
    const submitBtn = document.createElement('button');
    submitBtn.type = 'button';
    submitBtn.textContent = '결제 수단 선택';
    submitBtn.className = 'payment-modal-btn-submit';

    Object.assign(submitBtn.style, {
        padding: '10px 20px',
        backgroundColor: '#3b82f6',
        color: 'white',
        border: 'none',
        borderRadius: '6px',
        fontSize: '14px',
        fontWeight: '500',
        cursor: 'pointer',
        transition: 'background-color 0.2s'
    });

    submitBtn.addEventListener('mouseenter', () => {
        submitBtn.style.backgroundColor = '#2563eb';
    });

    submitBtn.addEventListener('mouseleave', () => {
        submitBtn.style.backgroundColor = '#3b82f6';
    });

    submitBtn.addEventListener('click', () => {
        handlePaymentSubmit(overlay, body, resolve);
    });

    footer.appendChild(cancelBtn);
    footer.appendChild(submitBtn);

    return footer;
}

/**
 * 결제 수단 선택 처리
 */
function handlePaymentSubmit(overlay, body, resolve) {
    const selectedRadio = body.querySelector('input[name="paymentMethod"]:checked');

    if (!selectedRadio) {
        if (typeof showToast !== 'undefined') {
            showToast('결제 수단을 선택해주세요.', 'error');
        } else {
            alert('결제 수단을 선택해주세요.');
        }
        return;
    }

    const paymentMethod = selectedRadio.value;

    // 성공: 모달 닫고 결제 수단 반환
    closePaymentModal(overlay, null);
    resolve(paymentMethod);
}

/**
 * 모달 닫기
 */
function closePaymentModal(overlay, reject) {
    // 페이드 아웃 애니메이션
    overlay.style.opacity = '0';

    setTimeout(() => {
        overlay.remove();
        if (reject) {
            reject(null);
        }
    }, 200);
}

/**
 * 모달 스타일 적용
 */
function applyPaymentModalStyles(overlay, dialog) {
    // 오버레이 스타일
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
        zIndex: '10000',
        opacity: '0',
        transition: 'opacity 0.2s'
    });

    // 페이드 인 애니메이션
    setTimeout(() => {
        overlay.style.opacity = '1';
    }, 10);

    // 다이얼로그 스타일
    Object.assign(dialog.style, {
        backgroundColor: 'white',
        borderRadius: '12px',
        width: '500px',
        maxWidth: '90%',
        maxHeight: '90vh',
        boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)',
        display: 'flex',
        flexDirection: 'column',
        overflow: 'hidden'
    });

    // 반응형 디자인
    if (window.innerWidth < 640) {
        dialog.style.width = '90%';
    }
}
