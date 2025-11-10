package project.amall.order.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import project.amall.alarm.service.AlarmService;
import project.amall.cart.dto.CartDto;
import project.amall.cart.mapper.CartMapper;
import project.amall.common.exception.BusinessException;
import project.amall.common.exception.code.ErrorCode;
import project.amall.gift.dto.GiftDeliveryRequestDto;
import project.amall.gift.mapper.GiftDeliveryRequestMapper;
import project.amall.member.dto.MemberDto;
import project.amall.member.mapper.MemberMapper;
import project.amall.order.dto.OrderDto;
import project.amall.order.dto.OrderItemDto;
import project.amall.order.mapper.OrderMapper;
import project.amall.product.dto.ProductDto;
import project.amall.product.mapper.ProductMapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * OrderService 테스트
 *
 * Phase 9: Order System Implementation
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService 테스트")
class OrderServiceTest {

	@InjectMocks
	private OrderService orderService;

	@Mock
	private OrderMapper orderMapper;

	@Mock
	private CartMapper cartMapper;

	@Mock
	private ProductMapper productMapper;

	@Mock
	private MemberMapper memberMapper;

	@Mock
	private GiftDeliveryRequestMapper giftDeliveryRequestMapper;

	@Mock
	private AlarmService alarmService;

	// ========== createOrder 테스트 ==========

	@Test
	@DisplayName("일반 주문 생성 - 정상")
	void createOrder_Success() {
		// given
		String memberId = "testUser";
		List<Integer> cartIds = Arrays.asList(1, 2);
		String deliveryName = "홍길동";
		String deliveryPhone = "010-1234-5678";
		String deliveryPostCode = "12345";
		String deliveryAddress = "서울시 강남구";
		String deliveryDetailAddress = "101호";
		String deliveryMessage = "문 앞에 놓아주세요";
		String paymentMethod = "CARD";

		MemberDto member = createMember(memberId);
		ProductDto product1 = createProduct(1, 10, 10000);
		ProductDto product2 = createProduct(2, 20, 20000);
		CartDto cart1 = createCart(1, memberId, 1, 2, false);
		CartDto cart2 = createCart(2, memberId, 2, 3, false);
		cart1.setProductDto(product1);
		cart2.setProductDto(product2);

		given(memberMapper.getMemberById(memberId)).willReturn(member);
		given(cartMapper.showMyCart(memberId)).willReturn(Arrays.asList(cart1, cart2));
		given(orderMapper.createOrder(any(OrderDto.class))).willReturn(1);
		given(productMapper.decreaseStock(1, 2)).willReturn(1);
		given(productMapper.decreaseStock(2, 3)).willReturn(1);
		given(orderMapper.addOrderItem(any(OrderItemDto.class))).willReturn(1);
		given(cartMapper.removeFromCart(anyInt())).willReturn(1);

		// when
		String orderId = orderService.createOrder(
				memberId, cartIds, deliveryName, deliveryPhone,
				deliveryPostCode, deliveryAddress, deliveryDetailAddress,
				deliveryMessage, paymentMethod
		);

		// then
		assertThat(orderId).isNotNull();
		assertThat(orderId).startsWith("ORD-" + memberId);
		then(memberMapper).should(times(1)).getMemberById(memberId);
		then(orderMapper).should(times(1)).createOrder(any(OrderDto.class));
		then(productMapper).should(times(1)).decreaseStock(1, 2);
		then(productMapper).should(times(1)).decreaseStock(2, 3);
		then(orderMapper).should(times(2)).addOrderItem(any(OrderItemDto.class));
		then(cartMapper).should(times(2)).removeFromCart(anyInt());
	}

	@Test
	@DisplayName("일반 주문 생성 - 회원을 찾을 수 없을 때 예외")
	void createOrder_MemberNotFound() {
		// given
		String memberId = "nonExistentUser";
		List<Integer> cartIds = Arrays.asList(1);

		given(memberMapper.getMemberById(memberId)).willReturn(null);

		// when & then
		assertThatThrownBy(() -> orderService.createOrder(
				memberId, cartIds, "홍길동", "010-1234-5678",
				"12345", "서울시", "101호", "메시지", "CARD"
		))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> {
					BusinessException be = (BusinessException) e;
					assertThat(be.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
				});
	}

	@Test
	@DisplayName("일반 주문 생성 - 장바구니를 찾을 수 없을 때 예외")
	void createOrder_CartNotFound() {
		// given
		String memberId = "testUser";
		List<Integer> cartIds = Arrays.asList(999); // 존재하지 않는 장바구니 ID

		MemberDto member = createMember(memberId);
		given(memberMapper.getMemberById(memberId)).willReturn(member);
		given(cartMapper.showMyCart(memberId)).willReturn(Collections.emptyList());

		// when & then
		assertThatThrownBy(() -> orderService.createOrder(
				memberId, cartIds, "홍길동", "010-1234-5678",
				"12345", "서울시", "101호", "메시지", "CARD"
		))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("장바구니를 찾을 수 없습니다");
	}

	@Test
	@DisplayName("일반 주문 생성 - 상품 정보를 찾을 수 없을 때 예외")
	void createOrder_ProductNotFound() {
		// given
		String memberId = "testUser";
		List<Integer> cartIds = Arrays.asList(1);

		MemberDto member = createMember(memberId);
		CartDto cart = createCart(1, memberId, 1, 2, false);
		cart.setProductDto(null); // 상품 정보 없음

		given(memberMapper.getMemberById(memberId)).willReturn(member);
		given(cartMapper.showMyCart(memberId)).willReturn(Arrays.asList(cart));

		// when & then
		assertThatThrownBy(() -> orderService.createOrder(
				memberId, cartIds, "홍길동", "010-1234-5678",
				"12345", "서울시", "101호", "메시지", "CARD"
		))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> {
					BusinessException be = (BusinessException) e;
					assertThat(be.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
				});
	}

	@Test
	@DisplayName("일반 주문 생성 - 재고가 부족할 때 예외")
	void createOrder_InsufficientStock() {
		// given
		String memberId = "testUser";
		List<Integer> cartIds = Arrays.asList(1);

		MemberDto member = createMember(memberId);
		ProductDto product = createProduct(1, 1, 10000); // 재고 1개
		CartDto cart = createCart(1, memberId, 1, 5, false); // 주문 수량 5개
		cart.setProductDto(product);

		given(memberMapper.getMemberById(memberId)).willReturn(member);
		given(cartMapper.showMyCart(memberId)).willReturn(Arrays.asList(cart));

		// when & then
		assertThatThrownBy(() -> orderService.createOrder(
				memberId, cartIds, "홍길동", "010-1234-5678",
				"12345", "서울시", "101호", "메시지", "CARD"
		))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("재고 부족");
	}

	@Test
	@DisplayName("일반 주문 생성 - 재고 차감 실패 시 예외")
	void createOrder_StockDecreaseFails() {
		// given
		String memberId = "testUser";
		List<Integer> cartIds = Arrays.asList(1);

		MemberDto member = createMember(memberId);
		ProductDto product = createProduct(1, 10, 10000);
		CartDto cart = createCart(1, memberId, 1, 2, false);
		cart.setProductDto(product);

		given(memberMapper.getMemberById(memberId)).willReturn(member);
		given(cartMapper.showMyCart(memberId)).willReturn(Arrays.asList(cart));
		given(orderMapper.createOrder(any(OrderDto.class))).willReturn(1);
		given(productMapper.decreaseStock(1, 2)).willReturn(0); // 재고 차감 실패

		// when & then
		assertThatThrownBy(() -> orderService.createOrder(
				memberId, cartIds, "홍길동", "010-1234-5678",
				"12345", "서울시", "101호", "메시지", "CARD"
		))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("재고가 부족합니다");
	}

	@Test
	@DisplayName("일반 주문 생성 - 주문 생성 실패 시 예외")
	void createOrder_OrderCreationFails() {
		// given
		String memberId = "testUser";
		List<Integer> cartIds = Arrays.asList(1);

		MemberDto member = createMember(memberId);
		ProductDto product = createProduct(1, 10, 10000);
		CartDto cart = createCart(1, memberId, 1, 2, false);
		cart.setProductDto(product);

		given(memberMapper.getMemberById(memberId)).willReturn(member);
		given(cartMapper.showMyCart(memberId)).willReturn(Arrays.asList(cart));
		given(orderMapper.createOrder(any(OrderDto.class))).willReturn(0); // 주문 생성 실패

		// when & then
		assertThatThrownBy(() -> orderService.createOrder(
				memberId, cartIds, "홍길동", "010-1234-5678",
				"12345", "서울시", "101호", "메시지", "CARD"
		))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("주문 생성 실패");
	}

	// ========== createGiftOrder 테스트 ==========

	@Test
	@DisplayName("선물 주문 생성 - 정상")
	void createGiftOrder_Success() {
		// given
		String senderId = "sender";
		String recipientId = "recipient";
		int cartId = 1;
		String giftMessage = "생일 축하해!";
		String paymentMethod = "CARD";

		MemberDto sender = createMember(senderId);
		MemberDto recipient = createMember(recipientId);
		ProductDto product = createProduct(1, 10, 50000);
		CartDto cart = createCart(cartId, senderId, 1, 1, true);
		cart.setProductDto(product);
		cart.setGiftToMemberId(recipientId);

		given(memberMapper.getMemberById(senderId)).willReturn(sender);
		given(cartMapper.showMyCart(senderId)).willReturn(Arrays.asList(cart));
		given(memberMapper.getMemberById(recipientId)).willReturn(recipient);
		given(productMapper.decreaseStock(1, 1)).willReturn(1);
		given(orderMapper.createOrder(any(OrderDto.class))).willReturn(1);
		given(orderMapper.addOrderItem(any(OrderItemDto.class))).willReturn(1);
		given(giftDeliveryRequestMapper.createGiftDeliveryRequest(any(GiftDeliveryRequestDto.class))).willReturn(1);
		willDoNothing().given(alarmService).createGiftNotification(anyString(), eq(senderId), eq(recipientId));
		given(cartMapper.removeFromCart(cartId)).willReturn(1);

		// when
		String orderId = orderService.createGiftOrder(senderId, cartId, giftMessage, paymentMethod);

		// then
		assertThat(orderId).isNotNull();
		assertThat(orderId).startsWith("ORD-" + senderId);
		then(memberMapper).should(times(1)).getMemberById(senderId);
		then(memberMapper).should(times(1)).getMemberById(recipientId);
		then(productMapper).should(times(1)).decreaseStock(1, 1);
		then(orderMapper).should(times(1)).createOrder(any(OrderDto.class));
		then(giftDeliveryRequestMapper).should(times(1)).createGiftDeliveryRequest(any(GiftDeliveryRequestDto.class));
		then(alarmService).should(times(1)).createGiftNotification(anyString(), eq(senderId), eq(recipientId));
		then(cartMapper).should(times(1)).removeFromCart(cartId);
	}

	@Test
	@DisplayName("선물 주문 생성 - 보낸 사람을 찾을 수 없을 때 예외")
	void createGiftOrder_SenderNotFound() {
		// given
		String senderId = "nonExistentSender";
		int cartId = 1;

		given(memberMapper.getMemberById(senderId)).willReturn(null);

		// when & then
		assertThatThrownBy(() -> orderService.createGiftOrder(senderId, cartId, "메시지", "CARD"))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> {
					BusinessException be = (BusinessException) e;
					assertThat(be.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
				});
	}

	@Test
	@DisplayName("선물 주문 생성 - 장바구니를 찾을 수 없을 때 예외")
	void createGiftOrder_CartNotFound() {
		// given
		String senderId = "sender";
		int cartId = 999;

		MemberDto sender = createMember(senderId);
		given(memberMapper.getMemberById(senderId)).willReturn(sender);
		given(cartMapper.showMyCart(senderId)).willReturn(Collections.emptyList());

		// when & then
		assertThatThrownBy(() -> orderService.createGiftOrder(senderId, cartId, "메시지", "CARD"))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("장바구니를 찾을 수 없습니다");
	}

	@Test
	@DisplayName("선물 주문 생성 - 선물 상품이 아닐 때 예외")
	void createGiftOrder_NotGiftItem() {
		// given
		String senderId = "sender";
		int cartId = 1;

		MemberDto sender = createMember(senderId);
		ProductDto product = createProduct(1, 10, 50000);
		CartDto cart = createCart(cartId, senderId, 1, 1, false); // 선물이 아님
		cart.setProductDto(product);

		given(memberMapper.getMemberById(senderId)).willReturn(sender);
		given(cartMapper.showMyCart(senderId)).willReturn(Arrays.asList(cart));

		// when & then
		assertThatThrownBy(() -> orderService.createGiftOrder(senderId, cartId, "메시지", "CARD"))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("선물 상품이 아닙니다");
	}

	@Test
	@DisplayName("선물 주문 생성 - 선물 받는 사람이 지정되지 않았을 때 예외")
	void createGiftOrder_RecipientNotSpecified() {
		// given
		String senderId = "sender";
		int cartId = 1;

		MemberDto sender = createMember(senderId);
		ProductDto product = createProduct(1, 10, 50000);
		CartDto cart = createCart(cartId, senderId, 1, 1, true);
		cart.setProductDto(product);
		cart.setGiftToMemberId(null); // 받는 사람 미지정

		given(memberMapper.getMemberById(senderId)).willReturn(sender);
		given(cartMapper.showMyCart(senderId)).willReturn(Arrays.asList(cart));

		// when & then
		assertThatThrownBy(() -> orderService.createGiftOrder(senderId, cartId, "메시지", "CARD"))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("선물 받는 사람이 지정되지 않았습니다");
	}

	@Test
	@DisplayName("선물 주문 생성 - 받는 사람을 찾을 수 없을 때 예외")
	void createGiftOrder_RecipientNotFound() {
		// given
		String senderId = "sender";
		String recipientId = "nonExistentRecipient";
		int cartId = 1;

		MemberDto sender = createMember(senderId);
		ProductDto product = createProduct(1, 10, 50000);
		CartDto cart = createCart(cartId, senderId, 1, 1, true);
		cart.setProductDto(product);
		cart.setGiftToMemberId(recipientId);

		given(memberMapper.getMemberById(senderId)).willReturn(sender);
		given(cartMapper.showMyCart(senderId)).willReturn(Arrays.asList(cart));
		given(memberMapper.getMemberById(recipientId)).willReturn(null);

		// when & then
		assertThatThrownBy(() -> orderService.createGiftOrder(senderId, cartId, "메시지", "CARD"))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("선물 받는 사람을 찾을 수 없습니다");
	}

	@Test
	@DisplayName("선물 주문 생성 - 재고가 부족할 때 예외")
	void createGiftOrder_InsufficientStock() {
		// given
		String senderId = "sender";
		String recipientId = "recipient";
		int cartId = 1;

		MemberDto sender = createMember(senderId);
		MemberDto recipient = createMember(recipientId);
		ProductDto product = createProduct(1, 10, 50000);
		CartDto cart = createCart(cartId, senderId, 1, 1, true);
		cart.setProductDto(product);
		cart.setGiftToMemberId(recipientId);

		given(memberMapper.getMemberById(senderId)).willReturn(sender);
		given(cartMapper.showMyCart(senderId)).willReturn(Arrays.asList(cart));
		given(memberMapper.getMemberById(recipientId)).willReturn(recipient);
		given(productMapper.decreaseStock(1, 1)).willReturn(0); // 재고 차감 실패

		// when & then
		assertThatThrownBy(() -> orderService.createGiftOrder(senderId, cartId, "메시지", "CARD"))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("재고가 부족합니다");
	}

	@Test
	@DisplayName("선물 주문 생성 - 주문 생성 실패 시 예외")
	void createGiftOrder_OrderCreationFails() {
		// given
		String senderId = "sender";
		String recipientId = "recipient";
		int cartId = 1;

		MemberDto sender = createMember(senderId);
		MemberDto recipient = createMember(recipientId);
		ProductDto product = createProduct(1, 10, 50000);
		CartDto cart = createCart(cartId, senderId, 1, 1, true);
		cart.setProductDto(product);
		cart.setGiftToMemberId(recipientId);

		given(memberMapper.getMemberById(senderId)).willReturn(sender);
		given(cartMapper.showMyCart(senderId)).willReturn(Arrays.asList(cart));
		given(memberMapper.getMemberById(recipientId)).willReturn(recipient);
		given(productMapper.decreaseStock(1, 1)).willReturn(1);
		given(orderMapper.createOrder(any(OrderDto.class))).willReturn(0); // 주문 생성 실패

		// when & then
		assertThatThrownBy(() -> orderService.createGiftOrder(senderId, cartId, "메시지", "CARD"))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("주문 생성 실패");
	}

	// ========== updateDeliveryAddress 테스트 ==========

	@Test
	@DisplayName("배송지 정보 업데이트 - 정상")
	void updateDeliveryAddress_Success() {
		// given
		String recipientId = "recipient";
		String orderId = "ORD-001";
		String deliveryName = "홍길동";
		String deliveryPhone = "010-1234-5678";
		String deliveryPostCode = "12345";
		String deliveryAddress = "서울시 강남구";
		String deliveryDetailAddress = "101호";
		String deliveryMessage = "문 앞에 놓아주세요";

		OrderDto giftOrder = createGiftOrder(orderId, recipientId, "sender", "PENDING");
		GiftDeliveryRequestDto giftRequest = new GiftDeliveryRequestDto();
		giftRequest.setRequestId(1);
		giftRequest.setOrderId(orderId);
		giftRequest.setRequestStatus(GiftDeliveryRequestDto.RequestStatus.PENDING.getCode());

		given(orderMapper.findOrderById(orderId)).willReturn(giftOrder);
		given(orderMapper.findOrderItemsByOrderId(orderId)).willReturn(Collections.emptyList());
		given(orderMapper.updateDeliveryInfo(any(OrderDto.class))).willReturn(1);
		given(giftDeliveryRequestMapper.findRequestByOrderId(orderId)).willReturn(giftRequest);
		given(giftDeliveryRequestMapper.updateRequestStatus(1, GiftDeliveryRequestDto.RequestStatus.COMPLETED.getCode())).willReturn(1);
		given(orderMapper.updateOrderStatus(orderId, OrderDto.OrderStatus.PAID.getCode())).willReturn(1);

		// when
		orderService.updateDeliveryAddress(
				recipientId, orderId, deliveryName, deliveryPhone,
				deliveryPostCode, deliveryAddress, deliveryDetailAddress, deliveryMessage
		);

		// then
		then(orderMapper).should(times(1)).updateDeliveryInfo(any(OrderDto.class));
		then(giftDeliveryRequestMapper).should(times(1)).updateRequestStatus(1, GiftDeliveryRequestDto.RequestStatus.COMPLETED.getCode());
		then(orderMapper).should(times(1)).updateOrderStatus(orderId, OrderDto.OrderStatus.PAID.getCode());
	}

	@Test
	@DisplayName("배송지 정보 업데이트 - 주문을 찾을 수 없을 때 예외")
	void updateDeliveryAddress_OrderNotFound() {
		// given
		String recipientId = "recipient";
		String orderId = "ORD-999";

		given(orderMapper.findOrderById(orderId)).willReturn(null);

		// when & then
		assertThatThrownBy(() -> orderService.updateDeliveryAddress(
				recipientId, orderId, "홍길동", "010-1234-5678",
				"12345", "서울시", "101호", "메시지"
		))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> {
					BusinessException be = (BusinessException) e;
					assertThat(be.getErrorCode()).isEqualTo(ErrorCode.ORDER_NOT_FOUND);
				});
	}

	@Test
	@DisplayName("배송지 정보 업데이트 - 권한이 없을 때 예외")
	void updateDeliveryAddress_Unauthorized() {
		// given
		String recipientId = "recipient";
		String wrongMemberId = "wrongUser";
		String orderId = "ORD-001";

		OrderDto giftOrder = createGiftOrder(orderId, recipientId, "sender", "PENDING");

		given(orderMapper.findOrderById(orderId)).willReturn(giftOrder);
		given(orderMapper.findOrderItemsByOrderId(orderId)).willReturn(Collections.emptyList());

		// when & then
		assertThatThrownBy(() -> orderService.updateDeliveryAddress(
				wrongMemberId, orderId, "홍길동", "010-1234-5678",
				"12345", "서울시", "101호", "메시지"
		))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> {
					BusinessException be = (BusinessException) e;
					assertThat(be.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN);
				});
	}

	@Test
	@DisplayName("배송지 정보 업데이트 - 선물 주문이 아닐 때 예외")
	void updateDeliveryAddress_NotGiftOrder() {
		// given
		String memberId = "testUser";
		String orderId = "ORD-001";

		OrderDto normalOrder = createOrder(orderId, memberId, "PENDING");

		given(orderMapper.findOrderById(orderId)).willReturn(normalOrder);
		given(orderMapper.findOrderItemsByOrderId(orderId)).willReturn(Collections.emptyList());

		// when & then
		assertThatThrownBy(() -> orderService.updateDeliveryAddress(
				memberId, orderId, "홍길동", "010-1234-5678",
				"12345", "서울시", "101호", "메시지"
		))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("선물 주문이 아닙니다");
	}

	@Test
	@DisplayName("배송지 정보 업데이트 - 업데이트 실패 시 예외")
	void updateDeliveryAddress_UpdateFails() {
		// given
		String recipientId = "recipient";
		String orderId = "ORD-001";

		OrderDto giftOrder = createGiftOrder(orderId, recipientId, "sender", "PENDING");

		given(orderMapper.findOrderById(orderId)).willReturn(giftOrder);
		given(orderMapper.findOrderItemsByOrderId(orderId)).willReturn(Collections.emptyList());
		given(orderMapper.updateDeliveryInfo(any(OrderDto.class))).willReturn(0); // 업데이트 실패

		// when & then
		assertThatThrownBy(() -> orderService.updateDeliveryAddress(
				recipientId, orderId, "홍길동", "010-1234-5678",
				"12345", "서울시", "101호", "메시지"
		))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("배송지 정보 업데이트 실패");
	}

	// ========== cancelOrder 테스트 ==========

	@Test
	@DisplayName("주문 취소 - 정상 (일반 주문)")
	void cancelOrder_Success_NormalOrder() {
		// given
		String memberId = "testUser";
		String orderId = "ORD-001";

		OrderDto order = createOrder(orderId, memberId, "PENDING");
		OrderItemDto orderItem = createOrderItem(orderId, 1, 2, 10000);
		order.setOrderItems(Arrays.asList(orderItem));

		given(orderMapper.findOrderById(orderId)).willReturn(order);
		given(orderMapper.findOrderItemsByOrderId(orderId)).willReturn(Arrays.asList(orderItem));
		given(productMapper.increaseStock(1, 2)).willReturn(1);
		given(orderMapper.updateOrderStatus(orderId, OrderDto.OrderStatus.CANCELLED.getCode())).willReturn(1);

		// when
		orderService.cancelOrder(memberId, orderId);

		// then
		then(productMapper).should(times(1)).increaseStock(1, 2);
		then(orderMapper).should(times(1)).updateOrderStatus(orderId, OrderDto.OrderStatus.CANCELLED.getCode());
	}

	@Test
	@DisplayName("주문 취소 - 정상 (선물 주문 - 보낸 사람)")
	void cancelOrder_Success_GiftOrder_Sender() {
		// given
		String senderId = "sender";
		String recipientId = "recipient";
		String orderId = "ORD-001";

		OrderDto giftOrder = createGiftOrder(orderId, recipientId, senderId, "PENDING");
		OrderItemDto orderItem = createOrderItem(orderId, 1, 1, 50000);
		giftOrder.setOrderItems(Arrays.asList(orderItem));

		given(orderMapper.findOrderById(orderId)).willReturn(giftOrder);
		given(orderMapper.findOrderItemsByOrderId(orderId)).willReturn(Arrays.asList(orderItem));
		given(productMapper.increaseStock(1, 1)).willReturn(1);
		given(orderMapper.updateOrderStatus(orderId, OrderDto.OrderStatus.CANCELLED.getCode())).willReturn(1);

		// when
		orderService.cancelOrder(senderId, orderId);

		// then
		then(productMapper).should(times(1)).increaseStock(1, 1);
		then(orderMapper).should(times(1)).updateOrderStatus(orderId, OrderDto.OrderStatus.CANCELLED.getCode());
	}

	@Test
	@DisplayName("주문 취소 - 정상 (선물 주문 - 받는 사람)")
	void cancelOrder_Success_GiftOrder_Recipient() {
		// given
		String senderId = "sender";
		String recipientId = "recipient";
		String orderId = "ORD-001";

		OrderDto giftOrder = createGiftOrder(orderId, recipientId, senderId, "PAID");
		OrderItemDto orderItem = createOrderItem(orderId, 1, 1, 50000);
		giftOrder.setOrderItems(Arrays.asList(orderItem));

		given(orderMapper.findOrderById(orderId)).willReturn(giftOrder);
		given(orderMapper.findOrderItemsByOrderId(orderId)).willReturn(Arrays.asList(orderItem));
		given(productMapper.increaseStock(1, 1)).willReturn(1);
		given(orderMapper.updateOrderStatus(orderId, OrderDto.OrderStatus.CANCELLED.getCode())).willReturn(1);

		// when
		orderService.cancelOrder(recipientId, orderId);

		// then
		then(productMapper).should(times(1)).increaseStock(1, 1);
		then(orderMapper).should(times(1)).updateOrderStatus(orderId, OrderDto.OrderStatus.CANCELLED.getCode());
	}

	@Test
	@DisplayName("주문 취소 - 주문을 찾을 수 없을 때 예외")
	void cancelOrder_OrderNotFound() {
		// given
		String memberId = "testUser";
		String orderId = "ORD-999";

		given(orderMapper.findOrderById(orderId)).willReturn(null);

		// when & then
		assertThatThrownBy(() -> orderService.cancelOrder(memberId, orderId))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> {
					BusinessException be = (BusinessException) e;
					assertThat(be.getErrorCode()).isEqualTo(ErrorCode.ORDER_NOT_FOUND);
				});
	}

	@Test
	@DisplayName("주문 취소 - 권한이 없을 때 예외")
	void cancelOrder_Unauthorized() {
		// given
		String memberId = "testUser";
		String wrongMemberId = "wrongUser";
		String orderId = "ORD-001";

		OrderDto order = createOrder(orderId, memberId, "PENDING");

		given(orderMapper.findOrderById(orderId)).willReturn(order);
		given(orderMapper.findOrderItemsByOrderId(orderId)).willReturn(Collections.emptyList());

		// when & then
		assertThatThrownBy(() -> orderService.cancelOrder(wrongMemberId, orderId))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> {
					BusinessException be = (BusinessException) e;
					assertThat(be.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN);
				});
	}

	@Test
	@DisplayName("주문 취소 - 취소 불가능한 상태일 때 예외")
	void cancelOrder_InvalidStatus() {
		// given
		String memberId = "testUser";
		String orderId = "ORD-001";

		OrderDto order = createOrder(orderId, memberId, "SHIPPED"); // 이미 배송 중

		given(orderMapper.findOrderById(orderId)).willReturn(order);
		given(orderMapper.findOrderItemsByOrderId(orderId)).willReturn(Collections.emptyList());

		// when & then
		assertThatThrownBy(() -> orderService.cancelOrder(memberId, orderId))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("취소할 수 없는 주문 상태입니다");
	}

	@Test
	@DisplayName("주문 취소 - 재고 복원 실패 시 예외")
	void cancelOrder_StockRestoreFails() {
		// given
		String memberId = "testUser";
		String orderId = "ORD-001";

		OrderDto order = createOrder(orderId, memberId, "PENDING");
		OrderItemDto orderItem = createOrderItem(orderId, 1, 2, 10000);
		order.setOrderItems(Arrays.asList(orderItem));

		given(orderMapper.findOrderById(orderId)).willReturn(order);
		given(orderMapper.findOrderItemsByOrderId(orderId)).willReturn(Arrays.asList(orderItem));
		given(productMapper.increaseStock(1, 2)).willReturn(0); // 재고 복원 실패

		// when & then
		assertThatThrownBy(() -> orderService.cancelOrder(memberId, orderId))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("재고 복원에 실패했습니다");
	}

	// ========== getOrderWithItems 테스트 ==========

	@Test
	@DisplayName("주문 조회 - 정상")
	void getOrderWithItems_Success() {
		// given
		String orderId = "ORD-001";
		OrderDto order = createOrder(orderId, "testUser", "PAID");
		List<OrderItemDto> orderItems = Arrays.asList(
				createOrderItem(orderId, 1, 2, 10000),
				createOrderItem(orderId, 2, 1, 20000)
		);

		given(orderMapper.findOrderById(orderId)).willReturn(order);
		given(orderMapper.findOrderItemsByOrderId(orderId)).willReturn(orderItems);

		// when
		OrderDto result = orderService.getOrderWithItems(orderId);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getOrderId()).isEqualTo(orderId);
		assertThat(result.getOrderItems()).hasSize(2);
		then(orderMapper).should(times(1)).findOrderById(orderId);
		then(orderMapper).should(times(1)).findOrderItemsByOrderId(orderId);
	}

	@Test
	@DisplayName("주문 조회 - 주문을 찾을 수 없을 때 예외")
	void getOrderWithItems_OrderNotFound() {
		// given
		String orderId = "ORD-999";

		given(orderMapper.findOrderById(orderId)).willReturn(null);

		// when & then
		assertThatThrownBy(() -> orderService.getOrderWithItems(orderId))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> {
					BusinessException be = (BusinessException) e;
					assertThat(be.getErrorCode()).isEqualTo(ErrorCode.ORDER_NOT_FOUND);
				});
	}

	// ========== getMemberOrders 테스트 ==========

	@Test
	@DisplayName("회원 주문 목록 조회 - 정상")
	void getMemberOrders_Success() {
		// given
		String memberId = "testUser";
		List<OrderDto> expectedOrders = Arrays.asList(
				createOrder("ORD-001", memberId, "PAID"),
				createOrder("ORD-002", memberId, "SHIPPED")
		);

		given(orderMapper.findOrdersByMemberId(memberId)).willReturn(expectedOrders);

		// when
		List<OrderDto> result = orderService.getMemberOrders(memberId);

		// then
		assertThat(result).hasSize(2);
		assertThat(result).isEqualTo(expectedOrders);
		then(orderMapper).should(times(1)).findOrdersByMemberId(memberId);
	}

	@Test
	@DisplayName("회원 주문 목록 조회 - 빈 목록")
	void getMemberOrders_EmptyList() {
		// given
		String memberId = "testUser";

		given(orderMapper.findOrdersByMemberId(memberId)).willReturn(Collections.emptyList());

		// when
		List<OrderDto> result = orderService.getMemberOrders(memberId);

		// then
		assertThat(result).isEmpty();
		then(orderMapper).should(times(1)).findOrdersByMemberId(memberId);
	}

	// ========== getGiftOrdersReceived 테스트 ==========

	@Test
	@DisplayName("선물 받은 주문 목록 조회 - 정상")
	void getGiftOrdersReceived_Success() {
		// given
		String recipientId = "recipient";
		List<OrderDto> expectedOrders = Arrays.asList(
				createGiftOrder("ORD-001", recipientId, "sender1", "PENDING"),
				createGiftOrder("ORD-002", recipientId, "sender2", "PAID")
		);

		given(orderMapper.findGiftOrdersReceivedByMemberId(recipientId)).willReturn(expectedOrders);

		// when
		List<OrderDto> result = orderService.getGiftOrdersReceived(recipientId);

		// then
		assertThat(result).hasSize(2);
		assertThat(result).isEqualTo(expectedOrders);
		then(orderMapper).should(times(1)).findGiftOrdersReceivedByMemberId(recipientId);
	}

	@Test
	@DisplayName("선물 받은 주문 목록 조회 - 빈 목록")
	void getGiftOrdersReceived_EmptyList() {
		// given
		String recipientId = "recipient";

		given(orderMapper.findGiftOrdersReceivedByMemberId(recipientId)).willReturn(Collections.emptyList());

		// when
		List<OrderDto> result = orderService.getGiftOrdersReceived(recipientId);

		// then
		assertThat(result).isEmpty();
		then(orderMapper).should(times(1)).findGiftOrdersReceivedByMemberId(recipientId);
	}

	// ========== Helper Methods ==========

	/**
	 * 테스트용 회원 생성
	 */
	private MemberDto createMember(String memberId) {
		MemberDto member = new MemberDto();
		member.setMemberId(memberId);
		member.setMemberName("Test User " + memberId);
		member.setMemberEmail(memberId + "@test.com");
		member.setMemberPhone("010-1234-5678");
		return member;
	}

	/**
	 * 테스트용 상품 생성
	 */
	private ProductDto createProduct(int prodNum, int stock, int price) {
		ProductDto product = new ProductDto();
		product.setProdNum(prodNum);
		product.setProdName("Test Product " + prodNum);
		product.setProdStock(stock);
		product.setProdPrice(price);
		product.setProdDeliveryfee(3000);
		return product;
	}

	/**
	 * 테스트용 장바구니 생성
	 */
	private CartDto createCart(int cartId, String memberId, int prodNum, int quantity, boolean isGift) {
		CartDto cart = new CartDto();
		cart.setCartId(cartId);
		cart.setMemberId(memberId);
		cart.setProdNum(prodNum);
		cart.setCartQuantity(quantity);
		if (isGift) {
			cart.setIsGift("Y");
		} else {
			cart.setIsGift("N");
		}
		return cart;
	}

	/**
	 * 테스트용 일반 주문 생성
	 */
	private OrderDto createOrder(String orderId, String memberId, String status) {
		OrderDto order = new OrderDto();
		order.setOrderId(orderId);
		order.setMemberId(memberId);
		order.setOrderStatus(status);
		order.setTotalAmount(50000);
		order.setDeliveryFee(3000);
		order.setPaymentMethod("CARD");
		order.setAsNormalOrder();
		return order;
	}

	/**
	 * 테스트용 선물 주문 생성
	 */
	private OrderDto createGiftOrder(String orderId, String recipientId, String senderId, String status) {
		OrderDto order = new OrderDto();
		order.setOrderId(orderId);
		order.setMemberId(recipientId);
		order.setOrderStatus(status);
		order.setTotalAmount(50000);
		order.setDeliveryFee(3000);
		order.setPaymentMethod("CARD");
		order.setAsGiftOrder(senderId, "축하합니다!");
		return order;
	}

	/**
	 * 테스트용 주문 상품 생성
	 */
	private OrderItemDto createOrderItem(String orderId, int prodNum, int quantity, int price) {
		OrderItemDto orderItem = new OrderItemDto();
		orderItem.setOrderId(orderId);
		orderItem.setProdNum(prodNum);
		orderItem.setProdName("Test Product " + prodNum);
		orderItem.setProdPrice(price);
		orderItem.setQuantity(quantity);
		return orderItem;
	}
}
