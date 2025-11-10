/**
 * 회원가입 폼 핸들러
 *
 * header.html의 회원가입 모달과 연동
 */

// DOMContentLoaded 후 실행
document.addEventListener('DOMContentLoaded', function() {
    const signUpForm = document.querySelector('form[action="/signup/amall.com"]');

    if (!signUpForm) {
        return; // 회원가입 폼이 없으면 종료
    }

    // form action 제거 및 event listener 추가
    signUpForm.removeAttribute('action');
    signUpForm.removeAttribute('method');

    // 기존 onsubmit 제거
    signUpForm.setAttribute('onsubmit', 'return false;');

    // 새로운 submit handler 추가
    signUpForm.addEventListener('submit', async function(event) {
        event.preventDefault();

        // 폼 데이터 수집
        const formData = {
            memberId: document.getElementById('signMemberId').value,
            memberName: document.getElementById('signMemberName').value,
            memberBirth: document.getElementById('signMemberBirth').value,
            memberGender: document.getElementById('signMemberSex').value,
            memberPwd: document.getElementById('signMemberPwd').value,
            memberPwdCheck: document.getElementById('signMemberPwdCheck').value,
            memberEmail: document.getElementById('signMemberEmail').value,
            memberPostCode: document.getElementById('signMemberPostCode').value,
            memberAddress: document.getElementById('signMemberAddres').value,
            memberDetailAddress: document.getElementById('signMemberDetailAddress').value,
            memberExtraAddress: document.getElementById('signMemberExtraAddress').value,
            memberPhone: document.getElementById('signMemberPhone').value
        };

        // API 호출
        const result = await signUp(formData);

        if (result) {
            // 성공: 폼 초기화
            signUpForm.reset();

            // 모달 닫기
            if (typeof offClickSignUp === 'function') {
                offClickSignUp();
            }

            // 로그인 모달 열기
            setTimeout(() => {
                if (typeof onClickLogin === 'function') {
                    onClickLogin();
                }
            }, 500);
        }
    });

    // 비밀번호 확인 필드에 name 속성 추가
    const pwdCheckField = document.getElementById('signMemberPwdCheck');
    if (pwdCheckField && !pwdCheckField.getAttribute('name')) {
        pwdCheckField.setAttribute('name', 'memberPwdCheck');
    }

    // 성별 필드 name 수정 (memberSex → memberGender)
    const genderField = document.getElementById('signMemberSex');
    if (genderField) {
        genderField.setAttribute('name', 'memberGender');
    }
});
