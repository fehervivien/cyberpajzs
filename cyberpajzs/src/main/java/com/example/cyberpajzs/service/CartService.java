package com.example.cyberpajzs.service;

import com.example.cyberpajzs.entity.CartItem;
import com.example.cyberpajzs.entity.Product;
import com.example.cyberpajzs.entity.User;
import com.example.cyberpajzs.repository.CartItemRepository;
import com.example.cyberpajzs.repository.ProductRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserService userService;

    public CartService(CartItemRepository cartItemRepository, ProductRepository productRepository, UserService userService) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userService = userService;
    }

    @Transactional
    public void addProductToCart(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("A termék nem található: " + productId));

        if (quantity <= 0) {
            throw new IllegalArgumentException("A mennyiségnek pozitív számnak kell lennie.");
        }

        if (product.getStock() < quantity) {
            throw new IllegalStateException("Nincs elegendő készlet a " + product.getName() + " termékből.");
        }

        User currentUser = null;
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!username.equals("anonymousUser")) {
            Optional<User> userOptional = userService.findUserByUsername(username);
            if (userOptional.isPresent()) {
                currentUser = userOptional.get();
            }
        }

        CartItem cartItem = cartItemRepository.findByUserAndProduct(currentUser, product)
                .orElse(new CartItem());

        if (cartItem.getId() == null) { // Új kosárelem
            cartItem.setUser(currentUser);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setPriceAtOrder(product.getPrice());
        } else { // Meglévő kosárelem, frissítjük a mennyiséget
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        }

        cartItemRepository.save(cartItem);
    }

    public List<CartItem> getCartItems() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username.equals("anonymousUser")) {
            return new ArrayList<>();
        }

        Optional<User> userOptional = userService.findUserByUsername(username);

        if (userOptional.isPresent()) {
            User currentUser = userOptional.get();
            return cartItemRepository.findByUser(currentUser);
        } else {
            return new ArrayList<>();
        }
    }

    public BigDecimal calculateCartTotal() {
        List<CartItem> cartItems = getCartItems();
        return cartItems.stream()
                .map(item -> item.getPriceAtOrder().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public void updateCartItemQuantity(Long productId, int newQuantity) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username.equals("anonymousUser")) {
            throw new IllegalStateException("A kosár frissítéséhez be kell jelentkezni vagy vendégként kell kezelni a kosarat (ez a funkció jelenleg nem támogatott).");
        }

        Optional<User> userOptional = userService.findUserByUsername(username);

        if (userOptional.isPresent()) {
            User currentUser = userOptional.get();
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("A termék nem található: " + productId));

            CartItem cartItem = cartItemRepository.findByUserAndProduct(currentUser, product)
                    .orElseThrow(() -> new IllegalArgumentException("A kosárelem nem található."));

            if (newQuantity <= 0) {
                cartItemRepository.delete(cartItem);
            } else {
                if (product.getStock() < newQuantity) {
                    throw new IllegalStateException("Nincs elegendő készlet a " + product.getName() + " termékből.");
                }
                cartItem.setQuantity(newQuantity);
                cartItemRepository.save(cartItem);
            }
        }
    }

    @Transactional
    public void removeProductFromCart(Long productId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username.equals("anonymousUser")) {
            throw new IllegalStateException("A kosárból való eltávolításhoz be kell jelentkezni vagy vendégként kell kezelni a kosarat.");
        }

        Optional<User> userOptional = userService.findUserByUsername(username);

        if (userOptional.isPresent()) {
            User currentUser = userOptional.get();
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("A termék nem található: " + productId));

            cartItemRepository.findByUserAndProduct(currentUser, product)
                    .ifPresent(cartItemRepository::delete);
        }
    }

    @Transactional
    public void clearCart() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username.equals("anonymousUser")) {
            throw new IllegalStateException("A kosár ürítéséhez be kell jelentkezni vagy vendégként kell kezelni a kosarat.");
        }

        Optional<User> userOptional = userService.findUserByUsername(username);

        if (userOptional.isPresent()) {
            User currentUser = userOptional.get();
            cartItemRepository.deleteByUser(currentUser);
        }
    }
}
