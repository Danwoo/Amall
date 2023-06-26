package project.amall.member.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import project.amall.alarm.dto.AlarmDto;
import project.amall.alarm.service.AlarmService;
import project.amall.controller.AmallController;
import project.amall.member.dto.MemberDto;
import project.amall.member.hashcode.HashCode;
import project.amall.member.service.MemberService;
import project.amall.product.dto.ProductDto;
import project.amall.product.service.ProductService;
import project.amall.script.utils.ScriptUtils;

@Controller
public class MemberController {
	
	@Autowired
	private AmallController amallController;
	@Autowired
	private MemberService memberService;
	@Autowired
	private AlarmService alarmService;
	@Autowired
	private ProductService productService;
	
	
	@RequestMapping(value="/signup/amall.com", produces="application/text;charset=utf-8")
	public String signUpMember(@ModelAttribute MemberDto member, Model model) throws Exception {
		member.setMemberHash(HashCode.convert(member.getMemberId()));
		memberService.signUpMember(member);
		List<ProductDto> mainGift = productService.showMain("GIFT");
		List<ProductDto> mainCouple = productService.showMain("COUPLE");
		model.addAttribute("mainGift", mainGift);
		model.addAttribute("mainCouple", mainCouple);
		return "content/home";
	}
	
	@RequestMapping(value="/login/amall.com", produces="application/text;charset=utf-8")
	public String loginMember(HttpSession session, Model model, HttpServletResponse response, MemberDto member) throws IOException {
		MemberDto dto = memberService.loginMember(member);
		if (!(dto==null)) {
			memberService.updateConnectDate(dto);
			session.setAttribute("memberDto", dto);
			session.setAttribute("checkLoginInSession", true);
			model.addAttribute("checkLogin", session.getAttribute("checkLoginInSession"));
			return amallController.goHome(session, model);
		}
		ScriptUtils.alert(response, "로그인에 실패하였습니다.");
		return amallController.goHome(session, model);
	}
	
	@RequestMapping(value="/logout/amall.com", produces="application/text;charset=utf-8")
	public String logoutMember(HttpSession session, Model model) {
		session.invalidate();
		List<ProductDto> mainGift = productService.showMain("GIFT");
		List<ProductDto> mainCouple = productService.showMain("COUPLE");
		model.addAttribute("mainGift", mainGift);
		model.addAttribute("mainCouple", mainCouple);
		return "content/home";
	}
	
	@RequestMapping(value="/updateprofile/amall.com", produces="application/text;charset=utf-8")
	public String updateMember(HttpSession session, Model model, HttpServletResponse response, MemberDto member) throws IOException {
		//세션에서 checkLoginInSession의 값을 가져와 로그인 상태를 확인함(로그인X : null, 로그인O : true)
		model.addAttribute("checkLogin", session.getAttribute("checkLoginInSession"));
		//세션에서 로그인 된 계정의 정보를 DTO에 담는다
		MemberDto dto = (MemberDto) session.getAttribute("memberDto");
		//받아온 DTO에는 HashCode가 없으니 세션에서 HashCode를 가져와 넣는다
		member.setMemberHash(dto.getMemberHash());
		//수정된 정보로 DB에 있는 정보를 업데이트 시킨다
		memberService.updateMember(member);
		//세션의 정보를 업데이트 한다
		dto = memberService.getDtoUseHash(dto.getMemberHash());
		session.setAttribute("memberDto", dto);
		ScriptUtils.alert(response, "회원정보가 수정되었습니다.");
		return "content/myPage";
	}
	
	@RequestMapping(value="/sendMatching/amall.com", produces="application/text;charset=utf-8")
	public String sendMatching(HttpSession session, Model model, HttpServletResponse response, String memberMatchHash) throws IOException {
		/* (1).내가 이미 매칭 된 상태일 경우
		 * (2).잘못 입력한 경우
		 * (3).이미 상대가 매칭된 상태일 경우
		 * (4).자기 자신에게 보낸 경우
		 * (5).정상 입력 했지만 이미 보낸 경우
		 * (6).정상 입력한 경우
		 * */
		//세션에서 나의 DTO를 가져옴
		MemberDto myDto = (MemberDto) session.getAttribute("memberDto");
		//내 DTO에서 내 매칭 상대의 아이디를 가져옴
		String myMatchId = myDto.getMemberMatchId();
		//내가 이미 매칭 된 상태인지 확인
		if (!(myMatchId==null)) {
			//내가 이미 매칭 된 상태인 경우(1)
			ScriptUtils.alert(response, "매칭은 1명만 가능합니다.");
			return amallController.goUpdateProfile(session, model, response, myDto.getMemberPwd());
		}
		//입력된 매칭상대의 해쉬코드로 해당 회원의 정보를 가져옴
		MemberDto yourDto = memberService.getDtoUseHash(memberMatchHash);
		//가져온 상대 정보가 null인지 확인
		if (!(yourDto==null)) {
			//정상적으로 아이디 정보를 가져왔을 때
			//가져온 매칭 상대의 정보에서 아이디를 matchId에 저장
			String matchId = yourDto.getMemberId();
			//나의 아이디를 myDto에서 가져와 myId에 저장
			String myId = myDto.getMemberId();
			//이미 상대가 매칭이 되어있는 상태인지 확인
			if (!(yourDto.getMemberMatchId()==null)) {
				//이미 상대가 매칭이 되어있는 상태인 경우(3)
				ScriptUtils.alert(response, "상대가 이미 매칭이 된 상태입니다.");
				return amallController.goUpdateProfile(session, model, response, myDto.getMemberPwd());
			}
			//입력한 해쉬코드가 다른 사람의 것인지 확인
			if (myId.equals(matchId)) {
				//입력한 해쉬코드가 자기 자신의 해쉬코드인 경우(4)
				ScriptUtils.alert(response, "자기 자신에게는 메세지를 보낼 수 없습니다.");
				return amallController.goUpdateProfile(session, model, response, myDto.getMemberPwd());
			}
			//이미 상대에게 전송된 상태인지 확인
			AlarmDto checkSend = alarmService.CheckSend(myId, matchId);
			if(!(checkSend==null)) {
				//이미 이전에 전송함(5)
				ScriptUtils.alert(response, "이미 메세지를 전송하셨습니다.");
				return amallController.goUpdateProfile(session, model, response, myDto.getMemberPwd());
			}
			//주소창의 보안을 위해 코드를 생성
			String alarmId = yourDto.getMemberHash() + myDto.getMemberHash();
			//아무 문제가 없을 경우 메시지를 전송한다(6)
			alarmService.sendMatchingMassage(alarmId, myId, matchId);
			ScriptUtils.alert(response, "매칭 알람을 전송하였습니다.");
			return amallController.goMyPage(session, model, response);
		}
		//만약 해쉬코드를 잘못 입력하여 가져온 정보가 없을 경우(2)
		ScriptUtils.alert(response, "매칭 코드를 잘못 입력하였습니다.");
		return amallController.goUpdateProfile(session, model, response, myDto.getMemberPwd());
	}
	
	@RequestMapping(value="/delete/amall.com", produces="application/text;charset=utf-8")
	public String deleteMember(HttpSession session, Model model, HttpServletResponse response) throws IOException {
		MemberDto dto = (MemberDto) session.getAttribute("memberDto");
		memberService.deleteMember(dto);
		session.invalidate();
		ScriptUtils.alert(response, "계정이 삭제되었습니다.");
		List<ProductDto> mainGift = productService.showMain("GIFT");
		List<ProductDto> mainCouple = productService.showMain("COUPLE");
		model.addAttribute("mainGift", mainGift);
		model.addAttribute("mainCouple", mainCouple);
		return "content/home";
	}
	
	@RequestMapping(value="/acceptmatching/{alarmId}/amall.com", produces="application/text;charset=utf-8")
	public String acceptMatching(HttpSession session, Model model, HttpServletResponse response, @PathVariable String alarmId) throws IOException {
		//session에서 나의 해쉬코드와 아이디를 뽑아냄
		MemberDto myDto = (MemberDto) session.getAttribute("memberDto");
		String myHash = myDto.getMemberHash();
		String myId = myDto.getMemberId();
		//상대방의 해쉬코드를 뽑아냄
		String yourHash = alarmId.replaceFirst(myHash, "");
		//해쉬코드를 이용해서 상대방의 DTO를 가져옴
		MemberDto yourDto = memberService.getDtoUseHash(yourHash);
		//상대방의 DTO에서 ID를 꺼냄
		String yourId = yourDto.getMemberId();
		//해쉬코드를 이용하여 나와 상대방의 정보를 업데이트 함
		memberService.finishMatching(myHash, myId, yourHash, yourId);
		//ID를 이용하여 나와 상대방의 알람 모두 제거
		alarmService.deleteAllAlarm(myId, yourId);
		return amallController.goAlarm(model, session, response);
	}
	
}
      
