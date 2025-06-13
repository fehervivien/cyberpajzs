package com.example.cyberpajzs.repository;

import com.example.cyberpajzs.entity.CartItem;
import com.example.cyberpajzs.entity.Product;
import com.example.cyberpajzs.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    Optional<CartItem> findByUserAndProduct(User user, Product product);
    Optional<CartItem> findByUserAndProductId(User user, Long productId);
}