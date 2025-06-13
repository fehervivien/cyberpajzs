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
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private int durationMonths;

    @Column(nullable = false)
    private int deviceLimit;

    private String imageUrl;

    private int stock; // Rendelkezésre álló licenckulcsok száma
    private String licenseInfo; // Licenckulcsok információi, pl. formátum, aktiválási folyamat stb.
}