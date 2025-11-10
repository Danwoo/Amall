package project.amall.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.amall.cart.dto.CartDto;
import project.amall.cart.mapper.CartMapper;
import project.amall.common.constants.AppConstants;
import project.amall.common.exception.BusinessException;
import project.amall.common.exception.code.ErrorCode;
import project.amall.common.util.IdGenerator;
import project.amall.gift.dto.GiftDeliveryRequestDto;
import project.amall.gift.mapper.GiftDeliveryRequestMapper;
import project.amall.member.dto.MemberDto;
import project.amall.member.mapper.MemberMapper;
import project.amall.order.dto.OrderDto;
import project.amall.order.dto.OrderItemDto;
import project.amall.order.mapper.OrderMapper;
import project.amall.product.dto.ProductDto;
import project.amall.product.mapper.ProductMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * 주문 서비스
 *
 * Phase 9: Order System Implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

	private final OrderMapper orderMapper;
	private final CartMapper cartMapper;
	private final ProductMapper productMapper;
	private final MemberMapper memberMapper;
	private final GiftDeliveryRequestMapper giftDeliveryRequestMapper;
	private final project.amall.alarm.service.AlarmService alarmService;

	/**
	 * 일반 주문 생성
	 *
	 * @param memberId 회원 ID
	 * @param cartIds 장바구니 ID 목록
	 * @param deliveryName 받는 사람 이름
	 * @param deliveryPhone 받는 사람 전화번호
	 * @param deliveryPostCode 우편번호
	 * @param deliveryAddress 주소
	 * @param deliveryDetailAddress 상세주소
	 * @param deliveryMessage 배송 메시지
	 * @param paymentMethod 결제 방법
	 * @return 주문 ID
	 * @throws BusinessException 회원이 없거나, 장바구니를 찾을 수 없거나, 재고가 부족한 경우
	 */
	@Transactional
	public String createOrder(
			String memberId,
			List<Integer> cartIds,
			String deliveryName,
			String deliveryPhone,
			String deliveryPostCode,
			String deliveryAddress,
			String deliveryDetailAddress,
			String deliveryMessage,
			String paymentMethod
	) {
		log.debug("일반 주문 생성: memberId={}, cartIds={}", memberId, cartIds);

		// (1) 회원 존재 여부 확인
		validateMemberExists(memberId);

		// (2) 장바구니 아이템 조회 및 검증
		List<CartDto> cartList = getCartListByIds(memberId, cartIds);
		validateNoGiftItems(cartList);

		// (3) 총 금액 계산
		int totalAmount = calculateTotalAmount(cartList);
		int deliveryFee = calculateDeliveryFee(cartList);

		// (4) 주문 생성
		String orderId = IdGenerator.generateOrderId(memberId);
		OrderDto order = new OrderDto();
		order.setOrderId(orderId);
		order.setOrderStatus(OrderDto.OrderStatus.PENDING.getCode());
		order.setMemberId(memberId);
		order.setDeliveryName(deliveryName);
		order.setDeliveryPhone(deliveryPhone);
		order.setDeliveryPostCode(deliveryPostCode);
		order.setDeliveryAddress(deliveryAddress);
		order.setDeliveryDetailAddress(deliveryDetailAddress);
		order.setDeliveryMessage(deliveryMessage);
		order.setTotalAmount(totalAmount);
		order.setDeliveryFee(deliveryFee);
		order.setPaymentMethod(paymentMethod);
		order.setAsNormalOrder();

		int result = orderMapper.createOrder(order);
		if (result == 0) {
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "주문 생성 실패");
		}

		// (5) 재고 차감
		for (CartDto cart : cartList) {
			decreaseProductStock(cart);
		}

		// (6) 주문 상품 추가
		for (CartDto cart : cartList) {
			addOrderItem(orderId, cart);
		}

		// (7) 장바구니에서 제거
		for (Integer cartId : cartIds) {
			cartMapper.removeFromCart(cartId);
		}

		log.info("주문 생성 완료: orderId={}", orderId);
		return orderId;
	}

	/**
	 * 선물 주문 생성
	 *
	 * 선물 주문은 배송지 정보 없이 생성되며,
	 * 선물 받는 사람이 나중에 배송지를 입력합니다.
	 *
	 * @param memberId 회원 ID (선물 보내는 사람)
	 * @param cartId 장바구니 ID (선물 상품 1개)
	 * @param giftMessage 선물 메시지
	 * @param paymentMethod 결제 방법
	 * @return 주문 ID
	 * @throws BusinessException 회원이 없거나, 선물 상품이 아니거나, 재고가 부족한 경우
	 */
	@Transactional
	public String createGiftOrder(
			String memberId,
			int cartId,
			String giftMessage,
			String paymentMethod
	) {
		log.debug("선물 주문 생성: memberId={}, cartId={}, giftMessage={}", memberId, cartId, giftMessage);

		// (1) 회원 존재 여부 확인
		validateMemberExists(memberId);

		// (2) 장바구니 아이템 조회 및 검증
		List<CartDto> cartList = getCartListByIds(memberId, List.of(cartId));
		if (cartList.isEmpty()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "장바구니를 찾을 수 없습니다.");
		}

		CartDto cart = cartList.get(0);

		// (3) 선물 정보 검증
		if (!AppConstants.IS_GIFT_YES.equals(cart.getIsGift())) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "선물 상품이 아닙니다.");
		}

		if (cart.getGiftToMemberId() == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "선물 받는 사람이 지정되지 않았습니다.");
		}

		// (4) 선물 받는 사람 존재 여부 확인
		MemberDto recipient = memberMapper.reloadMemberData(cart.getGiftToMemberId());
		if (recipient == null) {
			throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND, "선물 받는 사람을 찾을 수 없습니다: " + cart.getGiftToMemberId());
		}

		// (5) 총 금액 계산
		int totalAmount = calculateTotalAmount(cartList);
		int deliveryFee = calculateDeliveryFee(cartList);

		// (6) 재고 차감
		decreaseProductStock(cart);

		// (7) 주문 생성 (배송지 정보 없음)
		String orderId = IdGenerator.generateOrderId(memberId);
		OrderDto order = new OrderDto();
		order.setOrderId(orderId);
		order.setOrderStatus(OrderDto.OrderStatus.PENDING.getCode());
		order.setMemberId(cart.getGiftToMemberId()); // 선물 받는 사람이 주문자
		order.setTotalAmount(totalAmount);
		order.setDeliveryFee(deliveryFee);
		order.setPaymentMethod(paymentMethod);
		order.setAsGiftOrder(memberId, giftMessage);

		int result = orderMapper.createOrder(order);
		if (result == 0) {
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "주문 생성 실패");
		}

		// (8) 주문 상품 추가
		addOrderItem(orderId, cart);

		// (9) 선물 배송지 입력 요청 생성
		GiftDeliveryRequestDto giftRequest = new GiftDeliveryRequestDto();
		giftRequest.setOrderId(orderId);
		giftRequest.setToMemberId(cart.getGiftToMemberId());
		giftRequest.setFromMemberId(memberId);
		giftRequest.setGiftMessage(giftMessage);
		giftRequest.setRequestStatus(GiftDeliveryRequestDto.RequestStatus.PENDING.getCode());

		giftDeliveryRequestMapper.createGiftDeliveryRequest(giftRequest);

		// (10) 선물 알림 생성
		alarmService.createGiftNotification(orderId, memberId, cart.getGiftToMemberId());

		// (11) 장바구니에서 제거
		cartMapper.removeFromCart(cartId);

		log.info("선물 주문 생성 완료: orderId={}, toMember={}", orderId, cart.getGiftToMemberId());
		return orderId;
	}

	/**
	 * 배송지 정보 업데이트 (선물 받는 사람이 입력)
	 *
	 * @param memberId 회원 ID (선물 받는 사람)
	 * @param orderId 주문 ID
	 * @param deliveryName 받는 사람 이름
	 * @param deliveryPhone 받는 사람 전화번호
	 * @param deliveryPostCode 우편번호
	 * @param deliveryAddress 주소
	 * @param deliveryDetailAddress 상세주소
	 * @param deliveryMessage 배송 메시지
	 * @throws BusinessException 주문이 없거나, 권한이 없거나, 선물 주문이 아닌 경우
	 */
	@Transactional
	public void updateDeliveryAddress(
			String memberId,
			String orderId,
			String deliveryName,
			String deliveryPhone,
			String deliveryPostCode,
			String deliveryAddress,
			String deliveryDetailAddress,
			String deliveryMessage
	) {
		log.debug("배송지 정보 업데이트: memberId={}, orderId={}", memberId, orderId);

		// (1) 주문 조회
		OrderDto order = getOrderWithItems(orderId);

		// (2) 권한 확인 (선물 받는 사람만 가능)
		if (!memberId.equals(order.getMemberId())) {
			throw new BusinessException(ErrorCode.FORBIDDEN, "배송지 입력 권한이 없습니다.");
		}

		// (3) 선물 주문인지 확인
		if (!AppConstants.IS_GIFT_YES.equals(order.getIsGift())) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "선물 주문이 아닙니다.");
		}

		// (4) 배송지 정보 업데이트
		order.setDeliveryName(deliveryName);
		order.setDeliveryPhone(deliveryPhone);
		order.setDeliveryPostCode(deliveryPostCode);
		order.setDeliveryAddress(deliveryAddress);
		order.setDeliveryDetailAddress(deliveryDetailAddress);
		order.setDeliveryMessage(deliveryMessage);

		int result = orderMapper.updateDeliveryInfo(order);
		if (result == 0) {
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "배송지 정보 업데이트 실패");
		}

		// (5) 선물 배송지 입력 요청 상태 업데이트
		GiftDeliveryRequestDto giftRequest = giftDeliveryRequestMapper.findRequestByOrderId(orderId);
		if (giftRequest != null) {
			giftDeliveryRequestMapper.updateRequestStatus(
					giftRequest.getRequestId(),
					GiftDeliveryRequestDto.RequestStatus.COMPLETED.getCode()
			);
		}

		// (6) 주문 상태를 PAID로 변경 (결제 완료 상태로)
		orderMapper.updateOrderStatus(orderId, OrderDto.OrderStatus.PAID.getCode());

		log.info("배송지 정보 업데이트 완료: orderId={}", orderId);
	}

	/**
	 * 주문 조회 (주문 상품 포함)
	 *
	 * @param orderId 주문 ID
	 * @return 주문 정보
	 * @throws BusinessException 주문을 찾을 수 없는 경우
	 */
	public OrderDto getOrderWithItems(String orderId) {
		OrderDto order = orderMapper.findOrderById(orderId);
		if (order == null) {
			throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "orderId=" + orderId);
		}

		// 주문 상품 목록 조회
		List<OrderItemDto> orderItems = orderMapper.findOrderItemsByOrderId(orderId);
		order.setOrderItems(orderItems);

		return order;
	}

	/**
	 * 회원의 주문 목록 조회
	 *
	 * @param memberId 회원 ID
	 * @return 주문 목록
	 */
	public List<OrderDto> getMemberOrders(String memberId) {
		log.debug("회원 주문 목록 조회: memberId={}", memberId);
		return orderMapper.findOrdersByMemberId(memberId);
	}

	/**
	 * 회원이 선물 받은 주문 목록 조회
	 *
	 * @param memberId 회원 ID
	 * @return 선물 받은 주문 목록
	 */
	public List<OrderDto> getGiftOrdersReceived(String memberId) {
		log.debug("선물 받은 주문 목록 조회: memberId={}", memberId);
		return orderMapper.findGiftOrdersReceivedByMemberId(memberId);
	}

	/**
	 * 주문 취소
	 *
	 * @param memberId 회원 ID
	 * @param orderId 주문 ID
	 * @throws BusinessException 주문이 없거나, 권한이 없거나, 취소할 수 없는 상태인 경우
	 */
	@Transactional
	public void cancelOrder(String memberId, String orderId) {
		log.debug("주문 취소: memberId={}, orderId={}", memberId, orderId);

		// (1) 주문 조회
		OrderDto order = getOrderWithItems(orderId);

		// (2) 권한 확인
		if (!memberId.equals(order.getMemberId())) {
			// 선물 주문의 경우 선물 보낸 사람도 취소 가능
			if (!AppConstants.IS_GIFT_YES.equals(order.getIsGift()) || !memberId.equals(order.getGiftFromMemberId())) {
				throw new BusinessException(ErrorCode.FORBIDDEN, "주문 취소 권한이 없습니다.");
			}
		}

		// (3) 취소 가능 상태 확인 (PENDING, PAID만 취소 가능)
		if (!OrderDto.OrderStatus.PENDING.getCode().equals(order.getOrderStatus()) &&
			!OrderDto.OrderStatus.PAID.getCode().equals(order.getOrderStatus())) {
			throw new BusinessException(
					ErrorCode.INVALID_INPUT_VALUE,
					"취소할 수 없는 주문 상태입니다: " + order.getOrderStatus()
			);
		}

		// (4) 재고 복원
		for (OrderItemDto orderItem : order.getOrderItems()) {
			int result = productMapper.increaseStock(orderItem.getProdNum(), orderItem.getQuantity());
			if (result == 0) {
				log.error("재고 복원 실패: prodNum={}, quantity={}", orderItem.getProdNum(), orderItem.getQuantity());
				throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "재고 복원에 실패했습니다.");
			}
		}

		// (5) 주문 상태 변경
		int result = orderMapper.updateOrderStatus(orderId, OrderDto.OrderStatus.CANCELLED.getCode());
		if (result == 0) {
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "주문 취소에 실패했습니다.");
		}

		// (6) 선물 주문인 경우 배송지 입력 요청도 취소 처리
		if (AppConstants.IS_GIFT_YES.equals(order.getIsGift())) {
			GiftDeliveryRequestDto giftRequest = giftDeliveryRequestMapper.findRequestByOrderId(orderId);
			if (giftRequest != null && AppConstants.GIFT_REQUEST_STATUS_PENDING.equals(giftRequest.getRequestStatus())) {
				// 배송지 입력 요청을 CANCELLED 상태로 변경 (새로운 상태 필요)
				// 현재는 스키마에 CANCELLED 상태가 없으므로 스킵
				log.info("선물 배송지 입력 요청 취소: requestId={}", giftRequest.getRequestId());
			}
		}

		log.info("주문 취소 완료: orderId={}", orderId);
	}

	// ===== 내부 메서드 =====

	/**
	 * 장바구니 ID 목록으로 장바구니 조회
	 */
	private List<CartDto> getCartListByIds(String memberId, List<Integer> cartIds) {
		List<CartDto> allCart = cartMapper.showMyCart(memberId);
		List<CartDto> result = new ArrayList<>();

		for (Integer cartId : cartIds) {
			CartDto cart = allCart.stream()
					.filter(c -> c.getCartId() == cartId)
					.findFirst()
					.orElseThrow(() -> new BusinessException(
							ErrorCode.INVALID_INPUT_VALUE,
							"장바구니를 찾을 수 없습니다: cartId=" + cartId
					));

			// 상품 정보 확인
			if (cart.getProductDto() == null) {
				throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "상품 정보를 찾을 수 없습니다.");
			}

			// 재고 확인
			ProductDto product = cart.getProductDto();
			if (product.getProdStock() < cart.getCartQuantity()) {
				throw new BusinessException(
						ErrorCode.INVALID_INPUT_VALUE,
						String.format("재고 부족: %s (요청 수량=%d, 재고=%d)",
								product.getProdName(), cart.getCartQuantity(), product.getProdStock())
				);
			}

			result.add(cart);
		}

		return result;
	}

	/**
	 * 총 금액 계산
	 */
	private int calculateTotalAmount(List<CartDto> cartList) {
		return cartList.stream()
				.mapToInt(cart -> cart.getProductDto().getProdPrice() * cart.getCartQuantity())
				.sum();
	}

	/**
	 * 배송비 계산 (간단 구현: 가장 높은 배송비 적용)
	 */
	private int calculateDeliveryFee(List<CartDto> cartList) {
		return cartList.stream()
				.mapToInt(cart -> cart.getProductDto().getProdDeliveryfee() != null
						? cart.getProductDto().getProdDeliveryfee()
						: 0)
				.max()
				.orElse(0);
	}

	// 주문 ID 생성은 IdGenerator로 통합됨

	/**
	 * 주문 상품 추가
	 */
	private void addOrderItem(String orderId, CartDto cart) {
		ProductDto product = cart.getProductDto();

		OrderItemDto orderItem = new OrderItemDto();
		orderItem.setOrderId(orderId);
		orderItem.setProdNum(product.getProdNum());
		orderItem.setProdName(product.getProdName());
		orderItem.setProdPrice(product.getProdPrice());
		orderItem.setQuantity(cart.getCartQuantity());

		int result = orderMapper.addOrderItem(orderItem);
		if (result == 0) {
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "주문 상품 추가 실패");
		}
	}

	/**
	 * 회원 존재 여부 확인
	 *
	 * @param memberId 회원 ID
	 * @return 회원 정보
	 * @throws BusinessException 회원이 없는 경우
	 */
	private MemberDto validateMemberExists(String memberId) {
		MemberDto member = memberMapper.reloadMemberData(memberId);
		if (member == null) {
			throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND, "memberId=" + memberId);
		}
		return member;
	}

	/**
	 * 장바구니 아이템들이 선물이 아닌지 검증
	 *
	 * @param cartList 장바구니 목록
	 * @throws BusinessException 선물 상품이 포함된 경우
	 */
	private void validateNoGiftItems(List<CartDto> cartList) {
		boolean hasGiftItem = cartList.stream()
			.anyMatch(cart -> AppConstants.IS_GIFT_YES.equals(cart.getIsGift()));
		if (hasGiftItem) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
				"선물 상품은 별도로 주문해야 합니다.");
		}
	}

	/**
	 * 재고 감소 처리
	 *
	 * @param cart 장바구니 아이템
	 * @throws BusinessException 재고 감소 실패 시
	 */
	private void decreaseProductStock(CartDto cart) {
		int stockResult = productMapper.decreaseStock(
			cart.getProdNum(),
			cart.getCartQuantity()
		);
		if (stockResult == 0) {
			throw new BusinessException(
				ErrorCode.INTERNAL_SERVER_ERROR,
				String.format("재고 감소 실패: prodNum=%d, quantity=%d",
					cart.getProdNum(), cart.getCartQuantity())
			);
		}
	}
}
