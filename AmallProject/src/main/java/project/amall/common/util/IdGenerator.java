package project.amall.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ID 생성 유틸리티 클래스
 *
 * 장바구니, 주문, 위시리스트 등의 ID 생성을 위한 통합 유틸리티
 *
 * 주의: 현재는 타임스탬프 기반으로 구현되어 있으나,
 * 프로덕션 환경에서는 데이터베이스 시퀀스나 AUTO_INCREMENT 사용을 권장합니다.
 */
public class IdGenerator {

	/**
	 * 타임스탬프 기반 정수 ID 생성
	 *
	 * 현재 시간의 밀리초를 Integer 범위 내로 변환하여 반환
	 * (동시성 처리가 필요한 경우 별도의 동기화 메커니즘 필요)
	 *
	 * @return 생성된 ID (int)
	 */
	public static int generateIntId() {
		return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
	}

	/**
	 * 장바구니 ID 생성
	 *
	 * @return 장바구니 ID
	 */
	public static int generateCartId() {
		return generateIntId();
	}

	/**
	 * 위시리스트 ID 생성
	 *
	 * @return 위시리스트 ID
	 */
	public static int generateWishlistId() {
		return generateIntId();
	}

	/**
	 * 주문 ID 생성
	 *
	 * 형식: ORD-{회원ID}-{타임스탬프}
	 * 예: ORD-user123-20241110123045
	 *
	 * @param memberId 회원 ID
	 * @return 주문 ID (String)
	 */
	public static String generateOrderId(String memberId) {
		String timestamp = LocalDateTime.now()
				.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		return "ORD-" + memberId + "-" + timestamp;
	}

	/**
	 * 선물 배송 요청 ID 생성
	 *
	 * 형식: GIFT-{타임스탬프}
	 * 예: GIFT-20241110123045
	 *
	 * @return 선물 배송 요청 ID (String)
	 */
	public static String generateGiftRequestId() {
		String timestamp = LocalDateTime.now()
				.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		return "GIFT-" + timestamp;
	}

	/**
	 * 알림 ID 생성
	 *
	 * 형식: ALARM-{타임스탬프}-{랜덤값}
	 *
	 * @return 알림 ID (String)
	 */
	public static String generateAlarmId() {
		String timestamp = LocalDateTime.now()
				.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		int random = (int) (Math.random() * 10000);
		return "ALARM-" + timestamp + "-" + random;
	}

	/**
	 * UUID 기반 ID 생성 (확장 용도)
	 *
	 * 더 강력한 유일성이 필요한 경우 사용
	 *
	 * @return UUID 문자열
	 */
	public static String generateUUID() {
		return java.util.UUID.randomUUID().toString();
	}

	/**
	 * 커스텀 접두사를 가진 ID 생성
	 *
	 * @param prefix ID 접두사 (예: "PROD", "USER")
	 * @param useTimestamp 타임스탬프 포함 여부
	 * @return 생성된 ID
	 */
	public static String generateCustomId(String prefix, boolean useTimestamp) {
		if (useTimestamp) {
			String timestamp = LocalDateTime.now()
					.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
			return prefix + "-" + timestamp;
		} else {
			return prefix + "-" + generateIntId();
		}
	}
}
