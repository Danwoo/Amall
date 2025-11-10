package project.amall.gift.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.amall.gift.dto.GiftDeliveryRequestDto;

import java.time.LocalDateTime;

/**
 * 선물 배송지 입력 요청 응답 DTO
 *
 * Phase 9: Order System Implementation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GiftDeliveryRequestResponse {

	private Integer requestId;
	private String orderId;
	private String toMemberId;
	private String fromMemberId;
	private String giftMessage;
	private String requestStatus;
	private LocalDateTime createdDate;
	private LocalDateTime completedDate;

	/**
	 * GiftDeliveryRequestDto로부터 Response 생성
	 */
	public static GiftDeliveryRequestResponse from(GiftDeliveryRequestDto dto) {
		return GiftDeliveryRequestResponse.builder()
				.requestId(dto.getRequestId())
				.orderId(dto.getOrderId())
				.toMemberId(dto.getToMemberId())
				.fromMemberId(dto.getFromMemberId())
				.giftMessage(dto.getGiftMessage())
				.requestStatus(dto.getRequestStatus())
				.createdDate(dto.getCreatedDate())
				.completedDate(dto.getCompletedDate())
				.build();
	}
}
