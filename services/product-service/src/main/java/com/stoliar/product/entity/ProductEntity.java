package com.stoliar.product.entity;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Document(collection = "products")
public class ProductEntity {

    @Id
    private UUID id;

    @Field("name")
    private String name;

    @Field("description")
    private String description;

    @Field("category")
    private String category;

    @Field(
            value = "price",
            targetType = FieldType.DECIMAL128
    )
    private BigDecimal price;

    @Field("active")
    private Boolean active;

    @CreatedDate
    @Field("created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private Instant updatedAt;

    protected ProductEntity() {
    }

    public ProductEntity(
            UUID id,
            String name,
            String description,
            String category,
            BigDecimal price,
            Boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Boolean getActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void update(
            String name,
            String description,
            String category,
            BigDecimal price
    ) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.updatedAt = Instant.now();
    }

    public void publish() {
        this.active = true;
        this.updatedAt = Instant.now();
    }

    public void hide() {
        this.active = false;
        this.updatedAt = Instant.now();
    }
}
