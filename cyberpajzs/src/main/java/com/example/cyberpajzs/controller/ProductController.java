package com.example.cyberpajzs.controller;

import com.example.cyberpajzs.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping({"/", "/product-list"})
    public String showProductList(Model model, @RequestParam(value = "keyword", required = false) String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            model.addAttribute("products", productService.searchProducts(keyword.trim()));
            model.addAttribute("keyword", keyword);
        } else {
            model.addAttribute("products", productService.findAllProducts());
        }
        return "product-list";
    }

    @GetMapping("/product-details/{id}")
    public String showProductDetails(@PathVariable Long id, Model model) {
        productService.findProductById(id).ifPresent(product -> model.addAttribute("product", product));
        return "product-details";
    }
}