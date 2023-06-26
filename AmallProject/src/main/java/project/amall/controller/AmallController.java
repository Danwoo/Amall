package project.amall.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import project.amall.alarm.dto.AlarmDto;
import project.amall.alarm.service.AlarmService;
import project.amall.cart.dto.CartDto;
import project.amall.cart.service.CartService;
import project.amall.member.dto.MemberDto;
import project.amall.member.service.MemberService;
import project.amall.product.dto.ProductDto;
import project.amall.product.service.ProductService;
import project.amall.script.utils.ScriptUtils;
import project.amall.wishlist.service.WishListService;

@Controller
public class AmallController {
	
	@Autowired
	private MemberService memberService;
	@Autowired
	private ProductService productService;
	@Autowired
	private CartService cartService;
	@Autowired
	private WishListService wishListService;
	@Autowired
	private AlarmService alarmService;
	
	@RequestMapping(value="/amall.com", produces="application/text;charset=utf-8")
	public String goHome(HttpSession session, Model model) {
		//MemberDto admin = memberService.getDtoUseHash("e99a18c428cb38d5f260853678922e03");//==================================
		//session.setAttribute("memberDto", admin);
		//session.setAttribute("checkLoginInSession", true);
		//System.out.println("관리자 아이디가 \'goHome\'에 로그인 되어 있음");//==================================================
		model.addAttribute("checkLogin", session.getAttribute("checkLoginInSession"));
		//홈에 있는 이미지 출력용
		List<ProductDto> mainGift = productService.showMain("GIFT");
		List<ProductDto> mainCouple = productService.showMain("COUPLE");
		model.addAttribute("mainGift", mainGift);
		model.addAttribute("mainCouple", mainCouple);
	    return "content/home";
	}
	
	@RequestMapping(value="/amall.eventpage.com", produces="application/text;charset=utf-8")
	public String goEventPage(HttpSession session, Model model) {
		model.addAttribute("checkLogin", session.getAttribute("checkLoginInSession"));
		return "content/eventPage";
	}
	
	@RequestMapping(value="/amall.store.com", produces="application/text;charset=utf-8")
	public String goStore(HttpSession session, Model model) {
		model.addAttribute("checkLogin", session.getAttribute("checkLoginInSession"));
		List<ProductDto> productList = productService.showAllProductStore();
		model.addAttribute("productList", productList);
		return "content/store";
	}
	
	@RequestMapping(value="/amall.storedetail.com", produces="application/text;charset=utf-8")
	public String goStoreDetail(HttpSession session, Model model, String prodCode) {
		model.addAttribute("checkLogin", session.getAttribute("checkLoginInSession"));
		List<ProductDto> productDetail = productService.showProductDetail(prodCode);
		model.addAttribute("productDetail", productDetail);
		System.out.println(productDetail);
		return "content/storeDetail";
	}
	
	@RequestMapping(value="/amall.mypage.com", produces="application/text;charset=utf-8")
	public String goMyPage(HttpSession session, Model model, HttpServletResponse response) throws IOException {
		model.addAttribute("checkLogin", session.getAttribute("checkLoginInSession"));
		Object checkLogin = model.getAttribute("checkLogin");
		if (!(checkLogin == null)) {
			//회원정보 갱신
			//현재 session에 있는 회원의 DTO를 받아 옴
			MemberDto dto = (MemberDto) session.getAttribute("memberDto");
			//그 DTO의 HashCode값을 이용하여 DB에서 업데이트 된 정보를 가져 와 session을 업데이트 시킨다
			session.setAttribute("memberDto", memberService.getDtoUseHash(dto.getMemberHash()));
			return "content/myPage";
		}
		ScriptUtils.alert(response, "로그인이 필요한 서비스입니다.");
		return "content/home";
	}
	
	@RequestMapping(value="/amall.updateprofile.com", produces="application/text;charset=utf-8")
	public String goUpdateProfile(HttpSession session, Model model, HttpServletResponse response, String memberPwd) throws IOException {
		/* (1).로그인 유무를 체크해야 함
		 * (2).나의 정보와 입력한 비밀번호 값이 같은지 확인해야 함
		 * */
		//세션에서 checkLoginInSession의 값을 가져와 로그인 상태를 확인함(로그인X : null, 로그인O : true)
		model.addAttribute("checkLogin", session.getAttribute("checkLoginInSession"));
		
		Object checkLogin = model.getAttribute("checkLogin");
		if (!(checkLogin == null)) {
			//회원정보 갱신
			//현재 session에 있는 회원의 DTO를 받아 옴
			MemberDto dto = (MemberDto) session.getAttribute("memberDto");
			//그 DTO의 HashCode값을 이용하여 DB에서 업데이트 된 정보를 가져 와 session을 업데이트 시킨다
			session.setAttribute("memberDto", memberService.getDtoUseHash(dto.getMemberHash()));
			//DTO를 갱신 시킨다
			dto = (MemberDto) session.getAttribute("memberDto");
			if (!(memberPwd == null) && memberPwd.equals(dto.getMemberPwd())) {
				model.addAttribute("memberDto", session.getAttribute("memberDto"));
				return "content/updateProfile";
			}
			ScriptUtils.alert(response, "비밀번호가 다릅니다.");
			return "content/myPage";
		}
		ScriptUtils.alert(response, "로그인이 필요한 서비스입니다.");
		return "content/home";
	}
	
	@RequestMapping(value="/amall.wishlist.com", produces="application/text;charset=utf-8")
	public String goWishList(HttpSession session, Model model, HttpServletResponse response) throws IOException {
		model.addAttribute("checkLogin", session.getAttribute("checkLoginInSession"));
		Object checkLogin = model.getAttribute("checkLogin");
		if (!(checkLogin == null)) {
			//회원정보 갱신
			//현재 session에 있는 회원의 DTO를 받아 옴
			MemberDto dto = (MemberDto) session.getAttribute("memberDto");
			//그 DTO의 HashCode값을 이용하여 DB에서 업데이트 된 정보를 가져 와 session을 업데이트 시킨다
			session.setAttribute("memberDto", memberService.getDtoUseHash(dto.getMemberHash()));
			//위시리스트를 가져오기 위해 DTO를 갱신 시킨다
			dto = (MemberDto) session.getAttribute("memberDto");
			List<ProductDto> myWishList = wishListService.showThisIdWishList(dto.getMemberId());
			model.addAttribute("myWishList", myWishList);
			if (!(dto.getMemberMatchId()==null)) {
				List<ProductDto> yourWishList = wishListService.showThisIdWishList(dto.getMemberMatchId());
				model.addAttribute("yourWishList", yourWishList);
				model.addAttribute("checkCouple", true);
				return "content/wishList";
			}
			model.addAttribute("checkSolo", (int)(Math.random()*10));
			return "content/wishList";
		}
		ScriptUtils.alert(response, "로그인이 필요한 서비스입니다.");
		return "content/home";
	}
	
	@RequestMapping(value="/amall.bag.com", produces="application/text;charset=utf-8")
	public String goBag(HttpSession session, Model model, HttpServletResponse response) throws IOException {
		model.addAttribute("checkLogin", session.getAttribute("checkLoginInSession"));
		Object checkLogin = model.getAttribute("checkLogin");
		if (!(checkLogin == null)) {
			//회원정보 갱신
			//현재 session에 있는 회원의 DTO를 받아 옴
			MemberDto dto = (MemberDto) session.getAttribute("memberDto");
			//그 DTO의 HashCode값을 이용하여 DB에서 업데이트 된 정보를 가져 와 session을 업데이트 시킨다
			session.setAttribute("memberDto", memberService.getDtoUseHash(dto.getMemberHash()));
			//장바구니를 가져오기 위해 DTO를 갱신 시킨다
			dto = (MemberDto) session.getAttribute("memberDto");
			List<CartDto> cartList = cartService.showMyCart(dto.getMemberId());
			model.addAttribute("cartList", cartList);
			System.out.println(cartList);
			return "content/bag";
		}
		ScriptUtils.alert(response, "로그인이 필요한 서비스입니다.");
		return "content/home";
	}
	
	@RequestMapping(value="/amall.alarm.com", produces="application/text;charset=utf-8")
	public String goAlarm(Model model, HttpSession session, HttpServletResponse response) throws IOException{
		model.addAttribute("checkLogin", session.getAttribute("checkLoginInSession"));
		Object checkLogin = model.getAttribute("checkLogin");
		//로그인 체크
		if (!(checkLogin == null)) {
			//회원정보 갱신
			//현재 session에 있는 회원의 DTO를 받아 옴
			MemberDto myDto = (MemberDto) session.getAttribute("memberDto");
			//그 DTO의 HashCode값을 이용하여 DB에서 업데이트 된 정보를 가져 와 session을 업데이트 시킨다
			session.setAttribute("memberDto", memberService.getDtoUseHash(myDto.getMemberHash()));
			//알람을 가져오기 위해 DTO를 갱신 시킨다
			myDto = (MemberDto) session.getAttribute("memberDto");
			//세션에 저장된 ID를 이용하여 받은 알람을 List에 담는다.
			List<Map<String, Object>> alarmList = alarmService.getMyAlarm(myDto.getMemberId());
			//alarmList의 내용을 모델에 저장함
			model.addAttribute("alarmList", alarmList);
			//th:switch를 사용하기 위해 alarmList의 사이즈를 모델에 저장함
			model.addAttribute("alarmSize", alarmList.size());
			//내 정보에서 매칭된 아이디가 있는지 확인한다
			String matchingId = myDto.getMemberMatchId();
			if (!(matchingId==null)) {
				model.addAttribute("checkMatching", true);
				model.addAttribute("matchDto", memberService.reloadMemberData(matchingId));
				System.out.println(model.getAttribute("matchDto"));
				return "content/alarm";
			}
			model.addAttribute("checkMatching", false);
			return "content/alarm";
		}
		ScriptUtils.alert(response, "로그인이 필요한 서비스입니다.");
		return "content/home";
	}
	
}
