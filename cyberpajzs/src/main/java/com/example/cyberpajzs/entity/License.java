package com.example.cyberpajzs.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "licenses")
public class License {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    // A gyártótól kapott egyedi licenckulcs
    private String licenseKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = true)
    private OrderItem orderItem;

    @Column(nullable = true)
    // Kiállítás dátuma
    private LocalDateTime issueDate;

    @Column(nullable = true)
    // Lejárat dátuma
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    //"AVAILABLE", "ACTIVE", "EXPIRED", "REVOKED", "PENDING"
    private String status;
}