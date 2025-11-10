package project.amall.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import project.amall.alarm.service.AlarmService;
import project.amall.cart.dto.CartDto;
import project.amall.cart.service.CartService;
import project.amall.common.constant.ProductCategory;
import project.amall.member.dto.MemberDto;
import project.amall.member.service.MemberService;
import project.amall.product.dto.ProductDto;
import project.amall.product.service.ProductService;
import project.amall.script.utils.ScriptUtils;
import project.amall.wishlist.service.WishListService;

/**
 * Amall 뷰 컨트롤러 (레거시)
 *
 * Thymeleaf 뷰 렌더링을 담당하는 컨트롤러
 * RESTful API는 별도의 ApiController 사용 권장
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class AmallController {

	private final MemberService memberService;
	private final ProductService productService;
	private final CartService cartService;
	private final WishListService wishListService;
	private final AlarmService alarmService;
	private final PasswordEncoder passwordEncoder;

	// ========== Helper Methods ==========

	/**
	 * 로그인 여부 확인
	 *
	 * @param session HTTP 세션
	 * @return 로그인 상태 (true: 로그인됨, false: 로그인 안됨)
	 */
	private boolean isLoggedIn(HttpSession session) {
		return session.getAttribute("checkLoginInSession") != null;
	}

	/**
	 * 세션의 회원 정보를 DB에서 최신 정보로 갱신
	 *
	 * 중복 제거: 5곳에서 반복되던 세션 갱신 로직 통합
	 * - goMyPage, goUpdateProfile, goWishList, goBag, goAlarm
	 *
	 * @param session HTTP 세션
	 * @return 갱신된 회원 정보
	 */
	private MemberDto refreshMemberSession(HttpSession session) {
		MemberDto currentDto = (MemberDto) session.getAttribute("memberDto");
		if (currentDto == null) {
			return null;
		}

		MemberDto refreshedDto = memberService.getDtoUseHash(currentDto.getMemberHash());
		session.setAttribute("memberDto", refreshedDto);

		log.debug("회원 세션 갱신: memberId={}", refreshedDto.getMemberId());
		return refreshedDto;
	}

	/**
	 * 로그인 체크 및 알림 후 홈으로 리다이렉트
	 *
	 * @param session HTTP 세션
	 * @param model 모델
	 * @param response HTTP 응답
	 * @return 로그인되어 있으면 true, 아니면 false
	 * @throws IOException IO 예외
	 */
	private boolean checkLoginAndRedirect(HttpSession session, Model model, HttpServletResponse response) throws IOException {
		model.addAttribute("checkLogin", session.getAttribute("checkLoginInSession"));
		if (!isLoggedIn(session)) {
			ScriptUtils.alert(response, "로그인이 필요한 서비스입니다.");
			return false;
		}
		return true;
	}

	// ========== View Handlers ==========

	/**
	 * 홈 페이지
	 *
	 * GET /amall.com
	 */
	@RequestMapping(value="/amall.com", produces="application/text;charset=utf-8")
	public String goHome(HttpSession session, Model model) {
		model.addAttribute("checkLogin", session.getAttribute("checkLoginInSession"));

		// 홈 페이지 상품 출력 (GIFT, COUPLE)
		List<ProductDto> mainGift = productService.showMainByCategory(ProductCategory.GIFT);
		List<ProductDto> mainCouple = productService.showMainByCategory(ProductCategory.COUPLE);

		model.addAttribute("mainGift", mainGift);
		model.addAttribute("mainCouple", mainCouple);

		return "content/home";
	}
	
	/**
	 * 이벤트 페이지
	 *
	 * GET /amall.eventpage.com
	 */
	@RequestMapping(value="/amall.eventpage.com", produces="application/text;charset=utf-8")
	public String goEventPage(HttpSession session, Model model) {
		model.addAttribute("checkLogin", session.getAttribute("checkLoginInSession"));
		return "content/eventPage";
	}

	/**
	 * 스토어 (전체 상품 목록)
	 *
	 * GET /amall.store.com
	 */
	@RequestMapping(value="/amall.store.com", produces="application/text;charset=utf-8")
	public String goStore(HttpSession session, Model model) {
		model.addAttribute("checkLogin", session.getAttribute("checkLoginInSession"));

		List<ProductDto> productList = productService.showAllProductStore();
		model.addAttribute("productList", productList);

		return "content/store";
	}

	/**
	 * 상품 상세 페이지
	 *
	 * GET /amall.storedetail.com?prodCode={prodCode}
	 *
	 * @param prodCode 상품 코드
	 */
	@RequestMapping(value="/amall.storedetail.com", produces="application/text;charset=utf-8")
	public String goStoreDetail(HttpSession session, Model model, String prodCode) {
		model.addAttribute("checkLogin", session.getAttribute("checkLoginInSession"));

		List<ProductDto> productDetail = productService.showProductDetail(prodCode);
		model.addAttribute("productDetail", productDetail);

		log.debug("상품 상세 조회: prodCode={}, count={}", prodCode, productDetail.size());

		return "content/storeDetail";
	}
	
	/**
	 * 마이페이지
	 *
	 * GET /amall.mypage.com
	 *
	 * 로그인 필수
	 */
	@RequestMapping(value="/amall.mypage.com", produces="application/text;charset=utf-8")
	public String goMyPage(HttpSession session, Model model, HttpServletResponse response) throws IOException {
		if (!checkLoginAndRedirect(session, model, response)) {
			return "content/home";
		}

		// 회원 정보 갱신 (Helper 메서드 사용 - 중복 제거)
		refreshMemberSession(session);

		return "content/myPage";
	}
	
	/**
	 * 회원정보 수정 페이지
	 *
	 * GET /amall.updateprofile.com?memberPwd={password}
	 *
	 * 로그인 필수, 비밀번호 확인 필수
	 *
	 * @param memberPwd 현재 비밀번호 (확인용)
	 */
	@RequestMapping(value="/amall.updateprofile.com", produces="application/text;charset=utf-8")
	public String goUpdateProfile(HttpSession session, Model model, HttpServletResponse response, String memberPwd) throws IOException {
		// (1) 로그인 체크
		if (!checkLoginAndRedirect(session, model, response)) {
			return "content/home";
		}

		// (2) 회원 정보 갱신 (Helper 메서드 사용 - 중복 제거)
		MemberDto dto = refreshMemberSession(session);

		// (3) 비밀번호 확인 (BCrypt 사용 - 보안 개선)
		if (memberPwd != null && passwordEncoder.matches(memberPwd, dto.getMemberPwd())) {
			model.addAttribute("memberDto", dto);
			return "content/updateProfile";
		}

		ScriptUtils.alert(response, "비밀번호가 다릅니다.");
		return "content/myPage";
	}
	
	/**
	 * 위시리스트 페이지
	 *
	 * GET /amall.wishlist.com
	 *
	 * 로그인 필수
	 * 커플 매칭된 경우 상대방 위시리스트도 함께 표시
	 */
	@RequestMapping(value="/amall.wishlist.com", produces="application/text;charset=utf-8")
	public String goWishList(HttpSession session, Model model, HttpServletResponse response) throws IOException {
		if (!checkLoginAndRedirect(session, model, response)) {
			return "content/home";
		}

		// 회원 정보 갱신 (Helper 메서드 사용 - 중복 제거)
		MemberDto dto = refreshMemberSession(session);

		// 내 위시리스트 조회
		List<ProductDto> myWishList = wishListService.showThisIdWishList(dto.getMemberId());
		model.addAttribute("myWishList", myWishList);

		// 커플 매칭 여부 확인
		if (dto.getMemberMatchId() != null) {
			// 커플인 경우: 상대방 위시리스트도 조회
			List<ProductDto> yourWishList = wishListService.showThisIdWishList(dto.getMemberMatchId());
			model.addAttribute("yourWishList", yourWishList);
			model.addAttribute("checkCouple", true);
		} else {
			// 솔로인 경우: 랜덤 값 추가
			model.addAttribute("checkSolo", (int)(Math.random()*10));
		}

		return "content/wishList";
	}
	
	/**
	 * 장바구니 페이지
	 *
	 * GET /amall.bag.com
	 *
	 * 로그인 필수
	 */
	@RequestMapping(value="/amall.bag.com", produces="application/text;charset=utf-8")
	public String goBag(HttpSession session, Model model, HttpServletResponse response) throws IOException {
		if (!checkLoginAndRedirect(session, model, response)) {
			return "content/home";
		}

		// 회원 정보 갱신 (Helper 메서드 사용 - 중복 제거)
		MemberDto dto = refreshMemberSession(session);

		// 장바구니 조회
		List<CartDto> cartList = cartService.showMyCart(dto.getMemberId());
		model.addAttribute("cartList", cartList);

		log.debug("장바구니 조회: memberId={}, count={}", dto.getMemberId(), cartList.size());

		return "content/bag";
	}
	
	/**
	 * 알림 페이지
	 *
	 * GET /amall.alarm.com
	 *
	 * 로그인 필수
	 * 매칭 요청 알림 및 커플 정보 표시
	 */
	@RequestMapping(value="/amall.alarm.com", produces="application/text;charset=utf-8")
	public String goAlarm(Model model, HttpSession session, HttpServletResponse response) throws IOException {
		if (!checkLoginAndRedirect(session, model, response)) {
			return "content/home";
		}

		// 회원 정보 갱신 (Helper 메서드 사용 - 중복 제거)
		MemberDto myDto = refreshMemberSession(session);

		// 알림 목록 조회
		List<Map<String, Object>> alarmList = alarmService.getMyAlarm(myDto.getMemberId());
		model.addAttribute("alarmList", alarmList);
		model.addAttribute("alarmSize", alarmList.size());

		// 커플 매칭 여부 확인
		String matchingId = myDto.getMemberMatchId();
		if (matchingId != null) {
			model.addAttribute("checkMatching", true);
			model.addAttribute("matchDto", memberService.reloadMemberData(matchingId));
			log.debug("매칭된 커플: myId={}, partnerId={}", myDto.getMemberId(), matchingId);
		} else {
			model.addAttribute("checkMatching", false);
		}

		return "content/alarm";
	}
}
