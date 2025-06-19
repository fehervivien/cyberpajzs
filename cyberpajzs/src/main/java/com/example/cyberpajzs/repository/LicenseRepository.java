package com.example.cyberpajzs.repository;

import com.example.cyberpajzs.entity.License;
import com.example.cyberpajzs.entity.OrderItem;
import com.example.cyberpajzs.entity.Product;
import com.example.cyberpajzs.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LicenseRepository extends JpaRepository<License, Long> {
    Optional<License> findByLicenseKey(String licenseKey);
    List<License> findByUser(User user);
    List<License> findByUserAndStatus(User user, String status);
    Optional<License> findFirstByProductAndStatus(Product product, String status);
    long countByProductAndStatus(Product product, String status);
    List<License> findByOrderItemIdIn(List<Long> orderItemIds);
    List<License> findByOrderItemIn(List<OrderItem> orderItems);
}