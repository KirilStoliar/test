package com.stoliar.product.service;

import com.stoliar.product.dto.CreateProductRequest;
import com.stoliar.product.dto.UpdateProductRequest;
import com.stoliar.product.entity.ProductEntity;
import com.stoliar.product.exception.ProductNotFoundException;
import com.stoliar.product.repository.ProductRepository;
import org.bson.types.Decimal128;
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
            boolean admin,
            Pageable pageable
    ) {
        Query mongoQuery = new Query();

        addTextFilter(
                mongoQuery,
                query
        );

        addCategoryFilter(
                mongoQuery,
                category
        );

        addPriceFilter(
                mongoQuery,
                minPrice,
                maxPrice
        );

        /*
         * Security rule:
         *
         * USER:
         *     always sees active=true products.
         *
         * ADMIN:
         *     can explicitly filter by active.
         *     if active is omitted, admin sees both active and inactive.
         */
        if (admin) {
            if (active != null) {
                mongoQuery.addCriteria(
                        Criteria.where("active").is(active)
                );
            }
        } else {
            mongoQuery.addCriteria(
                    Criteria.where("active").is(true)
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

    public ProductEntity findById(
            UUID id,
            boolean admin
    ) {
        ProductEntity product = productRepository
                .findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(id)
                );

        /*
         * Hidden products must not be exposed to regular users.
         *
         * Returning ProductNotFoundException instead of AccessDeniedException
         * also avoids revealing that a hidden product exists.
         */
        if (!admin && !Boolean.TRUE.equals(product.getActive())) {
            throw new ProductNotFoundException(id);
        }

        return product;
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
        ProductEntity product = findByIdForAdmin(id);

        product.update(
                request.name().trim(),
                request.description().trim(),
                request.category().trim(),
                request.price()
        );

        return productRepository.save(product);
    }

    public void delete(UUID id) {
        ProductEntity product = findByIdForAdmin(id);

        productRepository.delete(product);
    }

    public ProductEntity publish(UUID id) {
        ProductEntity product = findByIdForAdmin(id);

        product.publish();

        return productRepository.save(product);
    }

    public ProductEntity hide(UUID id) {
        ProductEntity product = findByIdForAdmin(id);

        product.hide();

        return productRepository.save(product);
    }

    private ProductEntity findByIdForAdmin(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(id)
                );
    }

    private void addTextFilter(
            Query mongoQuery,
            String query
    ) {
        if (query == null || query.isBlank()) {
            return;
        }

        String normalizedQuery = query.trim();

        /*
         * Pattern.quote() treats the user input as literal text
         * instead of allowing regular-expression syntax.
         */
        Pattern pattern = Pattern.compile(
                Pattern.quote(normalizedQuery),
                Pattern.CASE_INSENSITIVE
        );

        mongoQuery.addCriteria(
                new Criteria().orOperator(
                        Criteria.where("name").regex(pattern),
                        Criteria.where("description").regex(pattern)
                )
        );
    }

    private void addCategoryFilter(
            Query mongoQuery,
            String category
    ) {
        if (category == null || category.isBlank()) {
            return;
        }

        mongoQuery.addCriteria(
                Criteria.where("category")
                        .is(category.trim())
        );
    }

    private void addPriceFilter(
            Query mongoQuery,
            BigDecimal minPrice,
            BigDecimal maxPrice
    ) {
        /*
         * ProductEntity.price is stored in MongoDB as Decimal128.
         *
         * Passing BigDecimal directly to Criteria.gte()/lte()
         * results in String values in the generated Mongo query
         * in the current configuration:
         *
         *     "$gte": "900"
         *     "$lte": "1100"
         *
         * MongoDB cannot match those String values against
         * the Decimal128 price field.
         *
         * Explicit Decimal128 conversion guarantees that the
         * generated query uses BSON Decimal128 values.
         */
        if (minPrice != null && maxPrice != null) {
            mongoQuery.addCriteria(
                    Criteria.where("price")
                            .gte(new Decimal128(minPrice))
                            .lte(new Decimal128(maxPrice))
            );
            return;
        }

        if (minPrice != null) {
            mongoQuery.addCriteria(
                    Criteria.where("price")
                            .gte(new Decimal128(minPrice))
            );
            return;
        }

        if (maxPrice != null) {
            mongoQuery.addCriteria(
                    Criteria.where("price")
                            .lte(new Decimal128(maxPrice))
            );
        }
    }
}
