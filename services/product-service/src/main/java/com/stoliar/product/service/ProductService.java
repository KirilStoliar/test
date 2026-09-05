package com.stoliar.product.service;

import com.stoliar.product.dto.CreateProductRequest;
import com.stoliar.product.dto.UpdateProductRequest;
import com.stoliar.product.entity.ProductEntity;
import com.stoliar.product.exception.ProductNotFoundException;
import com.stoliar.product.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final MongoTemplate mongoTemplate;

    public ProductService(
            ProductRepository productRepository,
            MongoTemplate mongoTemplate
    ) {
        this.productRepository = productRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public Page<ProductEntity> search(
            String query,
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean active,
            Pageable pageable
    ) {
        Query mongoQuery = new Query();

        if (query != null && !query.isBlank()) {
            String escapedQuery = Pattern.quote(query.trim());
            Pattern pattern = Pattern.compile(
                    escapedQuery,
                    Pattern.CASE_INSENSITIVE
            );

            mongoQuery.addCriteria(
                    new Criteria().orOperator(
                            Criteria.where("name").regex(pattern),
                            Criteria.where("description").regex(pattern)
                    )
            );
        }

        if (category != null && !category.isBlank()) {
            mongoQuery.addCriteria(
                    Criteria.where("category")
                            .is(category.trim())
            );
        }

        if (minPrice != null && maxPrice != null) {
            mongoQuery.addCriteria(
                    Criteria.where("price")
                            .gte(minPrice)
                            .lte(maxPrice)
            );
        } else if (minPrice != null) {
            mongoQuery.addCriteria(
                    Criteria.where("price")
                            .gte(minPrice)
            );
        } else if (maxPrice != null) {
            mongoQuery.addCriteria(
                    Criteria.where("price")
                            .lte(maxPrice)
            );
        }

        if (active != null) {
            mongoQuery.addCriteria(
                    Criteria.where("active").is(active)
            );
        }

        long total = mongoTemplate.count(
                mongoQuery,
                ProductEntity.class
        );

        mongoQuery.with(pageable);

        List<ProductEntity> products =
                mongoTemplate.find(
                        mongoQuery,
                        ProductEntity.class
                );

        return new PageImpl<>(
                products,
                pageable,
                total
        );
    }

    public ProductEntity findById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(id)
                );
    }

    public ProductEntity create(
            CreateProductRequest request
    ) {
        Instant now = Instant.now();

        ProductEntity product = new ProductEntity(
                UUID.randomUUID(),
                request.name().trim(),
                request.description().trim(),
                request.category().trim(),
                request.price(),
                false,
                now,
                now
        );

        return productRepository.save(product);
    }

    public ProductEntity update(
            UUID id,
            UpdateProductRequest request
    ) {
        ProductEntity product = findById(id);

        product.update(
                request.name().trim(),
                request.description().trim(),
                request.category().trim(),
                request.price()
        );

        return productRepository.save(product);
    }

    public void delete(UUID id) {
        ProductEntity product = findById(id);

        productRepository.delete(product);
    }

    public ProductEntity publish(UUID id) {
        ProductEntity product = findById(id);

        product.publish();

        return productRepository.save(product);
    }

    public ProductEntity hide(UUID id) {
        ProductEntity product = findById(id);

        product.hide();

        return productRepository.save(product);
    }
}