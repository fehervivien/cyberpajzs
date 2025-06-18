package com.example.cyberpajzs.controller;

import com.example.cyberpajzs.entity.Product;
import com.example.cyberpajzs.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger; // UJ IMPORT
import org.slf4j.LoggerFactory; // UJ IMPORT

@Controller
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class); // UJ: Logger inicializálása

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping({"/", "/product-list"})
    public String listProducts(Model model, @RequestParam(value = "search", required = false) String search) {
        List<Product> products;
        if (search != null && !search.isEmpty()) {
            products = productService.searchProducts(search);
            model.addAttribute("searchQuery", search);
        } else {
            products = productService.findAllProducts();
        }
        model.addAttribute("products", products);
        return "product-list";
    }

    @GetMapping("/product-details/{id}")
    public String showProductDetails(@PathVariable Long id, Model model) {
        logger.info("Termék részletek oldal kérése ID-vel: {}", id); // LOG: Termék részletek kérés

        // Ellenőrizzük, hogy érkezett-e success vagy error üzenet a RedirectAttributes-ből
        if (model.containsAttribute("success")) {
            logger.info("Flash attribútum 'success' megtalálva: {}", model.getAttribute("success"));
        }
        if (model.containsAttribute("error")) {
            logger.info("Flash attribútum 'error' megtalálva: {}", model.getAttribute("error"));
        }


        Optional<Product> productOptional = productService.findProductById(id);
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            model.addAttribute("product", product);
            logger.info("Termék megtalálva a részletekhez: {} (ID: {})", product.getName(), id); // LOG: Termék megtalálva
        } else {
            model.addAttribute("product", null);
            logger.warn("Termék (ID: {}) nem található a részletekhez.", id); // LOG: Termék nem található
        }
        return "product-details";
    }
}