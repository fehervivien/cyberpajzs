package com.example.cyberpajzs.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "order_items") // "OrderItem" helyett "order_items" javasolt
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order; // Melyik megrendeléshez tartozik

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // Melyik termék

    @Column(nullable = false)
    private int quantity; // Mennyiség

    @Column(nullable = false)
    private BigDecimal priceAtOrder; // Az ár a megrendelés pillanatában (fontos, mert az ár változhat a jövőben)
}