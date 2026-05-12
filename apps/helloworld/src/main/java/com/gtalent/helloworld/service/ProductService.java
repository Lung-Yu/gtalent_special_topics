package com.gtalent.helloworld.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.gtalent.helloworld.repository.ProductRepository;
import com.gtalent.helloworld.service.entities.Product;

@Service
public class ProductService {


    @Autowired
    private ProductRepository productRepository;


    public Page<Product> getProducts(String name, Pageable pageable) {
        if (name == null || name.isEmpty()) {
            return productRepository.findAll(pageable);
        } else {
            return productRepository.findByNameContaining(name.trim(), pageable);
        }
    }


    public Product createProduct(Product product) {
        return productRepository.save(product);
    }


    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }


    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }


    public Product updateProduct(Long id, Product product) {
        Product existingProduct = getProductById(id);
        if (existingProduct == null) {
            return null; // or throw an exception
        }

        existingProduct.setName(product.getName());
        existingProduct.setPrice(product.getPrice());
        // Update other fields as necessary

        return productRepository.save(existingProduct);
    }


}
