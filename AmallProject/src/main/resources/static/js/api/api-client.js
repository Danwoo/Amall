/**
 * API 클라이언트 - 모든 API 호출의 공통 래퍼
 *
 * 기능:
 * - 공통 에러 처리
 * - 로딩 상태 관리
 * - CSRF 토큰 자동 추가
 * - 응답 표준화 (ApiResponse 처리)
 */

class ApiClient {
    constructor(baseURL = '') {
        this.baseURL = baseURL;
        this.defaultHeaders = {
            'Content-Type': 'application/json'
        };
    }

    /**
     * CSRF 토큰 가져오기 (Spring Security)
     */
    getCsrfToken() {
        const token = document.querySelector('meta[name="_csrf"]');
        const header = document.querySelector('meta[name="_csrf_header"]');

        if (token && header) {
            return {
                header: header.getAttribute('content'),
                token: token.getAttribute('content')
            };
        }
        return null;
    }

    /**
     * 공통 fetch 래퍼
     */
    async request(url, options = {}) {
        const config = {
            ...options,
            headers: {
                ...this.defaultHeaders,
                ...options.headers
            }
        };

        // CSRF 토큰 추가
        const csrf = this.getCsrfToken();
        if (csrf && ['POST', 'PUT', 'DELETE'].includes(config.method?.toUpperCase())) {
            config.headers[csrf.header] = csrf.token;
        }

        try {
            const response = await fetch(this.baseURL + url, config);

            // HTTP 상태 코드 체크
            if (!response.ok) {
                const error = await this.handleError(response);
                throw error;
            }

            // 204 No Content
            if (response.status === 204) {
                return null;
            }

            // JSON 응답 파싱
            const data = await response.json();

            // ApiResponse<T> 형태인 경우
            if (data.success !== undefined) {
                return data;
            }

            return data;
        } catch (error) {
            console.error('API 요청 실패:', error);
            throw error;
        }
    }

    /**
     * 에러 처리
     */
    async handleError(response) {
        let errorData;

        try {
            errorData = await response.json();
        } catch (e) {
            // JSON 파싱 실패 시 기본 에러
            return {
                status: response.status,
                message: response.statusText || '서버 오류가 발생했습니다'
            };
        }

        // ErrorResponse 형태
        if (errorData.code) {
            return {
                status: response.status,
                code: errorData.code,
                message: errorData.message,
                detail: errorData.detail,
                errors: errorData.errors // 필드 에러
            };
        }

        return {
            status: response.status,
            message: errorData.message || '알 수 없는 오류가 발생했습니다'
        };
    }

    /**
     * GET 요청
     */
    async get(url, params = {}) {
        const queryString = new URLSearchParams(params).toString();
        const fullUrl = queryString ? `${url}?${queryString}` : url;

        return this.request(fullUrl, {
            method: 'GET'
        });
    }

    /**
     * POST 요청
     */
    async post(url, data) {
        return this.request(url, {
            method: 'POST',
            body: JSON.stringify(data)
        });
    }

    /**
     * PUT 요청
     */
    async put(url, data) {
        return this.request(url, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    }

    /**
     * DELETE 요청
     */
    async delete(url) {
        return this.request(url, {
            method: 'DELETE'
        });
    }
}

// 전역 API 클라이언트 인스턴스
const apiClient = new ApiClient();
