package project.amall.script.utils;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.http.HttpServletResponse;

/**
 * JavaScript Alert 스크립트 유틸리티
 *
 * @deprecated 이 방식은 보안 문제와 UX 문제가 있습니다.
 * FlashAttribute와 Thymeleaf를 사용하는 것을 권장합니다.
 *
 * 현재는 XSS 방지를 위해 이스케이프 처리를 적용했습니다.
 */
@Deprecated
public class ScriptUtils {

	/**
	 * 응답 초기화 (UTF-8 인코딩)
	 */
	public static void init(HttpServletResponse response) {
		response.setContentType("text/html; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
	}

	/**
	 * JavaScript 문자열 이스케이프 처리 (XSS 방지)
	 *
	 * @param input 이스케이프할 문자열
	 * @return 이스케이프된 문자열
	 */
	private static String escapeJavaScript(String input) {
		if (input == null) {
			return "";
		}

		return input
				.replace("\\", "\\\\")  // 백슬래시
				.replace("'", "\\'")    // 작은따옴표
				.replace("\"", "\\\"")  // 큰따옴표
				.replace("\n", "\\n")   // 개행
				.replace("\r", "\\r")   // 캐리지 리턴
				.replace("\t", "\\t")   // 탭
				.replace("<", "\\x3C")  // < (스크립트 태그 방지)
				.replace(">", "\\x3E")  // > (스크립트 태그 방지)
				.replace("/", "\\/");   // 슬래시 (</script> 방지)
	}

	/**
	 * Alert 창 표시 (XSS 방지 적용)
	 *
	 * @param response   HttpServletResponse
	 * @param alertText  표시할 메시지
	 * @throws IOException IO 예외
	 */
	public static void alert(HttpServletResponse response, String alertText) throws IOException {
		init(response);
		PrintWriter out = response.getWriter();
		String escapedText = escapeJavaScript(alertText);
		out.println("<script>alert('" + escapedText + "');</script>");
		out.flush();
	}

	/**
	 * Alert 창 표시 후 페이지 이동 (XSS 방지 적용)
	 *
	 * @param response   HttpServletResponse
	 * @param alertText  표시할 메시지
	 * @param nextPage   이동할 페이지 URL
	 * @throws IOException IO 예외
	 */
	public static void alertAndMovePage(HttpServletResponse response, String alertText, String nextPage)
			throws IOException {
		init(response);
		PrintWriter out = response.getWriter();
		String escapedText = escapeJavaScript(alertText);
		String escapedUrl = escapeJavaScript(nextPage);

		// URL 검증: javascript: 프로토콜 차단
		if (nextPage != null && nextPage.toLowerCase().startsWith("javascript:")) {
			throw new IllegalArgumentException("Invalid URL: javascript: protocol is not allowed");
		}

		out.println("<script>alert('" + escapedText + "'); location.href='" + escapedUrl + "';</script>");
		out.flush();
	}

	/**
	 * Alert 창 표시 후 이전 페이지로 이동 (XSS 방지 적용)
	 *
	 * @param response   HttpServletResponse
	 * @param alertText  표시할 메시지
	 * @throws IOException IO 예외
	 */
	public static void alertAndBackPage(HttpServletResponse response, String alertText) throws IOException {
		init(response);
		PrintWriter out = response.getWriter();
		String escapedText = escapeJavaScript(alertText);
		out.println("<script>alert('" + escapedText + "'); history.go(-1);</script>");
		out.flush();
	}

}
