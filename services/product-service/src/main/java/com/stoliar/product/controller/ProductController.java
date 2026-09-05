package com.stoliar.product.controller;

import com.stoliar.product.dto.CreateProductRequest;
import com.stoliar.product.dto.ProductResponse;
import com.stoliar.product.dto.UpdateProductRequest;
import com.stoliar.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@Tag(
        name = "Products",
        description = "Product catalog management"
)
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(
            summary = "Search products",
            description = """
                    Returns products matching the specified filters.

                    Regular authenticated users see only published products.
                    Administrators can use the active parameter to include hidden products.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Products successfully retrieved"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            )
    })
    public Page<ProductResponse> findAll(
            @Parameter(
                    description = "Search text in product name or description",
                    in = ParameterIn.QUERY
            )
            @RequestParam(required = false) String query,

            @Parameter(
                    description = "Product category"
            )
            @RequestParam(required = false) String category,

            @Parameter(
                    description = "Minimum product price"
            )
            @RequestParam(required = false) BigDecimal minPrice,

            @Parameter(
                    description = "Maximum product price"
            )
            @RequestParam(required = false) BigDecimal maxPrice,

            @Parameter(
                    description = "Filter by publication status"
            )
            @RequestParam(required = false) Boolean active,

            @Parameter(
                    description = "Zero-based page number",
                    example = "0"
            )
            @RequestParam(defaultValue = "0") int page,

            @Parameter(
                    description = "Number of products per page",
                    example = "20"
            )
            @RequestParam(defaultValue = "20") int size,

            @Parameter(
                    description = "Sort field",
                    example = "price"
            )
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(
                    description = "Sort direction",
                    example = "asc"
            )
            @RequestParam(defaultValue = "desc") String direction
    ) {
        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page must be greater than or equal to 0"
            );
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Size must be between 1 and 100"
            );
        }

        if (minPrice != null
                && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException(
                    "minPrice must be less than or equal to maxPrice"
            );
        }

        Sort.Direction sortDirection =
                Sort.Direction.fromString(direction);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );

        return productService.search(
                query,
                category,
                minPrice,
                maxPrice,
                active,
                pageable
        ).map(ProductResponse::from);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get product by ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product found"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            )
    })
    public ProductResponse findById(
            @Parameter(
                    description = "Product UUID",
                    required = true
            )
            @PathVariable UUID id
    ) {
        return ProductResponse.from(
                productService.findById(id)
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create product",
            description = "Creates a new unpublished product. Requires ROLE_ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Product created"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ROLE_ADMIN required"
            )
    })
    public ProductResponse create(
            @Valid @RequestBody CreateProductRequest request
    ) {
        return ProductResponse.from(
                productService.create(request)
        );
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update product",
            description = "Updates product data. Requires ROLE_ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product updated"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ROLE_ADMIN required"
            )
    })
    public ProductResponse update(
            @Parameter(
                    description = "Product UUID",
                    required = true
            )
            @PathVariable UUID id,

            @Valid @RequestBody UpdateProductRequest request
    ) {
        return ProductResponse.from(
                productService.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete product",
            description = "Deletes product. Requires ROLE_ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Product deleted"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ROLE_ADMIN required"
            )
    })
    public void delete(
            @PathVariable UUID id
    ) {
        productService.delete(id);
    }

    @PatchMapping("/{id}/publish")
    @Operation(
            summary = "Publish product",
            description = "Makes product visible in the catalog. Requires ROLE_ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product published"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ROLE_ADMIN required"
            )
    })
    public ProductResponse publish(
            @PathVariable UUID id
    ) {
        return ProductResponse.from(
                productService.publish(id)
        );
    }

    @PatchMapping("/{id}/hide")
    @Operation(
            summary = "Hide product",
            description = "Hides product from the catalog. Requires ROLE_ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product hidden"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ROLE_ADMIN required"
            )
    })
    public ProductResponse hide(
            @PathVariable UUID id
    ) {
        return ProductResponse.from(
                productService.hide(id)
        );
    }
}