package project.amall.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import project.amall.common.exception.BusinessException;
import project.amall.common.exception.code.ErrorCode;

/**
 * Spring Security 관련 유틸리티 클래스
 *
 * Phase 2: 보안 강화
 */
@Slf4j
public class SecurityUtils {

	/**
	 * 현재 인증된 사용자의 ID를 가져옵니다.
	 *
	 * @return 현재 로그인한 사용자 ID
	 * @throws BusinessException 인증되지 않은 경우
	 */
	public static String getCurrentMemberId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()) {
			throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
		}

		Object principal = authentication.getPrincipal();

		// UserDetails를 구현한 경우
		if (principal instanceof UserDetails) {
			return ((UserDetails) principal).getUsername();
		}

		// String인 경우 (익명 사용자 등)
		if (principal instanceof String) {
			String username = (String) principal;
			if ("anonymousUser".equals(username)) {
				throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
			}
			return username;
		}

		throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증 정보를 가져올 수 없습니다.");
	}

	/**
	 * 현재 로그인한 사용자와 요청한 memberId가 일치하는지 검증합니다.
	 *
	 * @param requestedMemberId 요청에 포함된 memberId
	 * @throws BusinessException 권한이 없는 경우
	 */
	public static void validateMemberAccess(String requestedMemberId) {
		String currentMemberId = getCurrentMemberId();

		if (!currentMemberId.equals(requestedMemberId)) {
			log.warn("권한 없는 접근 시도: currentUser={}, requestedUser={}", currentMemberId, requestedMemberId);
			throw new BusinessException(ErrorCode.FORBIDDEN, "본인의 정보만 접근할 수 있습니다.");
		}
	}

	/**
	 * 현재 로그인한 사용자가 관리자인지 확인합니다.
	 *
	 * @return 관리자 여부
	 */
	public static boolean isAdmin() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null) {
			return false;
		}

		return authentication.getAuthorities().stream()
				.anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));
	}
}
