package kitchenpos.application.dto.response;

import kitchenpos.domain.order.OrderLineItem;

public class OrderLineItemResponse {

    private final Long seq;
    private final Long menuId;
    private final long quantity;

    public OrderLineItemResponse(final OrderLineItem orderLineItem) {
        this(
            orderLineItem.getSeq(),
            orderLineItem.getOrderMenuId(),
            orderLineItem.getQuantity()
        );
    }

    public OrderLineItemResponse(final Long seq, final Long menuId, final long quantity) {
        this.seq = seq;
        this.menuId = menuId;
        this.quantity = quantity;
    }

    public Long getSeq() {
        return seq;
    }

    public Long getMenuId() {
        return menuId;
    }

    public long getQuantity() {
        return quantity;
    }
}
