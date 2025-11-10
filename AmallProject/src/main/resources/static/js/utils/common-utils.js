/**
 * 공통 유틸리티 함수 모음
 *
 * 여러 핸들러 파일에서 중복 사용되는 함수들을 통합
 */

/**
 * 페이지에서 회원 ID 추출
 *
 * Thymeleaf에서 주입한 meta 태그 또는 세션 스토리지에서 회원 ID를 가져옵니다.
 *
 * @returns {string|null} 회원 ID
 */
function getMemberIdFromPage() {
	// Thymeleaf에서 주입한 memberId를 찾음
	const metaTag = document.querySelector('meta[name="member-id"]');
	if (metaTag) {
		return metaTag.getAttribute('content');
	}

	// 세션 스토리지에서 조회 (로그인 시 저장된 경우)
	return sessionStorage.getItem('memberId');
}

/**
 * 날짜 포맷팅
 *
 * ISO 8601 날짜 문자열을 'YYYY-MM-DD HH:mm' 형식으로 변환
 *
 * @param {string} dateString - ISO 8601 날짜 문자열
 * @returns {string} 포맷된 날짜 문자열
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
		console.error('날짜 포맷팅 오류:', error);
		return dateString;
	}
}

/**
 * 날짜만 포맷팅 (시간 제외)
 *
 * ISO 8601 날짜 문자열을 'YYYY-MM-DD' 형식으로 변환
 *
 * @param {string} dateString - ISO 8601 날짜 문자열
 * @returns {string} 포맷된 날짜 문자열
 */
function formatDateOnly(dateString) {
	if (!dateString) return '';

	try {
		const date = new Date(dateString);
		const year = date.getFullYear();
		const month = String(date.getMonth() + 1).padStart(2, '0');
		const day = String(date.getDate()).padStart(2, '0');

		return `${year}-${month}-${day}`;
	} catch (error) {
		console.error('날짜 포맷팅 오류:', error);
		return dateString;
	}
}

/**
 * 가격 포맷팅
 *
 * 숫자를 한국 로케일 형식의 가격 문자열로 변환
 * 예: 10000 -> "10,000"
 *
 * @param {number} price - 가격 (숫자)
 * @returns {string} 포맷된 가격 문자열 (콤마 포함)
 */
function formatPrice(price) {
	if (!price && price !== 0) return '0';
	return Number(price).toLocaleString('ko-KR');
}

/**
 * 주문 상태 한글 변환
 *
 * 영문 주문 상태 코드를 한글 텍스트로 변환
 *
 * @param {string} status - 주문 상태 코드 (PENDING, PAID, SHIPPED, DELIVERED, CANCELLED)
 * @returns {string} 한글 상태 텍스트
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
 * 주문 상태 아이콘 가져오기
 *
 * 주문 상태에 맞는 이모지 아이콘 반환
 *
 * @param {string} status - 주문 상태 코드
 * @returns {string} 이모지 아이콘
 */
function getOrderStatusIcon(status) {
	const iconMap = {
		'PENDING': '⏳',
		'PAID': '✅',
		'SHIPPED': '🚚',
		'DELIVERED': '📦',
		'CANCELLED': '❌'
	};
	return iconMap[status] || '📋';
}

/**
 * 전화번호 포맷팅
 *
 * 숫자만 있는 전화번호를 하이픈이 포함된 형식으로 변환
 * 예: "01012345678" -> "010-1234-5678"
 *
 * @param {string} phoneNumber - 전화번호 (숫자만)
 * @returns {string} 포맷된 전화번호
 */
function formatPhoneNumber(phoneNumber) {
	if (!phoneNumber) return '';

	// 숫자만 추출
	const numbers = phoneNumber.replace(/[^0-9]/g, '');

	// 길이에 따라 포맷팅
	if (numbers.length === 11) {
		return numbers.replace(/(\d{3})(\d{4})(\d{4})/, '$1-$2-$3');
	} else if (numbers.length === 10) {
		return numbers.replace(/(\d{3})(\d{3})(\d{4})/, '$1-$2-$3');
	} else {
		return phoneNumber;
	}
}

/**
 * 우편번호 포맷팅
 *
 * 5자리 우편번호를 하이픈이 포함된 형식으로 변환
 * 예: "12345" -> "12-345"
 *
 * @param {string} postCode - 우편번호
 * @returns {string} 포맷된 우편번호
 */
function formatPostCode(postCode) {
	if (!postCode) return '';

	const numbers = postCode.replace(/[^0-9]/g, '');

	if (numbers.length === 5) {
		return numbers.replace(/(\d{2})(\d{3})/, '$1-$3');
	}

	return postCode;
}

/**
 * 쿼리 파라미터 추출
 *
 * URL에서 특정 쿼리 파라미터 값을 추출
 *
 * @param {string} paramName - 파라미터 이름
 * @returns {string|null} 파라미터 값
 */
function getQueryParam(paramName) {
	const urlParams = new URLSearchParams(window.location.search);
	return urlParams.get(paramName);
}

/**
 * 배열을 청크로 나누기
 *
 * 큰 배열을 지정된 크기의 작은 배열들로 분할
 *
 * @param {Array} array - 원본 배열
 * @param {number} size - 청크 크기
 * @returns {Array} 청크로 나뉜 배열들의 배열
 */
function chunkArray(array, size) {
	const chunks = [];
	for (let i = 0; i < array.length; i += size) {
		chunks.push(array.slice(i, i + size));
	}
	return chunks;
}

/**
 * 디바운스 함수
 *
 * 연속적인 이벤트 호출을 지연시켜 마지막 호출만 실행
 *
 * @param {Function} func - 실행할 함수
 * @param {number} wait - 대기 시간 (ms)
 * @returns {Function} 디바운스된 함수
 */
function debounce(func, wait) {
	let timeout;
	return function executedFunction(...args) {
		const later = () => {
			clearTimeout(timeout);
			func(...args);
		};
		clearTimeout(timeout);
		timeout = setTimeout(later, wait);
	};
}

/**
 * 쓰로틀 함수
 *
 * 연속적인 이벤트 호출을 일정 간격으로 제한
 *
 * @param {Function} func - 실행할 함수
 * @param {number} limit - 제한 시간 (ms)
 * @returns {Function} 쓰로틀된 함수
 */
function throttle(func, limit) {
	let inThrottle;
	return function(...args) {
		if (!inThrottle) {
			func.apply(this, args);
			inThrottle = true;
			setTimeout(() => inThrottle = false, limit);
		}
	};
}
