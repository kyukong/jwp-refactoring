package kitchenpos.application;

import kitchenpos.application.dto.request.ProductRequest;
import kitchenpos.application.dto.response.ProductResponse;
import kitchenpos.dao.ProductDao;
import kitchenpos.domain.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@Service
public class ProductService {
    private final ProductDao productDao;

    public ProductService(final ProductDao productDao) {
        this.productDao = productDao;
    }

    @Transactional
    public ProductResponse create(final ProductRequest request) {
        final Product savedProduct = productDao.save(
            new Product(
                request.getName(),
                request.getPrice()
            )
        );
        return new ProductResponse(savedProduct);
    }

    public List<ProductResponse> list() {
        final List<Product> products = productDao.findAll();
        return products.stream()
            .map(product -> new ProductResponse(product.getId(), product.getName(), product.getPrice()))
            .collect(Collectors.toUnmodifiableList());
    }
}
