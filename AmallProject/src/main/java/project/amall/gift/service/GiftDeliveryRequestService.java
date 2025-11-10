package project.amall.gift.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.amall.common.exception.BusinessException;
import project.amall.common.exception.code.ErrorCode;
import project.amall.gift.dto.GiftDeliveryRequestDto;
import project.amall.gift.mapper.GiftDeliveryRequestMapper;

import java.util.List;

/**
 * 선물 배송지 입력 요청 서비스
 *
 * Phase 9: Order System Implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GiftDeliveryRequestService {

	private final GiftDeliveryRequestMapper giftDeliveryRequestMapper;

	/**
	 * 회원의 대기 중인 선물 배송지 입력 요청 목록 조회
	 *
	 * @param memberId 회원 ID (선물 받는 사람)
	 * @return 대기 중인 요청 목록
	 */
	public List<GiftDeliveryRequestDto> getPendingRequests(String memberId) {
		log.debug("대기 중인 선물 배송지 입력 요청 조회: memberId={}", memberId);
		return giftDeliveryRequestMapper.findPendingRequestsByMemberId(memberId);
	}

	/**
	 * 선물 배송지 입력 요청 조회 (ID로)
	 *
	 * @param requestId 요청 ID
	 * @return 요청 정보
	 * @throws BusinessException 요청을 찾을 수 없는 경우
	 */
	public GiftDeliveryRequestDto getRequestById(int requestId) {
		GiftDeliveryRequestDto request = giftDeliveryRequestMapper.findRequestById(requestId);
		if (request == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "선물 배송지 입력 요청을 찾을 수 없습니다.");
		}
		return request;
	}

	/**
	 * 주문 ID로 선물 배송지 입력 요청 조회
	 *
	 * @param orderId 주문 ID
	 * @return 요청 정보
	 * @throws BusinessException 요청을 찾을 수 없는 경우
	 */
	public GiftDeliveryRequestDto getRequestByOrderId(String orderId) {
		GiftDeliveryRequestDto request = giftDeliveryRequestMapper.findRequestByOrderId(orderId);
		if (request == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "선물 배송지 입력 요청을 찾을 수 없습니다.");
		}
		return request;
	}
}
