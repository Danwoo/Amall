function checkUpdateProfile() {
	var updateMemberPwd = document.getElementById('updateProfilePwd').value;
	var updateMemberCheckPwd = document.getElementById('updateProfileCheckPwd').value;
	var updateMemberPhone = document.getElementById('updateProfilePhone').value;
	var updateMemberEmail = document.getElementById('updateProfileEmail').value;
	var updateMemberBirth = document.getElementById('updateProfileBirth').value;
	var updateMemberPostCode = document.getElementById('updateProfilePostcode').value;
	var updateMemberDetailAddress = document.getElementById('updateProfileDetailAddress').value;
	let regExpPwd = /^(?=.*[a-zA-Z])(?=.*[0-9]).{8,20}$/;
	let regExpBirth = /^\d{6}$/;
	let regExpEmail = /^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$/i;
	let regExpPhone = /^\d{11}$/;
	
	if(!updateMemberPwd || (!regExpPwd.test(updateMemberPwd))) {
		alert('비밀번호는 영문, 숫자 조합으로 8-20자리 입력해주세요.');
		document.getElementById('updateProfilePwd').focus();
		return false;
	}
	if(!updateMemberCheckPwd) {
		alert('비밀번호 확인을 입력해주세요.');
		document.getElementById('updateProfileCheckPwd').focus();
		return false;
	}
	if(!updateMemberPhone || (!regExpPhone.test(updateMemberPhone))) {
		alert("-을 제외한 연락처를 입력하세요.");
		document.getElementById('updateProfilePhone').focus();
		return false;
	}
	if(!updateMemberEmail || (!regExpEmail.test(updateMemberEmail))) {
		alert('이메일 형식이 아닙니다.');
		document.getElementById('updateProfileEmail').focus();
		return false;
	}
	if(!updateMemberBirth || (!regExpBirth.test(updateMemberBirth))) {
		alert('생년월일 6자리를 입력해주세요.');
		document.getElementById('updateProfileBirth').focus();
		return false;
	}
	if(!updateMemberPostCode) {
		alert('우편번호 찾기를 눌러주세요.');
		return false;
	}
	if(!updateMemberDetailAddress) {
		alert('상세주소를 입력해주세요.');
		document.getElementById('signMemberName').focus();
		return false;
	}
	if(updateMemberPwd != updateMemberCheckPwd) {
		alert('비밀번호가 서로 다릅니다.');
		document.getElementById('updateProfilePwd').value='';
		document.getElementById('updateProfileCheckPwd').value='';
		document.getElementById('updateProfilePwd').focus();
		return false;
	}
	document.getElementById('upgradeProfileForm').onsubmit();
}

function copyValue(value) {
  var input = document.getElementById("myHash");
  navigator.clipboard.writeText(value.placeholder);
  alert('클립보드에 매칭코드가 복사됐습니다.');
}

function onClickDeleteId() {
	window.scrollTo(0, 0);
	document.querySelector(".deleteIdModal").style.display = "block";
	document.body.style.overflow = "hidden";
}

function offClickDeleteId() {
	document.querySelector(".deleteIdModal").style.display = "none";
	document.body.style.removeProperty("overflow");
}