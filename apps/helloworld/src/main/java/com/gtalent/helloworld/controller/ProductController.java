package com.gtalent.helloworld.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.gtalent.helloworld.service.ProductService;
import com.gtalent.helloworld.service.entities.Product;



@RestController
public class ProductController {
    
    @Autowired
    private ProductService productService;


    @PostMapping("/products")
    public Product createProduct(@RequestBody Product product) {
        // Implementation for creating a new product
        Product result = productService.createProduct(product);

        return result;
    }

    @DeleteMapping("/products/{id}")
    public void deleteProduct(@PathVariable Long id) {
        // Implementation for deleting a product by ID
        productService.deleteProduct(id);
    }

    @PutMapping("/products/{id}")
    public Product updateProduct(@PathVariable Long id, @RequestBody Product product) {
        // Implementation for updating an existing product
        Product result = productService.updateProduct(id, product);
        return result;
    }

    // http get /products?name=xxx -> select * from product where name like '%xxx%'
    @GetMapping("/products")
   public List<Product> getProducts(@Param("name") String name) {
        // Implementation for retrieving products

        return productService.getProducts(name);
    }


    @GetMapping("/products/{id}")

    public Product getProductById(@PathVariable Long id) {
        // Implementation for retrieving a specific product by ID
        return productService.getProductById(id);
    }



}
