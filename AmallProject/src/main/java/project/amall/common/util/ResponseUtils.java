package project.amall.common.util;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * HTTP 응답 유틸리티
 *
 * @deprecated FlashAttribute와 Thymeleaf 사용 권장
 *
 * 기존 ScriptUtils를 더 명확한 이름으로 변경
 */
@Slf4j
@Deprecated
public class ResponseUtils {

    /**
     * JavaScript Alert 표시
     *
     * @param response HttpServletResponse
     * @param message 표시할 메시지
     */
    public static void sendAlert(HttpServletResponse response, String message) {
        try {
            response.setContentType("text/html; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");

            PrintWriter out = response.getWriter();
            String escapedMessage = escapeJavaScript(message);
            out.println("<script>alert('" + escapedMessage + "');</script>");
            out.flush();

        } catch (IOException e) {
            log.error("Alert 전송 실패", e);
        }
    }

    /**
     * Alert 표시 후 페이지 이동
     *
     * @param response HttpServletResponse
     * @param message 표시할 메시지
     * @param redirectUrl 이동할 URL
     */
    public static void sendAlertAndRedirect(HttpServletResponse response, String message, String redirectUrl) {
        try {
            // javascript: 프로토콜 차단
            if (redirectUrl != null && redirectUrl.toLowerCase().startsWith("javascript:")) {
                throw new IllegalArgumentException("Invalid URL: javascript: protocol not allowed");
            }

            response.setContentType("text/html; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");

            PrintWriter out = response.getWriter();
            String escapedMessage = escapeJavaScript(message);
            String escapedUrl = escapeJavaScript(redirectUrl);

            out.println("<script>");
            out.println("alert('" + escapedMessage + "');");
            out.println("location.href='" + escapedUrl + "';");
            out.println("</script>");
            out.flush();

        } catch (IOException e) {
            log.error("Alert 및 리다이렉트 실패", e);
        }
    }

    /**
     * Alert 표시 후 이전 페이지로 이동
     *
     * @param response HttpServletResponse
     * @param message 표시할 메시지
     */
    public static void sendAlertAndGoBack(HttpServletResponse response, String message) {
        try {
            response.setContentType("text/html; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");

            PrintWriter out = response.getWriter();
            String escapedMessage = escapeJavaScript(message);

            out.println("<script>");
            out.println("alert('" + escapedMessage + "');");
            out.println("history.go(-1);");
            out.println("</script>");
            out.flush();

        } catch (IOException e) {
            log.error("Alert 및 뒤로가기 실패", e);
        }
    }

    /**
     * JavaScript 문자열 이스케이프 (XSS 방지)
     */
    private static String escapeJavaScript(String input) {
        if (input == null) {
            return "";
        }

        return input
                .replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("<", "\\x3C")
                .replace(">", "\\x3E")
                .replace("/", "\\/");
    }
}
