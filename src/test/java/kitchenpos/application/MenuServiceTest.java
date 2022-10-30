package kitchenpos.application;

import static kitchenpos.domain.fixture.MenuFixture.후라이드_치킨_세트의_가격과_메뉴_상품_리스트는;
import static kitchenpos.domain.fixture.MenuGroupFixture.치킨_세트;
import static kitchenpos.domain.fixture.MenuProductFixture.가격_정보가_있는_상품_하나;
import static kitchenpos.domain.fixture.ProductFixture.후라이드_치킨;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import kitchenpos.application.dto.request.MenuProductRequest;
import kitchenpos.application.dto.request.MenuRequest;
import kitchenpos.application.dto.response.MenuResponse;
import kitchenpos.dao.MenuGroupDao;
import kitchenpos.dao.ProductDao;
import kitchenpos.dao.fake.FakeMenuDao;
import kitchenpos.dao.fake.FakeMenuGroupDao;
import kitchenpos.dao.fake.FakeMenuProductDao;
import kitchenpos.dao.fake.FakeProductDao;
import kitchenpos.domain.menu.Menu;
import kitchenpos.domain.menu.MenuGroup;
import kitchenpos.domain.menu.MenuProduct;
import kitchenpos.domain.Product;
import kitchenpos.repository.MenuRepository;

@SuppressWarnings("NonAsciiCharacters")
@DisplayName("Menu 서비스 테스트")
class MenuServiceTest {

    private static final String MENU_NAME = "후라이드 치킨 세트";
    private static final BigDecimal PRICE = BigDecimal.valueOf(15_000);
    private static final int MENU_PRODUCT_QUANTITY = 1;

    private MenuService menuService;
    private MenuRepository menuRepository;

    private MenuGroup 저장된_치킨_세트;
    private Product 저장된_후라이드_치킨;

    @BeforeEach
    void setUp() {
        final MenuGroupDao menuGroupDao = new FakeMenuGroupDao();
        final ProductDao productDao = new FakeProductDao();

        menuRepository = new MenuRepository(new FakeMenuDao(), new FakeMenuProductDao());
        menuService = new MenuService(menuRepository, menuGroupDao, productDao);

        저장된_후라이드_치킨 = productDao.save(후라이드_치킨());
        저장된_치킨_세트 = menuGroupDao.save(치킨_세트());
    }

    @DisplayName("메뉴를 등록한다")
    @Test
    void create() {
        final MenuProductRequest menuProductRequest = new MenuProductRequest(저장된_후라이드_치킨.getId(), MENU_PRODUCT_QUANTITY);
        final MenuRequest request = new MenuRequest(MENU_NAME, PRICE, 저장된_치킨_세트.getId(), List.of(menuProductRequest));

        final MenuResponse response = menuService.create(request);

        assertThat(response.getId()).isNotNull();
    }

    @DisplayName("메뉴 등록 시 메뉴 그룹의 아이디가 존재해야 한다")
    @Test
    void createMenuGroupIdIsNotExist() {
        final long notSavedMenuGroupId = 0L;

        final MenuProductRequest menuProductRequest = new MenuProductRequest(저장된_후라이드_치킨.getId(), MENU_PRODUCT_QUANTITY);
        final MenuRequest request = new MenuRequest(MENU_NAME, PRICE, notSavedMenuGroupId, List.of(menuProductRequest));

        assertThatThrownBy(() -> menuService.create(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("존재하지 않는 메뉴 그룹입니다.");
    }

    @DisplayName("메뉴 등록 시 메뉴 상품은 등록되어 있는 상품이어야 한다")
    @Test
    void createMenuProductIsNotExist() {
        final long notSavedProductId = 0L;

        final MenuProductRequest notSavedMenuProductRequest = new MenuProductRequest(notSavedProductId, MENU_PRODUCT_QUANTITY);
        final MenuRequest request = new MenuRequest(MENU_NAME, PRICE, 저장된_치킨_세트.getId(), List.of(notSavedMenuProductRequest));

        assertThatThrownBy(() -> menuService.create(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("존재하지 않는 상품입니다.");
    }

    @DisplayName("메뉴의 목록을 조회한다")
    @Test
    void list() {
        final int numberOfMenu = 5;
        for (int i = 0; i < numberOfMenu; i++) {
            final MenuProduct menuProduct = 가격_정보가_있는_상품_하나(저장된_후라이드_치킨.getId(), BigDecimal.valueOf(15_000));
            final Menu menu = 후라이드_치킨_세트의_가격과_메뉴_상품_리스트는(
                저장된_치킨_세트.getId(), BigDecimal.valueOf(15_000), List.of(menuProduct)
            );
            menuRepository.save(menu);
        }

        final List<MenuResponse> responses = menuService.list();

        assertThat(responses).hasSize(numberOfMenu);
    }
}
