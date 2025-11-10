/**
 * 위시리스트 페이지 핸들러 (Phase 9-3: 선물 기능 포함)
 *
 * /wishlist/amall.com 페이지 전용
 */

document.addEventListener('DOMContentLoaded', async function() {
	// 위시리스트 페이지인지 확인
	if (!window.location.pathname.includes('/wishlist')) {
		return;
	}

	console.log('위시리스트 핸들러 초기화');

	// 현재 로그인된 사용자 ID (Thymeleaf에서 주입)
	const memberId = getMemberIdFromPage();

	if (!memberId) {
		console.warn('로그인 정보가 없습니다.');
		return;
	}

	// 위시리스트 로드
	await loadMyWishList(memberId);
	await loadPartnerWishList(memberId);

	// 선물하기 모달 설정
	setupGiftModal();
});

// getMemberIdFromPage() is now in common-utils.js

/**
 * 내 위시리스트 로드
 */
async function loadMyWishList(memberId) {
	const wishList = await getMyWishList(memberId);

	const container = document.querySelector('#myWish').nextElementSibling.querySelector('.showItem');

	if (!container) {
		console.error('위시리스트 컨테이너를 찾을 수 없습니다.');
		return;
	}

	// 기존 내용 제거
	container.innerHTML = '';

	if (wishList.length === 0) {
		container.innerHTML = '<p style="text-align: center; padding: 40px;">위시리스트가 비어있습니다.</p>';
		return;
	}

	// 위시리스트 렌더링
	wishList.forEach(item => {
		const itemHtml = `
			<div class="itemInfor" data-prod-num="${item.prodNum}">
				<div class="itemImage">
					<img src="${escapeAttribute(item.prodImage1 || '/img/default-product.png')}">
				</div>
				<div class="itemExplanation">
					<h6>${escapeHtml(item.sellerId || '')}</h6>
					<h5>${escapeHtml(item.prodName)}</h5>
					<p>${item.prodPrice ? item.prodPrice.toLocaleString() + '원' : ''}</p>
					<nav>
						<a class="like" onclick="removeFromMyWishList('${escapeAttribute(memberId)}', ${item.prodNum})">
							<img src="/img/imgCommon/Good.png" alt="삭제">
						</a>
					</nav>
				</div>
			</div>
		`;

		container.innerHTML += itemHtml;
	});
}

/**
 * 상대방 위시리스트 로드 (Phase 9-3)
 */
async function loadPartnerWishList(memberId) {
	const partnerWishList = await getPartnerWishList(memberId);

	const yourWishSection = document.querySelector('#yourWish');

	if (!yourWishSection) {
		console.error('상대방 위시리스트 섹션을 찾을 수 없습니다.');
		return;
	}

	const container = yourWishSection.nextElementSibling.querySelector('.showItem');

	if (!container) {
		console.error('상대방 위시리스트 컨테이너를 찾을 수 없습니다.');
		return;
	}

	// 기존 내용 제거
	container.innerHTML = '';

	if (partnerWishList.length === 0) {
		container.innerHTML = '<p style="text-align: center; padding: 40px;">상대방의 위시리스트가 비어있습니다.</p>';
		return;
	}

	// 위시리스트 렌더링 (선물하기 버튼 포함)
	partnerWishList.forEach(item => {
		const itemHtml = `
			<div class="itemInfor" data-prod-num="${item.prodNum}">
				<div class="itemImage">
					<img src="${escapeAttribute(item.prodImage1 || '/img/default-product.png')}">
				</div>
				<div class="itemExplanation">
					<h6>${escapeHtml(item.sellerId || '')}</h6>
					<h5>${escapeHtml(item.prodName)}</h5>
					<p>${item.prodPrice ? item.prodPrice.toLocaleString() + '원' : ''}</p>
					<button class="gift-button" onclick="openGiftModal(${item.prodNum}, '${escapeAttribute(item.prodName)}')">
						🎁 선물하기
					</button>
				</div>
			</div>
		`;

		container.innerHTML += itemHtml;
	});
}

/**
 * 내 위시리스트에서 삭제
 */
async function removeFromMyWishList(memberId, wishlistId) {
	const success = await removeFromWishList(memberId, wishlistId);

	if (success) {
		// 페이지 새로고침
		await loadMyWishList(memberId);
	}
}

/**
 * 선물하기 모달 열기 (Phase 9-3)
 */
function openGiftModal(prodNum, prodName) {
	const modal = document.getElementById('giftModal');

	if (!modal) {
		console.error('선물하기 모달을 찾을 수 없습니다.');
		return;
	}

	// 상품 정보 설정
	document.getElementById('giftProdNum').value = prodNum;
	document.getElementById('giftProdName').textContent = prodName;

	// 모달 표시
	modal.style.display = 'block';
}

/**
 * 선물하기 모달 닫기
 */
function closeGiftModal() {
	const modal = document.getElementById('giftModal');

	if (modal) {
		modal.style.display = 'none';
		// 폼 초기화
		document.getElementById('giftQuantity').value = '1';
		document.getElementById('giftMessage').value = '';
	}
}

/**
 * 선물하기 모달 설정
 */
function setupGiftModal() {
	// 모달 닫기 버튼
	const closeBtn = document.querySelector('.gift-modal-close');
	if (closeBtn) {
		closeBtn.addEventListener('click', closeGiftModal);
	}

	// 모달 외부 클릭 시 닫기
	window.addEventListener('click', function(event) {
		const modal = document.getElementById('giftModal');
		if (event.target === modal) {
			closeGiftModal();
		}
	});

	// 선물하기 폼 제출
	const giftForm = document.getElementById('giftForm');
	if (giftForm) {
		giftForm.addEventListener('submit', async function(event) {
			event.preventDefault();
			await submitGift();
		});
	}
}

/**
 * 선물하기 제출 (Phase 9-3)
 */
async function submitGift() {
	const memberId = getMemberIdFromPage();

	if (!memberId) {
		showToast('로그인 정보가 없습니다.', 'error');
		return;
	}

	const prodNum = parseInt(document.getElementById('giftProdNum').value);
	const quantity = parseInt(document.getElementById('giftQuantity').value);
	const giftMessage = document.getElementById('giftMessage').value;

	// 상대방 ID (서버에서 검증됨)
	// TODO: 실제로는 MemberDto에서 memberMatchId를 가져와야 함
	const giftToMemberId = document.getElementById('giftToMemberId')?.value;

	if (!giftToMemberId) {
		showToast('선물 받는 사람 정보를 찾을 수 없습니다.', 'error');
		return;
	}

	// 선물로 장바구니 추가
	const success = await addToCartAsGift(
		memberId,
		prodNum,
		quantity,
		giftToMemberId,
		giftMessage
	);

	if (success) {
		closeGiftModal();
		// 선택: 장바구니 페이지로 이동
		const goToCart = await confirm('장바구니로 이동하시겠습니까?');
		if (goToCart) {
			window.location.href = '/bag/amall.com';
		}
	}
}
