package com.example.cyberpajzs.repository;

import com.example.cyberpajzs.entity.Order;
import com.example.cyberpajzs.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
    Optional<Order> findOrderByIdAndUser(Long id, User user);
    Optional<Order> findOrderById(Long id);
    List<Order> findOrdersByUser(User user);
}