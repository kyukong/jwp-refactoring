package kitchenpos.dao.fake;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import kitchenpos.dao.MenuDao;
import kitchenpos.domain.menu.Menu;

public class FakeMenuDao implements MenuDao {

    private long id = 0L;
    private final Map<Long, Menu> menus = new HashMap<>();

    @Override
    public Menu save(final Menu entity) {
        final Menu savedMenu = new Menu(++id, entity.getName(), entity.getPrice(), entity.getMenuGroupId());
        menus.put(savedMenu.getId(), savedMenu);
        return savedMenu;
    }

    @Override
    public Optional<Menu> findById(final Long id) {
        return Optional.ofNullable(menus.get(id));
    }

    @Override
    public List<Menu> findAll() {
        return List.copyOf(menus.values());
    }

    @Override
    public long countByIdIn(final List<Long> ids) {
        int count = 0;
        for (long savedId : ids) {
            if (menus.containsKey(savedId)) {
                count++;
            }
        }
        return count;
    }
}
