package com.stoliar.product.controller;

import com.stoliar.product.dto.CreateProductRequest;
import com.stoliar.product.dto.ProductResponse;
import com.stoliar.product.dto.UpdateProductRequest;
import com.stoliar.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@Tag(
        name = "Products",
        description = "Product catalog management"
)
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private static final int MAX_PAGE_SIZE = 100;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "name",
            "category",
            "price",
            "createdAt",
            "updatedAt"
    );

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

                    Administrators can additionally filter products by the
                    active parameter and therefore can see hidden products.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Products successfully retrieved"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request parameters"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            )
    })
    public Page<ProductResponse> findAll(
            @Parameter(
                    description = "Search text in product name or description",
                    in = ParameterIn.QUERY,
                    example = "phone"
            )
            @RequestParam(required = false)
            String query,

            @Parameter(
                    description = "Product category",
                    example = "electronics"
            )
            @RequestParam(required = false)
            String category,

            @Parameter(
                    description = "Minimum product price",
                    example = "100.00"
            )
            @RequestParam(required = false)
            BigDecimal minPrice,

            @Parameter(
                    description = "Maximum product price",
                    example = "1000.00"
            )
            @RequestParam(required = false)
            BigDecimal maxPrice,

            @Parameter(
                    description = """
                            Filter by publication status.
                            This parameter is available only to administrators.
                            Regular users always receive active products only.
                            """
            )
            @RequestParam(required = false)
            Boolean active,

            @Parameter(
                    description = "Zero-based page number",
                    example = "0"
            )
            @RequestParam(defaultValue = "0")
            int page,

            @Parameter(
                    description = "Number of products per page",
                    example = "20"
            )
            @RequestParam(defaultValue = "20")
            int size,

            @Parameter(
                    description = "Sort field",
                    example = "price"
            )
            @RequestParam(defaultValue = "createdAt")
            String sortBy,

            @Parameter(
                    description = "Sort direction",
                    example = "desc"
            )
            @RequestParam(defaultValue = "desc")
            String direction,

            Authentication authentication
    ) {
        validatePageParameters(page, size);
        validatePriceRange(minPrice, maxPrice);

        String normalizedSortBy = validateSortField(sortBy);
        Sort.Direction sortDirection = parseSortDirection(direction);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, normalizedSortBy)
        );

        boolean admin = isAdmin(authentication);

        return productService.search(
                query,
                category,
                minPrice,
                maxPrice,
                active,
                admin,
                pageable
        ).map(ProductResponse::from);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get product by ID",
            description = """
                    Returns a product by its UUID.

                    Regular authenticated users can retrieve only published
                    products.

                    Administrators can retrieve both published and hidden
                    products.
                    """
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
            @PathVariable
            UUID id,

            Authentication authentication
    ) {
        boolean admin = isAdmin(authentication);

        return ProductResponse.from(
                productService.findById(id, admin)
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create product",
            description = """
                    Creates a new unpublished product.

                    Requires ROLE_ADMIN.
                    """
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
            @Valid
            @RequestBody
            CreateProductRequest request
    ) {
        return ProductResponse.from(
                productService.create(request)
        );
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update product",
            description = """
                    Updates product data.

                    Requires ROLE_ADMIN.
                    """
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
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ROLE_ADMIN required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found"
            )
    })
    public ProductResponse update(
            @Parameter(
                    description = "Product UUID",
                    required = true
            )
            @PathVariable
            UUID id,

            @Valid
            @RequestBody
            UpdateProductRequest request
    ) {
        return ProductResponse.from(
                productService.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete product",
            description = """
                    Deletes a product.

                    Requires ROLE_ADMIN.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Product deleted"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ROLE_ADMIN required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found"
            )
    })
    public void delete(
            @Parameter(
                    description = "Product UUID",
                    required = true
            )
            @PathVariable
            UUID id
    ) {
        productService.delete(id);
    }

    @PatchMapping("/{id}/publish")
    @Operation(
            summary = "Publish product",
            description = """
                    Makes a product visible in the catalog.

                    Requires ROLE_ADMIN.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product published"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ROLE_ADMIN required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found"
            )
    })
    public ProductResponse publish(
            @Parameter(
                    description = "Product UUID",
                    required = true
            )
            @PathVariable
            UUID id
    ) {
        return ProductResponse.from(
                productService.publish(id)
        );
    }

    @PatchMapping("/{id}/hide")
    @Operation(
            summary = "Hide product",
            description = """
                    Hides a product from the regular catalog.

                    Requires ROLE_ADMIN.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product hidden"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ROLE_ADMIN required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found"
            )
    })
    public ProductResponse hide(
            @Parameter(
                    description = "Product UUID",
                    required = true
            )
            @PathVariable
            UUID id
    ) {
        return ProductResponse.from(
                productService.hide(id)
        );
    }

    private void validatePageParameters(
            int page,
            int size
    ) {
        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page must be greater than or equal to 0"
            );
        }

        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                    "Size must be between 1 and " + MAX_PAGE_SIZE
            );
        }
    }

    private void validatePriceRange(
            BigDecimal minPrice,
            BigDecimal maxPrice
    ) {
        if (minPrice != null && minPrice.signum() < 0) {
            throw new IllegalArgumentException(
                    "minPrice must be greater than or equal to 0"
            );
        }

        if (maxPrice != null && maxPrice.signum() < 0) {
            throw new IllegalArgumentException(
                    "maxPrice must be greater than or equal to 0"
            );
        }

        if (minPrice != null
                && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException(
                    "minPrice must be less than or equal to maxPrice"
            );
        }
    }

    private String validateSortField(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "createdAt";
        }

        String normalizedSortBy = sortBy.trim();

        if (!ALLOWED_SORT_FIELDS.contains(normalizedSortBy)) {
            throw new IllegalArgumentException(
                    "Unsupported sort field: " + normalizedSortBy
                            + ". Allowed values: "
                            + String.join(", ", ALLOWED_SORT_FIELDS)
            );
        }

        return normalizedSortBy;
    }

    private Sort.Direction parseSortDirection(
            String direction
    ) {
        if (direction == null || direction.isBlank()) {
            return Sort.Direction.DESC;
        }

        try {
            return Sort.Direction.fromString(
                    direction.trim()
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Unsupported sort direction: " + direction
                            + ". Allowed values: asc, desc"
            );
        }
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }
}