/* 로그인 창 Start */
function onClickLogin() {
	window.scrollTo(0, 0);
	document.querySelector(".loginModal").style.display = "block";
	document.body.style.overflow = "hidden";
}
function offClickLogin() {
	document.querySelector(".loginModal").style.display = "none";
	document.body.style.removeProperty("overflow");
}
/* 로그인 창 End */

/* 로그아웃 창 Start */
function onClickLogout() {
	window.scrollTo(0, 0);
	document.querySelector(".logoutModal").style.display = "block";
	document.body.style.overflow = "hidden";
}
function offClickLogout() {
	document.querySelector(".logoutModal").style.display = "none";
	document.body.style.removeProperty("overflow");
}
/* 로그아웃 창 End */

/* 회원가입 창 Start */
function onClickSignUp() {
	window.scrollTo(0, 0);
	document.querySelector(".signUpModal").style.display = "block";
	document.body.style.overflow = "hidden";
}
function offClickSignUp() {
	document.querySelector(".signUpModal").style.display = "none";
	document.body.style.removeProperty("overflow");
}
function onClickSignUpInLogin() {
	document.querySelector(".loginModal").style.display = "none";
	document.querySelector(".signUpModal").style.display = "block";
}
function selectSignUp(id) {
	document.getElementsByClassName("signUpSelectButton").style.backgroundcolor = "#e0e0e0";
	document.getElementsByClassName("signUpSelectButton").style.color = "#e0e0e0";
}
function signUpCheck() {
	var newMemberId = document.getElementById('signMemberId').value;
	var newMemberName = document.getElementById('signMemberName').value;
	var newMemberBirth = document.getElementById('signMemberBirth').value;
	var newMemberSex = document.getElementById('signMemberSex').value;
	var newMemberPwd = document.getElementById('signMemberPwd').value;
	var newMemberPwdCheck = document.getElementById('signMemberPwdCheck').value;
	var newMemberEmail = document.getElementById('signMemberEmail').value;
	var newMemberPhone = document.getElementById('signMemberPhone').value;
	var newMemberPostCode = document.getElementById('signMemberPostCode').value;
	var newMemberDetailAddress = document.getElementById('signMemberDetailAddress').value;
	let regExpId = /^(?=.*[a-zA-Z])(?=.*[0-9]).{6,20}$/;
	let regExpPwd = /^(?=.*[a-zA-Z])(?=.*[0-9]).{8,20}$/;
	let regExpBirth = /([0-9]{2}(0[1-9]|1[0-2])(0[1-9]|[1,2][0-9]|3[0,1]))/g;
	let regExpSex = /^\d{1}$/;
	let regExpEmail = /^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$/i;
	let regExpPhone = /^\d{11}$/;
	
	// 1) 빈 칸 없이 다 입력되었는지 확인
	
	if(!newMemberId || (!regExpId.test(newMemberId))) {
		alert("아이디는 영문, 숫자 조합으로 6-20자리 입력해주세요.");
		document.getElementById('signMemberId').focus();
		return false;
	}
	if(!newMemberName) {
		alert('이름을 입력해주세요.');
		document.getElementById('signMemberName').focus();
		return false;
	}
	if(!newMemberBirth || (!regExpBirth.test(newMemberBirth)) || (!regExpSex.test(newMemberSex))) {
		alert('주민등록번호를 뒷자리 첫번째까지 입력해주세요.');
		document.getElementById('signMemberBirth').focus();
		return false;
	}
	if(!newMemberPwd || (!regExpPwd.test(newMemberPwd))) {
		alert('비밀번호는 영문, 숫자 조합으로 8-20자리 입력해주세요.');
		document.getElementById('signMemberPwd').focus();
		return false;
	}
	if(!newMemberPwdCheck) {
		alert('비밀번호 확인을 입력해주세요.');
		document.getElementById('signMemberPwdCheck').focus();
		return false;
	}
	if(!newMemberEmail || (!regExpEmail.test(newMemberEmail))) {
		alert('이메일 형식이 아닙니다.');
		document.getElementById('signMemberEmail').focus();
		return false;
	}
	if(!newMemberPhone || (!regExpPhone.test(newMemberPhone))) {
		alert("-을 제외한 연락처를 입력하세요.");
		document.getElementById('signMemberPhone').focus();
		return false;
	}
	if(!newMemberPostCode) {
		alert('우편번호 찾기를 눌러주세요.');
		return false;
	}
	if(!newMemberDetailAddress) {
		alert('상세주소를 입력해주세요.');
		document.getElementById('signMemberName').focus();
		return false;
	}
	
	// 2) 비밀번호 비밀번호 확인 일치 여부
	if(newMemberPwd != newMemberPwdCheck) {
		alert('비밀번호가 서로 다릅니다.');
		document.getElementById('signMemberPwd').value='';
		document.getElementById('signMemberPwdCheck').value='';
		document.getElementById('signMemberPwd').focus();
		return false;
	}
	
	alert('회원가입을 축하합니다.');
}
/* 회원가입 창 End */

/* 매칭 링크 모달창 Start*/
function onClickMatchingLink() {
	document.querySelector(".matchingLinkModal").style.display = "block";
	document.body.style.overflow = "hidden";
}
function offClickMatchingLink() {
	document.querySelector(".matchingLinkModal").style.display = "none";
	document.body.style.removeProperty("overflow");
}
/* 매칭 링크 모달창 End*/