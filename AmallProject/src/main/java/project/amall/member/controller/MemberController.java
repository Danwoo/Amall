package project.amall.member.controller;

import java.io.IOException;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import project.amall.alarm.service.AlarmService;
import project.amall.common.constant.ProductCategory;
import project.amall.common.util.MemberHashGenerator;
import project.amall.member.dto.MemberDto;
import project.amall.member.service.MatchingService;
import project.amall.member.service.MemberService;
import project.amall.product.dto.ProductDto;
import project.amall.product.service.ProductService;
import project.amall.script.utils.ScriptUtils;

/**
 * 회원 뷰 컨트롤러 (레거시)
 *
 * Thymeleaf 뷰 렌더링을 담당하는 컨트롤러
 * RESTful API는 MemberApiController 사용 권장
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class MemberController {

	private final MemberService memberService;
	private final MatchingService matchingService;
	private final AlarmService alarmService;
	private final ProductService productService;
	private final PasswordEncoder passwordEncoder;

	// ========== Helper Methods ==========

	/**
	 * 홈 페이지 모델 설정
	 *
	 * 중복 제거: 5곳에서 반복되던 홈 페이지 상품 설정 로직 통합
	 *
	 * @param model 모델
	 */
	private void prepareHomePageModel(Model model) {
		List<ProductDto> mainGift = productService.showMainByCategory(ProductCategory.GIFT);
		List<ProductDto> mainCouple = productService.showMainByCategory(ProductCategory.COUPLE);
		model.addAttribute("mainGift", mainGift);
		model.addAttribute("mainCouple", mainCouple);
	}

	// ========== View Handlers ==========

	/**
	 * 회원가입
	 *
	 * POST /signup/amall.com
	 */
	@RequestMapping(value="/signup/amall.com", produces="application/text;charset=utf-8")
	public String signUpMember(@ModelAttribute MemberDto member, Model model) throws Exception {
		log.info("회원가입 요청: memberId={}", member.getMemberId());

		// 회원 해시 생성
		member.setMemberHash(MemberHashGenerator.generate(member.getMemberId()));

		// 회원가입 처리
		memberService.signUpMember(member);

		// 홈 페이지로 이동
		prepareHomePageModel(model);
		return "content/home";
	}

	/**
	 * 로그아웃
	 *
	 * POST /logout/amall.com
	 */
	@RequestMapping(value="/logout/amall.com", produces="application/text;charset=utf-8")
	public String logoutMember(HttpSession session, Model model) {
		log.info("로그아웃 요청");

		session.invalidate();

		prepareHomePageModel(model);
		return "content/home";
	}

	/**
	 * 회원정보 수정
	 *
	 * POST /updateprofile/amall.com
	 */
	@RequestMapping(value="/updateprofile/amall.com", produces="application/text;charset=utf-8")
	public String updateMember(HttpSession session, Model model, HttpServletResponse response, MemberDto member) throws IOException {
		model.addAttribute("checkLogin", session.getAttribute("checkLoginInSession"));

		MemberDto dto = (MemberDto) session.getAttribute("memberDto");
		member.setMemberHash(dto.getMemberHash());

		// 회원정보 업데이트
		memberService.updateMember(member);

		// 세션 갱신
		dto = memberService.getDtoUseHash(dto.getMemberHash());
		session.setAttribute("memberDto", dto);

		log.info("회원정보 수정 완료: memberId={}", dto.getMemberId());

		ScriptUtils.alert(response, "회원정보가 수정되었습니다.");
		return "content/myPage";
	}

	/**
	 * 커플 매칭 요청 전송
	 *
	 * POST /sendMatching/amall.com
	 *
	 * MatchingService 사용으로 57줄 → 15줄 감소
	 */
	@RequestMapping(value="/sendMatching/amall.com", produces="application/text;charset=utf-8")
	public String sendMatching(HttpSession session, Model model, HttpServletResponse response, String memberMatchHash) throws IOException {
		MemberDto myDto = (MemberDto) session.getAttribute("memberDto");
		String myHash = myDto.getMemberHash();

		log.info("매칭 요청: myId={}, targetHash={}", myDto.getMemberId(), memberMatchHash);

		try {
			// MatchingService에서 모든 검증 처리
			matchingService.sendMatchingRequest(myHash, memberMatchHash);

			ScriptUtils.alert(response, "매칭 알람을 전송하였습니다.");
			model.addAttribute("checkLogin", session.getAttribute("checkLoginInSession"));

			// 세션 갱신
			MemberDto refreshed = memberService.getDtoUseHash(myHash);
			session.setAttribute("memberDto", refreshed);

			return "content/myPage";
		} catch (Exception e) {
			log.error("매칭 요청 실패: {}", e.getMessage());
			ScriptUtils.alert(response, e.getMessage());

			model.addAttribute("checkLogin", session.getAttribute("checkLoginInSession"));
			model.addAttribute("memberDto", myDto);
			return "content/updateProfile";
		}
	}

	/**
	 * 회원 탈퇴
	 *
	 * POST /delete/amall.com
	 */
	@RequestMapping(value="/delete/amall.com", produces="application/text;charset=utf-8")
	public String deleteMember(HttpSession session, Model model, HttpServletResponse response) throws IOException {
		MemberDto dto = (MemberDto) session.getAttribute("memberDto");

		log.info("회원 탈퇴: memberId={}", dto.getMemberId());

		memberService.deleteMember(dto);
		session.invalidate();

		ScriptUtils.alert(response, "계정이 삭제되었습니다.");

		prepareHomePageModel(model);
		return "content/home";
	}

	/**
	 * 커플 매칭 수락
	 *
	 * POST /acceptmatching/{alarmId}/amall.com
	 *
	 * MatchingService 사용으로 로직 간소화
	 */
	@RequestMapping(value="/acceptmatching/{alarmId}/amall.com", produces="application/text;charset=utf-8")
	public String acceptMatching(HttpSession session, Model model, HttpServletResponse response, @PathVariable String alarmId) throws IOException {
		MemberDto myDto = (MemberDto) session.getAttribute("memberDto");
		String myHash = myDto.getMemberHash();

		log.info("매칭 수락: myId={}, alarmId={}", myDto.getMemberId(), alarmId);

		try {
			// MatchingService에서 매칭 처리
			matchingService.acceptMatching(myHash, alarmId);

			// 세션 갱신
			MemberDto refreshed = memberService.getDtoUseHash(myHash);
			session.setAttribute("memberDto", refreshed);

			// 알림 페이지로 이동
			model.addAttribute("checkLogin", session.getAttribute("checkLoginInSession"));

			// 알림 목록 조회
			List<java.util.Map<String, Object>> alarmList = alarmService.getMyAlarm(refreshed.getMemberId());
			model.addAttribute("alarmList", alarmList);
			model.addAttribute("alarmSize", alarmList.size());

			// 매칭된 상대 정보
			String matchingId = refreshed.getMemberMatchId();
			if (matchingId != null) {
				model.addAttribute("checkMatching", true);
				model.addAttribute("matchDto", memberService.reloadMemberData(matchingId));
			} else {
				model.addAttribute("checkMatching", false);
			}

			return "content/alarm";
		} catch (Exception e) {
			log.error("매칭 수락 실패: {}", e.getMessage());
			ScriptUtils.alert(response, e.getMessage());
			model.addAttribute("checkLogin", session.getAttribute("checkLoginInSession"));
			return "content/alarm";
		}
	}

	/**
	 * @deprecated Spring Security가 로그인을 처리합니다.
	 * SecurityConfig 및 CustomAuthenticationSuccessHandler 참조
	 *
	 * 이 메소드는 더 이상 사용되지 않습니다.
	 * 로그인은 POST /member/login 으로 Spring Security가 자동 처리합니다.
	 */
	@Deprecated
	@RequestMapping(value="/login/amall.com", produces="application/text;charset=utf-8")
	public String loginMember(HttpSession session, Model model, HttpServletResponse response, MemberDto member) throws IOException {
		MemberDto dto = memberService.loginMember(member);
		if (dto != null) {
			memberService.updateConnectDate(dto);
			session.setAttribute("memberDto", dto);
			session.setAttribute("checkLoginInSession", true);
			model.addAttribute("checkLogin", session.getAttribute("checkLoginInSession"));
			prepareHomePageModel(model);
			return "content/home";
		}
		ScriptUtils.alert(response, "로그인에 실패하였습니다.");
		prepareHomePageModel(model);
		return "content/home";
	}
}
