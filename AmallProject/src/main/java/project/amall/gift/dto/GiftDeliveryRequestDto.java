package project.amall.gift.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 선물 배송지 입력 요청 DTO
 *
 * 선물 주문 시 생성되며, 선물 받는 사람이 배송지를 입력하면 완료됩니다.
 *
 * Phase 9: Order System Implementation
 */
@Data
public class GiftDeliveryRequestDto {

	private Integer requestId;                  // 요청 ID
	private String orderId;                     // 주문 ID
	private String toMemberId;                  // 선물 받는 사람 ID
	private String fromMemberId;                // 선물 보낸 사람 ID
	private String giftMessage;                 // 선물 메시지
	private String requestStatus;               // 요청 상태 (PENDING, COMPLETED)
	private LocalDateTime createdDate;          // 생성 일시
	private LocalDateTime completedDate;        // 완료 일시

	/**
	 * 요청 상태 Enum
	 */
	public enum RequestStatus {
		PENDING("PENDING", "배송지 입력 대기"),
		COMPLETED("COMPLETED", "배송지 입력 완료");

		private final String code;
		private final String description;

		RequestStatus(String code, String description) {
			this.code = code;
			this.description = description;
		}

		public String getCode() {
			return code;
		}

		public String getDescription() {
			return description;
		}
	}

	/**
	 * 완료 여부 확인
	 */
	public boolean isCompleted() {
		return "COMPLETED".equals(this.requestStatus);
	}
}
