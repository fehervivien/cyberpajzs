package com.example.cyberpajzs.entity;

public enum LicenseStatus {
    UNASSIGNED, // Még nincs hozzárendelve felhasználóhoz
    ASSIGNED,   // Felhasználóhoz rendelve és aktív
    EXPIRED,    // Lejárt
    REVOKED     // Visszavonva (pl. visszatérítés miatt)
}
