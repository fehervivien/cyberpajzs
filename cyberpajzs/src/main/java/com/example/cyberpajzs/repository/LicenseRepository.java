package com.example.cyberpajzs.repository;

import com.example.cyberpajzs.entity.License;
import com.example.cyberpajzs.entity.LicenseStatus;
import com.example.cyberpajzs.entity.Product;
import com.example.cyberpajzs.entity.User;
import com.example.cyberpajzs.entity.OrderItem; // Importálva
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LicenseRepository extends JpaRepository<License, Long> {

    // Licenckulcs keresése kulcs alapján (egyedi)
    Optional<License> findByLicenseKey(String licenseKey);

    // Felhasználóhoz rendelt licencek lekérése
    List<License> findByUser(User user);

    // Licencek lekérése termék és státusz alapján
    List<License> findByProductAndStatus(Product product, LicenseStatus status);

    // ÚJ METÓDUS: Az első (top) nem hozzárendelt licenc lekérése termék alapján
    Optional<License> findTopByProductAndStatus(Product product, LicenseStatus status);

    // Licencek lekérése OrderItem alapján
    List<License> findByOrderItemIn(List<OrderItem> orderItems);
}
