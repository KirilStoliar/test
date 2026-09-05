package com.stoliar.product.repository;

import com.stoliar.product.entity.ProductEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface ProductRepository
        extends MongoRepository<ProductEntity, UUID> {
}