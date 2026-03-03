package com.philomath.controller;

import com.philomath.dto.ProductDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Product management with comprehensive constraint violations collection.
 * <p>
 * Uses @Validated annotation to collect all constraint violations across all 10 fields
 * of the ProductDTO and return them in a structured response.
 */
@RestController
@RequestMapping("/products")
@Validated
public class ProductController {

    /**
     * POST endpoint to create a product.
     * <p>
     * Collects all constraint violations across all fields and returns them in a structured format.
     * Uses @Valid annotation to trigger validation and MethodArgumentNotValidException handler
     * to collect and format violations.
     *
     * @param product ProductDTO object with 10 fields of various types
     * @return ProductDTO object if valid, or error response with all constraint violations
     */
    @PostMapping("/create")
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO product) {
        // If validation passes, product is returned as-is
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    /**
     * POST endpoint to create a product with detailed violation response.
     * <p>
     * Alternative endpoint that returns more detailed constraint violation information.
     *
     * @param product ProductDTO object with 10 fields of various types
     * @return ProductDTO object if valid, detailed error response with violations
     */
    @PostMapping("/create-detailed")
    public ResponseEntity<ProductDTO> createProductDetailed(@Valid @RequestBody ProductDTO product) {
        // If validation passes, product is created
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    /**
     * GET endpoint to retrieve a product (for testing purposes).
     *
     * @param productId the product ID
     * @return a sample ProductDTO object
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable Long productId) {
        // Return a sample product for testing
        ProductDTO product = new ProductDTO();
        product.setProductId(productId);
        product.setProductName("Sample Product");
        product.setDescription("This is a sample product for testing");
        product.setSku("SKU12345");
        return ResponseEntity.ok(product);
    }
}
