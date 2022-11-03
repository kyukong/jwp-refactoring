package kitchenpos.application;

import static kitchenpos.domain.fixture.OrderFixture.완료된_주문;
import static kitchenpos.domain.fixture.OrderFixture.요리중인_주문;
import static kitchenpos.domain.fixture.OrderTableFixture.비어있는_테이블;
import static kitchenpos.domain.fixture.OrderTableFixture.비어있지_않는_테이블;
import static kitchenpos.domain.fixture.OrderTableFixture.새로운_테이블;
import static kitchenpos.domain.fixture.TableGroupFixture.새로운_테이블_그룹;
import static kitchenpos.domain.fixture.TableGroupFixture.테이블_그룹의_주문_테이블들은;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import kitchenpos.application.dto.request.OrderTableChangeRequest;
import kitchenpos.application.dto.request.TableGroupRequest;
import kitchenpos.application.dto.response.TableGroupResponse;
import kitchenpos.domain.table.OrderTable;
import kitchenpos.domain.table.TableGroup;
import kitchenpos.repository.OrderRepository;
import kitchenpos.repository.OrderTableRepository;
import kitchenpos.repository.TableGroupRepository;

@SuppressWarnings("NonAsciiCharacters")
@SpringBootTest
@Transactional
@DisplayName("TableGroup 서비스 테스트")
class TableGroupServiceTest {

    @Autowired
    private TableGroupService tableGroupService;

    @Autowired
    private TableGroupRepository tableGroupRepository;

    @Autowired
    private OrderTableRepository orderTableRepository;

    @Autowired
    private OrderRepository orderRepository;

    @DisplayName("테이블 그룹을 등록한다")
    @Test
    void create() {
        final OrderTable saved1 = orderTableRepository.save(비어있는_테이블());
        final OrderTable saved2 = orderTableRepository.save(비어있는_테이블());

        final OrderTableChangeRequest orderTableChangeRequest1 = new OrderTableChangeRequest(
            saved1.getId(), saved1.getNumberOfGuests(), saved1.isEmpty()
        );
        final OrderTableChangeRequest orderTableChangeRequest2 = new OrderTableChangeRequest(
            saved2.getId(), saved2.getNumberOfGuests(), saved2.isEmpty()
        );

        final TableGroupRequest request = new TableGroupRequest(List.of(orderTableChangeRequest1,
            orderTableChangeRequest2));

        final TableGroupResponse response = tableGroupService.create(request);

        assertThat(response.getId()).isNotNull();
    }

    @DisplayName("테이블 그룹 등록 시 등록하려는 테이블 그룹이 존재해야 한다")
    @Test
    void createOrderTableIsNotExist() {
        final OrderTable notSaved1 = 새로운_테이블();
        final OrderTable notSaved2 = 새로운_테이블();
        final OrderTable notSaved3 = 새로운_테이블();

        final OrderTableChangeRequest notOrderTableChangeRequest1 = new OrderTableChangeRequest(
            notSaved1.getId(), notSaved1.getNumberOfGuests(), notSaved1.isEmpty()
        );
        final OrderTableChangeRequest notOrderTableChangeRequest2 = new OrderTableChangeRequest(
            notSaved2.getId(), notSaved2.getNumberOfGuests(), notSaved2.isEmpty()
        );
        final OrderTableChangeRequest notOrderTableChangeRequest3 = new OrderTableChangeRequest(
            notSaved3.getId(), notSaved3.getNumberOfGuests(), notSaved3.isEmpty()
        );

        final TableGroupRequest request = new TableGroupRequest(
            List.of(notOrderTableChangeRequest1, notOrderTableChangeRequest2, notOrderTableChangeRequest3)
        );

        assertThatThrownBy(() -> tableGroupService.create(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("존재하지 않는 테이블 정보가 포함되어 있습니다.");
    }

    @DisplayName("테이블 그룹 등록 시 주문 테이블이 비워져 있어야 한다")
    @Test
    void createOrderTableIsNotEmpty() {
        final OrderTable saved1 = orderTableRepository.save(비어있지_않는_테이블());
        final OrderTable saved2 = orderTableRepository.save(새로운_테이블());

        final OrderTableChangeRequest orderTableChangeRequest1 = new OrderTableChangeRequest(
            saved1.getId(), saved1.getNumberOfGuests(), saved1.isEmpty()
        );
        final OrderTableChangeRequest orderTableChangeRequest2 = new OrderTableChangeRequest(
            saved2.getId(), saved2.getNumberOfGuests(), saved2.isEmpty()
        );

        final TableGroupRequest request = new TableGroupRequest(List.of(orderTableChangeRequest1,
            orderTableChangeRequest2));

        assertThatThrownBy(() -> tableGroupService.create(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("테이블이 비워져 있어야 합니다.");
    }

    @DisplayName("테이블 그룹 등록 시 테이블의 테이블 그룹 아이디가 null 이어야 한다")
    @Test
    void createOrderTableIsNotNull() {
        final OrderTable saved1 = orderTableRepository.save(새로운_테이블(1L));
        final OrderTable saved2 = orderTableRepository.save(새로운_테이블(1L));
        tableGroupRepository.save(테이블_그룹의_주문_테이블들은(List.of(saved1, saved2)));

        final OrderTableChangeRequest orderTableChangeRequest1 = new OrderTableChangeRequest(
            saved1.getId(), saved1.getNumberOfGuests(), saved1.isEmpty()
        );
        final OrderTableChangeRequest orderTableChangeRequest2 = new OrderTableChangeRequest(
            saved2.getId(), saved2.getNumberOfGuests(), saved2.isEmpty()
        );

        final TableGroupRequest request = new TableGroupRequest(List.of(orderTableChangeRequest1, orderTableChangeRequest2));

        assertThatThrownBy(() -> tableGroupService.create(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("테이블 그룹의 아이디가 존재합니다.");
    }

    @DisplayName("테이블의 그룹을 해제한다")
    @Test
    void ungroup() {
        final TableGroup savedTableGroup = tableGroupRepository.save(새로운_테이블_그룹());
        final OrderTable savedOrderTable = orderTableRepository.save(새로운_테이블(savedTableGroup.getId()));

        orderRepository.save(완료된_주문(savedOrderTable.getId()));

        assertThatCode(() -> tableGroupService.ungroup(savedTableGroup.getId()))
            .doesNotThrowAnyException();
    }

    @DisplayName("테이블의 그룹을 해제할 때 테이블의 주문 상태가 요리중이거나 식사중일 경우 테이블을 비울 수 없다")
    @Test
    void ungroupOrderStatusIsCompletion() {
        final TableGroup savedTableGroup = tableGroupRepository.save(새로운_테이블_그룹());
        final OrderTable savedOrderTable = orderTableRepository.save(새로운_테이블(savedTableGroup.getId()));

        orderRepository.save(요리중인_주문(savedOrderTable.getId()));

        assertThatThrownBy(() -> tableGroupService.ungroup(savedTableGroup.getId()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("테이블의 주문이 완료되지 않았습니다.");
    }
}
