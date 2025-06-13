package com.example.cyberpajzs.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders") // "Order" helyett "orders" javasolt
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // A megrendeléshez tartozó felhasználó

    @Column(nullable = false)
    private LocalDateTime orderDate; // A megrendelés dátuma és ideje

    @Column(nullable = false)
    private BigDecimal totalAmount; // A megrendelés teljes összege

    @Column(nullable = false)
    private String status; // Pl. "PENDING", "COMPLETED", "CANCELLED"

    // Szállítási/Számlázási adatok (Egyszerűsített változat)
    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String lastName;
    @Column(nullable = false)
    private String email;
    private String phone; // Opcionális

    @Column(nullable = false)
    private String address;
    @Column(nullable = false)
    private String city;
    @Column(nullable = false)
    private String zipCode;
    @Column(nullable = false)
    private String country;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems; // A megrendeléshez tartozó tételek
}