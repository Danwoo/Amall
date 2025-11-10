package project.amall.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 날짜/시간 유틸리티 클래스
 *
 * 날짜와 시간 관련 공통 작업을 처리하는 유틸리티
 */
public class DateTimeUtils {

	// 기본 포맷터들
	private static final DateTimeFormatter DATETIME_FORMATTER =
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private static final DateTimeFormatter DATE_FORMATTER =
			DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private static final DateTimeFormatter TIME_FORMATTER =
			DateTimeFormatter.ofPattern("HH:mm:ss");

	private static final DateTimeFormatter COMPACT_DATETIME_FORMATTER =
			DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

	private static final DateTimeFormatter DISPLAY_FORMATTER =
			DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm");

	/**
	 * 현재 날짜시간 문자열 반환 (기본 형식)
	 *
	 * 형식: yyyy-MM-dd HH:mm:ss
	 * 예: 2024-11-10 14:30:45
	 *
	 * @return 현재 날짜시간 문자열
	 */
	public static String getCurrentDateTime() {
		return LocalDateTime.now().format(DATETIME_FORMATTER);
	}

	/**
	 * 현재 날짜 문자열 반환
	 *
	 * 형식: yyyy-MM-dd
	 * 예: 2024-11-10
	 *
	 * @return 현재 날짜 문자열
	 */
	public static String getCurrentDate() {
		return LocalDate.now().format(DATE_FORMATTER);
	}

	/**
	 * 현재 시간 문자열 반환
	 *
	 * 형식: HH:mm:ss
	 * 예: 14:30:45
	 *
	 * @return 현재 시간 문자열
	 */
	public static String getCurrentTime() {
		return LocalDateTime.now().format(TIME_FORMATTER);
	}

	/**
	 * 압축 형식의 현재 날짜시간 문자열 반환 (ID 생성용)
	 *
	 * 형식: yyyyMMddHHmmss
	 * 예: 20241110143045
	 *
	 * @return 압축 형식 날짜시간 문자열
	 */
	public static String getCompactDateTime() {
		return LocalDateTime.now().format(COMPACT_DATETIME_FORMATTER);
	}

	/**
	 * 사용자 표시용 날짜시간 문자열 반환
	 *
	 * 형식: yyyy년 MM월 dd일 HH:mm
	 * 예: 2024년 11월 10일 14:30
	 *
	 * @return 표시용 날짜시간 문자열
	 */
	public static String getDisplayDateTime() {
		return LocalDateTime.now().format(DISPLAY_FORMATTER);
	}

	/**
	 * LocalDateTime을 문자열로 변환 (기본 형식)
	 *
	 * @param dateTime LocalDateTime 객체
	 * @return 변환된 문자열 (yyyy-MM-dd HH:mm:ss)
	 */
	public static String format(LocalDateTime dateTime) {
		if (dateTime == null) {
			return null;
		}
		return dateTime.format(DATETIME_FORMATTER);
	}

	/**
	 * LocalDateTime을 커스텀 형식으로 변환
	 *
	 * @param dateTime LocalDateTime 객체
	 * @param pattern 날짜 패턴 (예: "yyyy/MM/dd")
	 * @return 변환된 문자열
	 */
	public static String format(LocalDateTime dateTime, String pattern) {
		if (dateTime == null) {
			return null;
		}
		return dateTime.format(DateTimeFormatter.ofPattern(pattern));
	}

	/**
	 * 문자열을 LocalDateTime으로 파싱 (기본 형식)
	 *
	 * @param dateTimeStr 날짜시간 문자열 (yyyy-MM-dd HH:mm:ss)
	 * @return LocalDateTime 객체
	 */
	public static LocalDateTime parse(String dateTimeStr) {
		if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
			return null;
		}
		return LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
	}

	/**
	 * 문자열을 LocalDateTime으로 파싱 (커스텀 형식)
	 *
	 * @param dateTimeStr 날짜시간 문자열
	 * @param pattern 날짜 패턴
	 * @return LocalDateTime 객체
	 */
	public static LocalDateTime parse(String dateTimeStr, String pattern) {
		if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
			return null;
		}
		return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(pattern));
	}

	/**
	 * 두 날짜 사이의 일수 계산
	 *
	 * @param start 시작 날짜
	 * @param end 종료 날짜
	 * @return 일수 차이
	 */
	public static long daysBetween(LocalDateTime start, LocalDateTime end) {
		if (start == null || end == null) {
			return 0;
		}
		return ChronoUnit.DAYS.between(start, end);
	}

	/**
	 * 두 날짜 사이의 시간 수 계산
	 *
	 * @param start 시작 날짜시간
	 * @param end 종료 날짜시간
	 * @return 시간 차이
	 */
	public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
		if (start == null || end == null) {
			return 0;
		}
		return ChronoUnit.HOURS.between(start, end);
	}

	/**
	 * 두 날짜 사이의 분 수 계산
	 *
	 * @param start 시작 날짜시간
	 * @param end 종료 날짜시간
	 * @return 분 차이
	 */
	public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
		if (start == null || end == null) {
			return 0;
		}
		return ChronoUnit.MINUTES.between(start, end);
	}

	/**
	 * 날짜가 오늘인지 확인
	 *
	 * @param dateTime 확인할 날짜시간
	 * @return 오늘이면 true, 아니면 false
	 */
	public static boolean isToday(LocalDateTime dateTime) {
		if (dateTime == null) {
			return false;
		}
		LocalDate today = LocalDate.now();
		return dateTime.toLocalDate().equals(today);
	}

	/**
	 * 날짜가 과거인지 확인
	 *
	 * @param dateTime 확인할 날짜시간
	 * @return 과거면 true, 아니면 false
	 */
	public static boolean isPast(LocalDateTime dateTime) {
		if (dateTime == null) {
			return false;
		}
		return dateTime.isBefore(LocalDateTime.now());
	}

	/**
	 * 날짜가 미래인지 확인
	 *
	 * @param dateTime 확인할 날짜시간
	 * @return 미래면 true, 아니면 false
	 */
	public static boolean isFuture(LocalDateTime dateTime) {
		if (dateTime == null) {
			return false;
		}
		return dateTime.isAfter(LocalDateTime.now());
	}

	/**
	 * N일 후의 날짜시간 계산
	 *
	 * @param days 일수
	 * @return N일 후의 LocalDateTime
	 */
	public static LocalDateTime addDays(int days) {
		return LocalDateTime.now().plusDays(days);
	}

	/**
	 * N시간 후의 날짜시간 계산
	 *
	 * @param hours 시간수
	 * @return N시간 후의 LocalDateTime
	 */
	public static LocalDateTime addHours(int hours) {
		return LocalDateTime.now().plusHours(hours);
	}

	/**
	 * N분 후의 날짜시간 계산
	 *
	 * @param minutes 분수
	 * @return N분 후의 LocalDateTime
	 */
	public static LocalDateTime addMinutes(int minutes) {
		return LocalDateTime.now().plusMinutes(minutes);
	}
}
