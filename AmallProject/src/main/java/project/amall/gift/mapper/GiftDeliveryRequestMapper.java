package project.amall.gift.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import project.amall.gift.dto.GiftDeliveryRequestDto;

import java.util.List;

/**
 * 선물 배송지 입력 요청 Mapper
 *
 * Phase 9: Order System Implementation
 */
@Mapper
public interface GiftDeliveryRequestMapper {

	/**
	 * 선물 배송지 입력 요청 생성
	 *
	 * @param requestDto 요청 정보
	 * @return 생성된 행 수
	 */
	int createGiftDeliveryRequest(GiftDeliveryRequestDto requestDto);

	/**
	 * 선물 배송지 입력 요청 조회 (ID로)
	 *
	 * @param requestId 요청 ID
	 * @return 요청 정보
	 */
	GiftDeliveryRequestDto findRequestById(int requestId);

	/**
	 * 주문 ID로 선물 배송지 입력 요청 조회
	 *
	 * @param orderId 주문 ID
	 * @return 요청 정보
	 */
	GiftDeliveryRequestDto findRequestByOrderId(String orderId);

	/**
	 * 회원의 대기 중인 선물 배송지 입력 요청 목록 조회
	 *
	 * @param toMemberId 선물 받는 사람 ID
	 * @return 대기 중인 요청 목록
	 */
	List<GiftDeliveryRequestDto> findPendingRequestsByMemberId(String toMemberId);

	/**
	 * 선물 배송지 입력 요청 상태 업데이트
	 *
	 * @param requestId 요청 ID
	 * @param status 새 상태
	 * @return 수정된 행 수
	 */
	int updateRequestStatus(@Param("requestId") int requestId, @Param("status") String status);
}
