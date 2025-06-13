package com.example.cyberpajzs.enums;

public enum OrderStatus {
    PENDING,        // Függőben lévő, még nem feldolgozott
    PROCESSING,     // Feldolgozás alatt
    COMPLETED,      // Elkészült, lezárva (pl. licenckulcs kiadva)
    CANCELLED,      // Törölt
    REFUNDED        // Visszatérített
}