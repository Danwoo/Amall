package project.amall.config.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 로그인 실패 시 처리 핸들러
 *
 * - 로그인 실패 메시지를 세션에 저장하여 화면에 표시
 */
@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        // 세션에 에러 메시지 저장
        HttpSession session = request.getSession();
        session.setAttribute("loginError", "아이디 또는 비밀번호가 올바르지 않습니다.");

        // 로그인 페이지로 리다이렉트
        response.sendRedirect("/member/login?error=true");
    }
}
