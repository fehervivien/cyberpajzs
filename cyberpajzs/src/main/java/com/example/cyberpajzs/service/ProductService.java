package com.example.cyberpajzs.service;

import com.example.cyberpajzs.entity.Product;
import com.example.cyberpajzs.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> findProductById(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> searchProducts(String searchTerm) {
        // Egyszerű keresés név és leírás alapján
        return productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(searchTerm, searchTerm);
    }

    @Transactional
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("A termék nem található, ID: " + id);
        }
        productRepository.deleteById(id);
    }

    // ÚJ METÓDUS: Termék készletének frissítése
    @Transactional
    public void updateProductStock(Product product, int quantityChange) {
        // Keresse meg a terméket az adatbázisban, hogy biztosan a legfrissebb verzióval dolgozzunk
        Product managedProduct = productRepository.findById(product.getId())
                .orElseThrow(() -> new IllegalArgumentException("A termék nem található, ID: " + product.getId()));

        int currentStock = managedProduct.getStock();
        int newStock = currentStock + quantityChange;

        if (newStock < 0) {
            throw new IllegalStateException("A készlet nem mehet null alá a " + managedProduct.getName() + " termék esetében.");
        }

        managedProduct.setStock(newStock);
        productRepository.save(managedProduct);
    }
}