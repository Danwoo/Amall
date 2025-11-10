package project.amall.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.amall.order.dto.OrderDto;
import project.amall.order.dto.OrderItemDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 주문 응답 DTO
 *
 * Phase 9: Order System Implementation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

	// 기본 정보
	private String orderId;
	private LocalDateTime orderDate;
	private String orderStatus;
	private String memberId;

	// 배송 정보
	private String deliveryName;
	private String deliveryPhone;
	private String deliveryPostCode;
	private String deliveryAddress;
	private String deliveryDetailAddress;
	private String deliveryMessage;

	// 결제 정보
	private Integer totalAmount;
	private Integer deliveryFee;
	private String paymentMethod;

	// 선물 정보
	private String isGift;
	private String giftFromMemberId;
	private String giftMessage;

	// 주문 상품 목록
	private List<OrderItemResponse> orderItems;

	/**
	 * OrderDto로부터 OrderResponse 생성
	 */
	public static OrderResponse from(OrderDto orderDto) {
		OrderResponseBuilder builder = OrderResponse.builder()
				.orderId(orderDto.getOrderId())
				.orderDate(orderDto.getOrderDate())
				.orderStatus(orderDto.getOrderStatus())
				.memberId(orderDto.getMemberId())
				.deliveryName(orderDto.getDeliveryName())
				.deliveryPhone(orderDto.getDeliveryPhone())
				.deliveryPostCode(orderDto.getDeliveryPostCode())
				.deliveryAddress(orderDto.getDeliveryAddress())
				.deliveryDetailAddress(orderDto.getDeliveryDetailAddress())
				.deliveryMessage(orderDto.getDeliveryMessage())
				.totalAmount(orderDto.getTotalAmount())
				.deliveryFee(orderDto.getDeliveryFee())
				.paymentMethod(orderDto.getPaymentMethod())
				.isGift(orderDto.getIsGift())
				.giftFromMemberId(orderDto.getGiftFromMemberId())
				.giftMessage(orderDto.getGiftMessage());

		// 주문 상품 목록이 있으면 포함
		if (orderDto.getOrderItems() != null) {
			List<OrderItemResponse> items = orderDto.getOrderItems().stream()
					.map(OrderItemResponse::from)
					.collect(Collectors.toList());
			builder.orderItems(items);
		}

		return builder.build();
	}
}
