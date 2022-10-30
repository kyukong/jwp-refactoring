package kitchenpos.dao.fake;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import kitchenpos.dao.TableGroupDao;
import kitchenpos.domain.table.TableGroup;

public class FakeTableGroupDao implements TableGroupDao {

    private long id = 0L;
    private final Map<Long, TableGroup> tableGroups = new HashMap<>();

    @Override
    public TableGroup save(TableGroup entity) {
        final TableGroup savedTableGroup = new TableGroup(++id, entity.getCreatedDate());
        tableGroups.put(savedTableGroup.getId(), savedTableGroup);
        return savedTableGroup;
    }

    @Override
    public Optional<TableGroup> findById(Long id) {
        return Optional.ofNullable(tableGroups.get(id));
    }

    @Override
    public List<TableGroup> findAll() {
        return List.copyOf(tableGroups.values());
    }
}
