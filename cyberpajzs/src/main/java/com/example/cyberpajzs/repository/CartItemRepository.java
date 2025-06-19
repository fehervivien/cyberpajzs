package com.example.cyberpajzs.repository;

import com.example.cyberpajzs.entity.CartItem;
import com.example.cyberpajzs.entity.Product;
import com.example.cyberpajzs.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByUserAndProduct(User user, Product product);
    List<CartItem> findByUser(User user);
    //Kosár elemek törlése felhasználó alapján
    void deleteByUser(User user);
}