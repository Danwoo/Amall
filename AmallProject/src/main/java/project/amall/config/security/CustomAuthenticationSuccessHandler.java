package project.amall.config.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import project.amall.member.dto.MemberDto;
import project.amall.member.service.MemberService;

import java.io.IOException;

/**
 * 로그인 성공 시 처리 핸들러
 *
 * - 기존 세션 방식과의 호환성 유지
 * - 로그인 성공 시 회원 정보를 세션에 저장
 */
@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final MemberService memberService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // 인증된 사용자 정보 가져오기
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String memberId = userDetails.getUsername();

        // DB에서 전체 회원 정보 조회
        MemberDto memberDto = memberService.reloadMemberData(memberId);

        if (memberDto != null) {
            // 마지막 접속 시간 업데이트
            memberService.updateConnectDate(memberDto);

            // 최신 정보 다시 조회
            memberDto = memberService.reloadMemberData(memberId);

            // 세션에 회원 정보 저장 (기존 코드와의 호환성)
            HttpSession session = request.getSession();
            session.setAttribute("memberDto", memberDto);
            session.setAttribute("checkLoginInSession", true);
        }

        // 홈 페이지로 리다이렉트
        response.sendRedirect("/");
    }
}
