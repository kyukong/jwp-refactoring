package kitchenpos.domain.fixture;

import java.math.BigDecimal;

import kitchenpos.domain.Product;

@SuppressWarnings("NonAsciiCharacters")
public class ProductFixture {

    private Long id;
    private String name;
    private BigDecimal price;

    private ProductFixture() {
    }

    public static Product 후라이드_치킨() {
        return 상품()
            .이름("후라이드 치킨")
            .가격(BigDecimal.valueOf(15_000))
            .build();
    }

    private static ProductFixture 상품() {
        return new ProductFixture();
    }

    private ProductFixture 이름(final String name) {
        this.name = name;
        return this;
    }

    private ProductFixture 가격(final BigDecimal price) {
        this.price = price;
        return this;
    }

    private Product build() {
        return new Product(id, name, price);
    }
}
