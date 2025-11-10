package project.amall.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.amall.order.dto.OrderItemDto;

/**
 * 주문 상품 응답 DTO
 *
 * Phase 9: Order System Implementation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {

	private Integer orderItemId;
	private String orderId;
	private Integer prodNum;
	private String prodName;
	private Integer prodPrice;
	private Integer quantity;
	private Integer totalPrice;                 // 상품 금액 * 수량

	/**
	 * OrderItemDto로부터 OrderItemResponse 생성
	 */
	public static OrderItemResponse from(OrderItemDto orderItemDto) {
		return OrderItemResponse.builder()
				.orderItemId(orderItemDto.getOrderItemId())
				.orderId(orderItemDto.getOrderId())
				.prodNum(orderItemDto.getProdNum())
				.prodName(orderItemDto.getProdName())
				.prodPrice(orderItemDto.getProdPrice())
				.quantity(orderItemDto.getQuantity())
				.totalPrice(orderItemDto.getTotalPrice())
				.build();
	}
}
