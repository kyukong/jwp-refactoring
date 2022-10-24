package kitchenpos.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import kitchenpos.domain.Product;

@DisplayName("Product 서비스 테스트")
@SpringBootTest
@Transactional
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @DisplayName("상품을 등록한다")
    @Test
    void create() {
        final Product product = new Product();
        product.setName("상품");
        product.setPrice(new BigDecimal(1000));

        final Product savedProduct = productService.create(product);

        assertThat(savedProduct.getId()).isNotNull();
    }

    @DisplayName("상품 등록 시 상품의 가격은 null 이 아니어야 한다")
    @Test
    void createPriceIsNotNull() {
        final Product product = new Product();
        product.setName("상품");
        product.setPrice(null);

        assertThatThrownBy(() -> productService.create(product))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("상품 등록 시 상품의 가격은 0원 이상이어야 한다")
    @Test
    void createPriceIsOverZero() {
        final Product product = new Product();
        product.setName("상품");
        product.setPrice(new BigDecimal(-1));

        assertThatThrownBy(() -> productService.create(product))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("상품의 목록을 조회한다")
    @Test
    void list() {
        final List<Product> products = productService.list();

        assertThat(products).hasSize(6);
    }
}
