package kitchenpos.application;

import kitchenpos.application.dto.convertor.OrderConvertor;
import kitchenpos.application.dto.request.OrderLineItemRequest;
import kitchenpos.application.dto.request.OrderRequest;
import kitchenpos.application.dto.request.OrderChangeRequest;
import kitchenpos.application.dto.response.OrderResponse;
import kitchenpos.dao.MenuDao;
import kitchenpos.dao.OrderTableDao;
import kitchenpos.domain.Order;
import kitchenpos.domain.OrderLineItem;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;
import kitchenpos.repository.OrderRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuDao menuDao;
    private final OrderTableDao orderTableDao;

    public OrderService(
            final OrderRepository orderRepository,
            final MenuDao menuDao,
            final OrderTableDao orderTableDao
    ) {
        this.orderRepository = orderRepository;
        this.menuDao = menuDao;
        this.orderTableDao = orderTableDao;
    }

    @Transactional
    public OrderResponse create(final OrderRequest request) {
        final OrderTable orderTable = orderTableDao.findById(request.getOrderTableId())
            .orElseThrow(() -> new IllegalArgumentException(String.format("존재하지 않는 테이블입니다. [%s]", request.getOrderTableId())));
        validateOrderTableIsNotEmpty(orderTable);

        final Order order = toOrder(request);
        validateOrderLineItemIsExist(order);

        final Order savedOrder = orderRepository.save(order);
        return OrderConvertor.convertToOrderResponse(savedOrder);
    }

    public List<OrderResponse> list() {
        final List<Order> orders = orderRepository.findAll();
        return OrderConvertor.convertToOrderResponse(orders);
    }

    @Transactional
    public OrderResponse changeOrderStatus(final Long orderId, final OrderChangeRequest request) {
        final Order order = OrderConvertor.convertToOrder(request);
        final Order changedOrder = changeStatus(orderId, order);
        return OrderConvertor.convertToOrderResponse(changedOrder);
    }

    private void validateOrderTableIsNotEmpty(final OrderTable orderTable) {
        if (orderTable.isEmpty()) {
            throw new IllegalArgumentException("테이블이 비어있으면 안됩니다.");
        }
    }

    public Order toOrder(final OrderRequest request) {
        return new Order(
            request.getOrderTableId(),
            OrderStatus.COOKING.name(),
            LocalDateTime.now(),
            toOrderLineItems(request.getOrderLineItems())
        );
    }

    public List<OrderLineItem> toOrderLineItems(final List<OrderLineItemRequest> orderLineItems) {
        return orderLineItems.stream()
            .map(request -> new OrderLineItem(request.getMenuId(), request.getQuantity()))
            .collect(Collectors.toUnmodifiableList());
    }

    private void validateOrderLineItemIsExist(final Order order) {
        final List<OrderLineItem> orderLineItems = order.getOrderLineItems();
        final List<Long> menuIds = orderLineItems.stream()
                .map(OrderLineItem::getMenuId)
                .collect(Collectors.toList());

        if (orderLineItems.size() != menuDao.countByIdIn(menuIds)) {
            throw new IllegalArgumentException("존재하지 않는 메뉴가 포함되어 있습니다.");
        }
    }

    private Order changeStatus(final Long orderId, final Order order) {
        final Order savedOrder = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException(String.format("존재하지 않는 주문입니다. [%s]", orderId)));

        savedOrder.changeStatus(order.getOrderStatus());
        return orderRepository.update(savedOrder);
    }
}
