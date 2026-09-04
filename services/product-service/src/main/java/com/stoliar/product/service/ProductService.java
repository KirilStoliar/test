package com.stoliar.product.service;

import com.stoliar.product.dto.CreateProductRequest;
import com.stoliar.product.dto.UpdateProductRequest;
import com.stoliar.product.entity.ProductEntity;
import com.stoliar.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductEntity> findAll() {
        return productRepository.findAll();
    }

    public ProductEntity findById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(id)
                );
    }

    @Transactional
    public ProductEntity create(CreateProductRequest request) {
        Instant now = Instant.now();

        ProductEntity product = new ProductEntity(
                UUID.randomUUID(),
                request.name(),
                request.description(),
                request.price(),
                true,
                now,
                now
        );

        return productRepository.save(product);
    }

    @Transactional
    public ProductEntity update(
            UUID id,
            UpdateProductRequest request
    ) {
        ProductEntity product = findById(id);

        product.update(
                request.name(),
                request.description(),
                request.price(),
                request.active()
        );

        return productRepository.save(product);
    }

    @Transactional
    public void delete(UUID id) {
        ProductEntity product = findById(id);

        productRepository.delete(product);
    }

    public static class ProductNotFoundException
            extends RuntimeException {

        public ProductNotFoundException(UUID id) {
            super("Product not found: " + id);
        }
    }
}