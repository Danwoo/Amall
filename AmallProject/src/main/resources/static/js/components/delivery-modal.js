/**
 * 배송지 입력 모달 컴포넌트
 *
 * 사용법:
 * const deliveryInfo = await showDeliveryModal();
 * if (deliveryInfo) {
 *   // 배송지 정보 사용
 * }
 *
 * 필수사항:
 * - ui-utils.js (escapeHtml 함수)
 * - validation.js (검증 규칙)
 * - Daum Postcode API 스크립트 (주소 검색용)
 *
 * Daum Postcode API 스크립트 추가 방법:
 * HTML에 다음 스크립트 태그를 추가하세요:
 * <script src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>
 */

/**
 * 배송지 입력 모달 표시
 *
 * @returns {Promise<Object|null>} - 배송 정보 객체 또는 null (취소 시)
 */
function showDeliveryModal() {
    return new Promise((resolve, reject) => {
        const modal = createDeliveryModal(resolve, reject);
        document.body.appendChild(modal);

        // 첫 번째 입력 필드에 포커스
        setTimeout(() => {
            const firstInput = modal.querySelector('input[name="deliveryName"]');
            if (firstInput) firstInput.focus();
        }, 100);
    });
}

/**
 * 배송지 모달 생성
 *
 * @param {Function} resolve - Promise resolve 함수
 * @param {Function} reject - Promise reject 함수
 * @returns {HTMLElement} 모달 요소
 */
function createDeliveryModal(resolve, reject) {
    const overlay = document.createElement('div');
    overlay.className = 'delivery-modal-overlay';
    overlay.setAttribute('role', 'dialog');
    overlay.setAttribute('aria-modal', 'true');
    overlay.setAttribute('aria-labelledby', 'delivery-modal-title');

    const dialog = document.createElement('div');
    dialog.className = 'delivery-modal-dialog';

    // 모달 헤더
    const header = createModalHeader();
    dialog.appendChild(header);

    // 모달 본문 (폼)
    const form = createDeliveryForm();
    dialog.appendChild(form);

    // 모달 푸터 (버튼)
    const footer = createModalFooter(overlay, form, resolve, reject);
    dialog.appendChild(footer);

    // 스타일 적용
    applyModalStyles(overlay, dialog);

    // 키보드 이벤트 (ESC로 닫기)
    overlay.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            closeModal(overlay, reject);
        }
    });

    // 오버레이 클릭 시 닫기
    overlay.addEventListener('click', (e) => {
        if (e.target === overlay) {
            closeModal(overlay, reject);
        }
    });

    overlay.appendChild(dialog);
    return overlay;
}

/**
 * 모달 헤더 생성
 */
function createModalHeader() {
    const header = document.createElement('div');
    header.className = 'delivery-modal-header';

    const title = document.createElement('h2');
    title.id = 'delivery-modal-title';
    title.textContent = '배송지 정보 입력';
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
 * 배송지 입력 폼 생성
 */
function createDeliveryForm() {
    const form = document.createElement('form');
    form.className = 'delivery-modal-form';
    form.setAttribute('novalidate', '');

    Object.assign(form.style, {
        padding: '24px',
        maxHeight: '60vh',
        overflowY: 'auto'
    });

    // 폼 필드 정의
    const fields = [
        {
            name: 'deliveryName',
            label: '받는 사람',
            type: 'text',
            required: true,
            placeholder: '이름을 입력해주세요',
            validator: 'memberName'
        },
        {
            name: 'deliveryPhone',
            label: '전화번호',
            type: 'tel',
            required: true,
            placeholder: '010-1234-5678',
            validator: 'memberPhone'
        },
        {
            name: 'deliveryPostCode',
            label: '우편번호',
            type: 'text',
            required: true,
            placeholder: '우편번호',
            readonly: true,
            hasButton: true,
            buttonText: '주소 검색',
            buttonHandler: openAddressSearch
        },
        {
            name: 'deliveryAddress',
            label: '주소',
            type: 'text',
            required: true,
            placeholder: '주소',
            readonly: true
        },
        {
            name: 'deliveryDetailAddress',
            label: '상세주소',
            type: 'text',
            required: false,
            placeholder: '상세주소를 입력해주세요 (선택)'
        },
        {
            name: 'deliveryMessage',
            label: '배송 메시지',
            type: 'textarea',
            required: false,
            placeholder: '배송 시 요청사항을 입력해주세요 (선택)',
            rows: 3
        }
    ];

    // 각 필드 생성
    fields.forEach(field => {
        const fieldGroup = createFormField(field);
        form.appendChild(fieldGroup);
    });

    // 폼 제출 방지
    form.addEventListener('submit', (e) => {
        e.preventDefault();
    });

    return form;
}

/**
 * 폼 필드 생성
 */
function createFormField(fieldConfig) {
    const fieldGroup = document.createElement('div');
    fieldGroup.className = 'form-field-group';

    Object.assign(fieldGroup.style, {
        marginBottom: '20px'
    });

    // 라벨
    const label = document.createElement('label');
    label.setAttribute('for', fieldConfig.name);
    label.textContent = fieldConfig.label;

    if (fieldConfig.required) {
        const required = document.createElement('span');
        required.textContent = ' *';
        required.style.color = '#ef4444';
        label.appendChild(required);
    }

    Object.assign(label.style, {
        display: 'block',
        marginBottom: '8px',
        fontSize: '14px',
        fontWeight: '500',
        color: '#374151'
    });

    fieldGroup.appendChild(label);

    // 입력 컨트롤 컨테이너 (버튼이 있는 경우)
    const inputContainer = document.createElement('div');
    if (fieldConfig.hasButton) {
        inputContainer.style.display = 'flex';
        inputContainer.style.gap = '8px';
    }

    // 입력 필드
    let input;
    if (fieldConfig.type === 'textarea') {
        input = document.createElement('textarea');
        input.rows = fieldConfig.rows || 3;
    } else {
        input = document.createElement('input');
        input.type = fieldConfig.type;
    }

    input.name = fieldConfig.name;
    input.id = fieldConfig.name;
    input.placeholder = fieldConfig.placeholder || '';

    if (fieldConfig.required) {
        input.setAttribute('aria-required', 'true');
    }

    if (fieldConfig.readonly) {
        input.readOnly = true;
        input.style.backgroundColor = '#f9fafb';
        input.style.cursor = 'not-allowed';
    }

    // 입력 필드 공통 스타일
    Object.assign(input.style, {
        width: '100%',
        padding: '10px 12px',
        border: '1px solid #d1d5db',
        borderRadius: '6px',
        fontSize: '14px',
        fontFamily: 'inherit',
        transition: 'border-color 0.2s',
        boxSizing: 'border-box'
    });

    // 포커스 스타일
    input.addEventListener('focus', () => {
        if (!input.readOnly) {
            input.style.borderColor = '#3b82f6';
            input.style.outline = 'none';
        }
    });

    input.addEventListener('blur', () => {
        input.style.borderColor = '#d1d5db';
    });

    // 실시간 검증 설정 (validator가 있는 경우)
    if (fieldConfig.validator && typeof ValidationRules !== 'undefined') {
        setupFieldValidationForDelivery(input, fieldConfig.validator);
    }

    inputContainer.appendChild(input);

    // 버튼 (주소 검색 등)
    if (fieldConfig.hasButton) {
        const button = document.createElement('button');
        button.type = 'button';
        button.textContent = fieldConfig.buttonText;
        button.className = 'address-search-btn';

        Object.assign(button.style, {
            padding: '10px 16px',
            backgroundColor: '#3b82f6',
            color: 'white',
            border: 'none',
            borderRadius: '6px',
            fontSize: '14px',
            fontWeight: '500',
            cursor: 'pointer',
            whiteSpace: 'nowrap',
            transition: 'background-color 0.2s'
        });

        button.addEventListener('mouseenter', () => {
            button.style.backgroundColor = '#2563eb';
        });

        button.addEventListener('mouseleave', () => {
            button.style.backgroundColor = '#3b82f6';
        });

        button.addEventListener('click', () => {
            if (fieldConfig.buttonHandler) {
                fieldConfig.buttonHandler();
            }
        });

        inputContainer.appendChild(button);
    }

    fieldGroup.appendChild(inputContainer);

    return fieldGroup;
}

/**
 * 배송지 필드 실시간 검증 설정
 */
function setupFieldValidationForDelivery(field, validatorName) {
    if (typeof ValidationRules === 'undefined' || !ValidationRules[validatorName]) {
        return;
    }

    const validator = ValidationRules[validatorName];

    field.addEventListener('blur', () => {
        const errorMessage = validator(field.value);

        if (errorMessage) {
            showDeliveryFieldError(field, errorMessage);
        } else {
            clearDeliveryFieldError(field);
        }
    });

    field.addEventListener('input', () => {
        clearDeliveryFieldError(field);
    });
}

/**
 * 배송지 필드 에러 표시
 */
function showDeliveryFieldError(field, errorMessage) {
    clearDeliveryFieldError(field);

    const errorEl = document.createElement('div');
    errorEl.className = 'delivery-field-error';
    errorEl.textContent = errorMessage;
    errorEl.setAttribute('role', 'alert');

    Object.assign(errorEl.style, {
        color: '#ef4444',
        fontSize: '12px',
        marginTop: '4px'
    });

    field.style.borderColor = '#ef4444';
    field.parentElement.appendChild(errorEl);
}

/**
 * 배송지 필드 에러 제거
 */
function clearDeliveryFieldError(field) {
    const container = field.parentElement;
    const existingError = container.querySelector('.delivery-field-error');
    if (existingError) {
        existingError.remove();
    }
    field.style.borderColor = '#d1d5db';
}

/**
 * 주소 검색 열기 (Daum Postcode API)
 */
function openAddressSearch() {
    // Daum Postcode API가 로드되어 있는지 확인
    if (typeof daum === 'undefined' || typeof daum.Postcode === 'undefined') {
        if (typeof showToast !== 'undefined') {
            showToast('주소 검색 API가 로드되지 않았습니다.\nHTML에 Daum Postcode 스크립트를 추가해주세요.', 'error', 5000);
        } else {
            alert('주소 검색 API가 로드되지 않았습니다.\nHTML에 Daum Postcode 스크립트를 추가해주세요:\n<script src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>');
        }
        return;
    }

    new daum.Postcode({
        oncomplete: function(data) {
            // 우편번호와 주소 정보를 입력 필드에 설정
            const postcodeInput = document.getElementById('deliveryPostCode');
            const addressInput = document.getElementById('deliveryAddress');
            const detailAddressInput = document.getElementById('deliveryDetailAddress');

            if (postcodeInput) {
                postcodeInput.value = data.zonecode;
                clearDeliveryFieldError(postcodeInput);
            }

            if (addressInput) {
                // 사용자가 선택한 주소 타입에 따라 주소 값 설정
                let fullAddress = data.userSelectedType === 'R' ? data.roadAddress : data.jibunAddress;

                // 건물명이 있을 경우 추가
                if (data.buildingName !== '') {
                    fullAddress += (fullAddress !== '' ? ', ' + data.buildingName : data.buildingName);
                }

                addressInput.value = fullAddress;
                clearDeliveryFieldError(addressInput);
            }

            // 상세주소 입력 필드에 포커스
            if (detailAddressInput) {
                detailAddressInput.focus();
            }
        }
    }).open();
}

/**
 * 모달 푸터 생성 (버튼)
 */
function createModalFooter(overlay, form, resolve, reject) {
    const footer = document.createElement('div');
    footer.className = 'delivery-modal-footer';

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
    cancelBtn.className = 'delivery-modal-btn-cancel';

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
        closeModal(overlay, reject);
    });

    // 확인 버튼
    const submitBtn = document.createElement('button');
    submitBtn.type = 'button';
    submitBtn.textContent = '배송지 정보 확인';
    submitBtn.className = 'delivery-modal-btn-submit';

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
        handleFormSubmit(overlay, form, resolve);
    });

    footer.appendChild(cancelBtn);
    footer.appendChild(submitBtn);

    return footer;
}

/**
 * 폼 제출 처리
 */
function handleFormSubmit(overlay, form, resolve) {
    // 모든 에러 제거
    form.querySelectorAll('.delivery-field-error').forEach(el => el.remove());
    form.querySelectorAll('input, textarea').forEach(el => {
        el.style.borderColor = '#d1d5db';
    });

    // 폼 데이터 수집
    const formData = new FormData(form);
    const deliveryInfo = {};

    formData.forEach((value, key) => {
        deliveryInfo[key] = value.trim();
    });

    // 검증
    const validationErrors = validateDeliveryInfo(deliveryInfo);

    if (Object.keys(validationErrors).length > 0) {
        // 에러 표시
        Object.entries(validationErrors).forEach(([fieldName, errorMessage]) => {
            const field = form.querySelector(`[name="${fieldName}"]`);
            if (field) {
                showDeliveryFieldError(field, errorMessage);
            }
        });

        // 첫 번째 에러 필드로 포커스
        const firstErrorField = form.querySelector(`[name="${Object.keys(validationErrors)[0]}"]`);
        if (firstErrorField) {
            firstErrorField.focus();
        }

        if (typeof showToast !== 'undefined') {
            showToast('입력 정보를 확인해주세요.', 'error');
        }

        return;
    }

    // XSS 방지: escapeHtml 적용
    const sanitizedDeliveryInfo = {};
    Object.entries(deliveryInfo).forEach(([key, value]) => {
        sanitizedDeliveryInfo[key] = typeof escapeHtml !== 'undefined' ? escapeHtml(value) : value;
    });

    // 성공: 모달 닫고 데이터 반환
    closeModal(overlay, null);

    // API에서 사용하는 필드명으로 매핑 (기존 코드와의 호환성)
    const result = {
        name: sanitizedDeliveryInfo.deliveryName,
        phone: sanitizedDeliveryInfo.deliveryPhone,
        postCode: sanitizedDeliveryInfo.deliveryPostCode,
        address: sanitizedDeliveryInfo.deliveryAddress,
        detailAddress: sanitizedDeliveryInfo.deliveryDetailAddress || '',
        message: sanitizedDeliveryInfo.deliveryMessage || ''
    };

    resolve(result);
}

/**
 * 배송 정보 검증
 */
function validateDeliveryInfo(deliveryInfo) {
    const errors = {};

    // 이름 검증
    if (!deliveryInfo.deliveryName || deliveryInfo.deliveryName.length === 0) {
        errors.deliveryName = '받는 사람 이름은 필수입니다.';
    } else if (deliveryInfo.deliveryName.length < 2 || deliveryInfo.deliveryName.length > 50) {
        errors.deliveryName = '이름은 2-50자여야 합니다.';
    }

    // 전화번호 검증
    if (!deliveryInfo.deliveryPhone || deliveryInfo.deliveryPhone.length === 0) {
        errors.deliveryPhone = '전화번호는 필수입니다.';
    } else {
        const phoneRegex = /^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$/;
        if (!phoneRegex.test(deliveryInfo.deliveryPhone)) {
            errors.deliveryPhone = '올바른 전화번호 형식이 아닙니다. (예: 010-1234-5678)';
        }
    }

    // 우편번호 검증
    if (!deliveryInfo.deliveryPostCode || deliveryInfo.deliveryPostCode.length === 0) {
        errors.deliveryPostCode = '우편번호는 필수입니다. 주소 검색 버튼을 이용해주세요.';
    }

    // 주소 검증
    if (!deliveryInfo.deliveryAddress || deliveryInfo.deliveryAddress.length === 0) {
        errors.deliveryAddress = '주소는 필수입니다. 주소 검색 버튼을 이용해주세요.';
    }

    return errors;
}

/**
 * 모달 닫기
 */
function closeModal(overlay, reject) {
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
function applyModalStyles(overlay, dialog) {
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
        minWidth: '500px',
        maxWidth: '600px',
        maxHeight: '90vh',
        boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)',
        display: 'flex',
        flexDirection: 'column',
        overflow: 'hidden'
    });

    // 반응형 디자인
    if (window.innerWidth < 640) {
        dialog.style.minWidth = '90%';
        dialog.style.maxWidth = '90%';
    }
}
