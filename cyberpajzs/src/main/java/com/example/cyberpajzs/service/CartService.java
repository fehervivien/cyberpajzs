package com.example.cyberpajzs.service;

import com.example.cyberpajzs.entity.CartItem;
import com.example.cyberpajzs.entity.Product;
import com.example.cyberpajzs.entity.User;
import com.example.cyberpajzs.repository.CartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final UserService userService;
    private final ProductService productService;

    @Autowired
    public CartService(CartItemRepository cartItemRepository, UserService userService, ProductService productService) {
        this.cartItemRepository = cartItemRepository;
        this.userService = userService;
        this.productService = productService;
    }

    public List<CartItem> getCartItems() {
        return userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName())
                .map(cartItemRepository::findByUser)
                .orElse(Collections.<CartItem>emptyList());
    }

    public BigDecimal calculateCartTotal() {
        return getCartItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public void addProductToCart(Product product, int quantity) {
        User currentUser = userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new RuntimeException("Felhasználó nem található."));

        Optional<CartItem> existingItemOptional = cartItemRepository.findByUserAndProduct(currentUser, product);
        if (existingItemOptional.isPresent()) {
            CartItem existingItem = existingItemOptional.get();
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setUser(currentUser);
            newItem.setQuantity(quantity);
            newItem.setPrice(product.getPrice());
            cartItemRepository.save(newItem);
        }
    }

    @Transactional
    public void updateProductQuantity(Long productId, int quantity) {
        User currentUser = userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new RuntimeException("Felhasználó nem található."));

        Optional<CartItem> itemOptional = cartItemRepository.findByUserAndProductId(currentUser, productId);
        itemOptional.ifPresent(item -> {
            if (quantity > 0) {
                item.setQuantity(quantity);
                cartItemRepository.save(item);
            } else {
                cartItemRepository.delete(item);
            }
        });
    }

    @Transactional
    public void removeProductFromCart(Long productId) {
        User currentUser = userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new RuntimeException("Felhasználó nem található."));

        Optional<CartItem> itemOptional = cartItemRepository.findByUserAndProductId(currentUser, productId);
        itemOptional.ifPresent(cartItemRepository::delete);
    }

    @Transactional
    public void clearCart() {
        User currentUser = userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new RuntimeException("Felhasználó nem található."));

        List<CartItem> userCart = cartItemRepository.findByUser(currentUser);
        cartItemRepository.deleteAll(userCart);
    }
}