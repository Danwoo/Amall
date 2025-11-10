package project.amall.order.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import project.amall.order.dto.OrderDto;
import project.amall.order.dto.OrderItemDto;

import java.util.List;

/**
 * 주문 Mapper
 *
 * Phase 9: Order System Implementation
 */
@Mapper
public interface OrderMapper {

	/**
	 * 주문 생성
	 *
	 * @param orderDto 주문 정보
	 * @return 생성된 행 수
	 */
	int createOrder(OrderDto orderDto);

	/**
	 * 주문 상품 추가
	 *
	 * @param orderItemDto 주문 상품 정보
	 * @return 추가된 행 수
	 */
	int addOrderItem(OrderItemDto orderItemDto);

	/**
	 * 주문 조회 (ID로)
	 *
	 * @param orderId 주문 ID
	 * @return 주문 정보
	 */
	OrderDto findOrderById(String orderId);

	/**
	 * 주문 상품 목록 조회
	 *
	 * @param orderId 주문 ID
	 * @return 주문 상품 목록
	 */
	List<OrderItemDto> findOrderItemsByOrderId(String orderId);

	/**
	 * 회원의 주문 목록 조회
	 *
	 * @param memberId 회원 ID
	 * @return 주문 목록
	 */
	List<OrderDto> findOrdersByMemberId(String memberId);

	/**
	 * 주문 상태 업데이트
	 *
	 * @param orderId 주문 ID
	 * @param status 새 상태
	 * @return 수정된 행 수
	 */
	int updateOrderStatus(@Param("orderId") String orderId, @Param("status") String status);

	/**
	 * 배송 정보 업데이트
	 *
	 * @param orderDto 배송 정보가 포함된 주문 DTO
	 * @return 수정된 행 수
	 */
	int updateDeliveryInfo(OrderDto orderDto);

	/**
	 * 회원이 선물 받은 주문 목록 조회
	 *
	 * @param memberId 회원 ID (선물 받은 사람)
	 * @return 선물 받은 주문 목록
	 */
	List<OrderDto> findGiftOrdersReceivedByMemberId(String memberId);
}
