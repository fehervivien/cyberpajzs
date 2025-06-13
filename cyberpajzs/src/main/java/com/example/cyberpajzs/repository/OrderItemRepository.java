package com.example.cyberpajzs.repository;

import com.example.cyberpajzs.entity.OrderItem;
import com.example.cyberpajzs.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    // Ezzel a metódussal lekérdezzük az OrderItem-eket a User-hez tartozó Order-en keresztül
    List<OrderItem> findByOrder_User(User user);
}