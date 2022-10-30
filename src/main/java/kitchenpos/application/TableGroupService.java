package kitchenpos.application;

import kitchenpos.application.dto.request.OrderTableChangeRequest;
import kitchenpos.application.dto.request.TableGroupRequest;
import kitchenpos.application.dto.response.TableGroupResponse;
import kitchenpos.dao.OrderDao;
import kitchenpos.dao.OrderTableDao;
import kitchenpos.dao.TableGroupDao;
import kitchenpos.domain.order.OrderStatus;
import kitchenpos.domain.table.OrderTable;
import kitchenpos.domain.table.TableGroup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@Service
public class TableGroupService {
    private final OrderDao orderDao;
    private final OrderTableDao orderTableDao;
    private final TableGroupDao tableGroupDao;

    public TableGroupService(final OrderDao orderDao, final OrderTableDao orderTableDao, final TableGroupDao tableGroupDao) {
        this.orderDao = orderDao;
        this.orderTableDao = orderTableDao;
        this.tableGroupDao = tableGroupDao;
    }

    @Transactional
    public TableGroupResponse create(final TableGroupRequest request) {
        final TableGroup tableGroup = createTableGroup(request);
        final TableGroup savedTableGroup = saveTableGroup(tableGroup);
        return new TableGroupResponse(savedTableGroup);
    }

    @Transactional
    public void ungroup(final Long tableGroupId) {
        final List<OrderTable> orderTables = orderTableDao.findAllByTableGroupId(tableGroupId);
        final List<Long> orderTableIds = getOrderTableIds(orderTables);

        validateOrderStatusIsCompletion(orderTableIds);
        ungroupOrderTables(orderTables);
    }

    private TableGroup createTableGroup(final TableGroupRequest request) {
        return new TableGroup(createOrderTables(request.getOrderTables()));
    }

    private List<OrderTable> createOrderTables(final List<OrderTableChangeRequest> requests) {
        return requests.stream()
            .map(orderTable -> new OrderTable(
                orderTable.getId(),
                orderTable.getNumberOfGuests(),
                orderTable.isEmpty()
            ))
            .collect(Collectors.toUnmodifiableList());
    }

    private TableGroup saveTableGroup(final TableGroup tableGroup) {
        final List<OrderTable> orderTables = tableGroup.getOrderTables();
        final List<OrderTable> savedOrderTables = findAllOrderTablesByIdIn(orderTables);
        validateOrderTableIsEmptyAndNotGrouped(savedOrderTables);

        final TableGroup savedTableGroup = tableGroupDao.save(tableGroup);
        return new TableGroup(
            savedTableGroup.getId(),
            savedTableGroup.getCreatedDate(),
            saveOrderTables(savedOrderTables, savedTableGroup)
        );
    }

    private List<OrderTable> findAllOrderTablesByIdIn(final List<OrderTable> orderTables) {
        final List<Long> orderTableIds = getOrderTableIds(orderTables);
        final List<OrderTable> savedOrderTables = orderTableDao.findAllByIdIn(orderTableIds);
        if (orderTables.size() != savedOrderTables.size()) {
            throw new IllegalArgumentException("존재하지 않는 테이블 정보가 포함되어 있습니다.");
        }
        return savedOrderTables;
    }

    private List<Long> getOrderTableIds(final List<OrderTable> orderTables) {
        return orderTables.stream()
            .map(OrderTable::getId)
            .collect(Collectors.toList());
    }

    private void validateOrderTableIsEmptyAndNotGrouped(final List<OrderTable> savedOrderTables) {
        for (final OrderTable savedOrderTable : savedOrderTables) {
            if (savedOrderTable.isNotEmpty()) {
                throw new IllegalArgumentException("테이블이 비워져 있어야 합니다.");
            }
            if (Objects.nonNull(savedOrderTable.getTableGroupId())) {
                throw new IllegalArgumentException(String.format("테이블 그룹의 아이디가 존재합니다. [%s]", savedOrderTable.getTableGroupId()));
            }
        }
    }

    private List<OrderTable> saveOrderTables(final List<OrderTable> savedOrderTables, final TableGroup savedTableGroup) {
        return savedOrderTables.stream()
            .map(orderTable -> {
                orderTable.group(savedTableGroup.getId());
                return orderTableDao.save(orderTable);
            })
            .collect(Collectors.toUnmodifiableList());
    }

    private void validateOrderStatusIsCompletion(final List<Long> orderTableIds) {
        if (orderDao.existsByOrderTableIdInAndOrderStatusIn(
            orderTableIds, Arrays.asList(OrderStatus.COOKING.name(), OrderStatus.MEAL.name()))) {
            throw new IllegalArgumentException("테이블의 주문이 완료되지 않았습니다.");
        }
    }

    private void ungroupOrderTables(final List<OrderTable> orderTables) {
        for (final OrderTable orderTable : orderTables) {
            orderTable.group(null);
            orderTableDao.save(orderTable);
        }
    }
}
