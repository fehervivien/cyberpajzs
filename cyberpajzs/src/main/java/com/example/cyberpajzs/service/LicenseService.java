package com.example.cyberpajzs.service;

import com.example.cyberpajzs.entity.License;
import com.example.cyberpajzs.entity.LicenseStatus;
import com.example.cyberpajzs.entity.Product;
import com.example.cyberpajzs.entity.User;
import com.example.cyberpajzs.entity.OrderItem;
import com.example.cyberpajzs.repository.LicenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LicenseService {

    private final LicenseRepository licenseRepository;

    public LicenseService(LicenseRepository licenseRepository) {
        this.licenseRepository = licenseRepository;
    }

    @Transactional
    public License assignLicenseToUser(Product product, User user, OrderItem orderItem, LocalDateTime issueDate) {
        // Keressünk egy nem hozzárendelt licenckulcsot az adott termékhez
        License availableLicense = licenseRepository.findTopByProductAndStatus(product, LicenseStatus.UNASSIGNED)
                .orElseThrow(() -> new IllegalStateException("Nincs elérhető licenckulcs a(z) " + product.getName() + " termékhez."));

        // Hozzárendeljük a felhasználóhoz és az orderItem-hez
        availableLicense.setUser(user);
        availableLicense.setOrderItem(orderItem);
        availableLicense.setStatus(LicenseStatus.ASSIGNED);
        availableLicense.setIssueDate(issueDate);

        return licenseRepository.save(availableLicense);
    }

    // ÚJ: Licenckulcs generáló metódus
    private String generateUniqueLicenseKey() {
        return UUID.randomUUID().toString().toUpperCase();
    }

    // ÚJ: Termékhez licencek generálása és mentése
    @Transactional
    public void generateLicensesForProduct(Product product, int quantity) {
        for (int i = 0; i < quantity; i++) {
            License license = new License();
            license.setProduct(product);
            license.setLicenseKey(generateUniqueLicenseKey());
            license.setStatus(LicenseStatus.UNASSIGNED); // Alapértelmezett állapot
            licenseRepository.save(license);
        }
    }

    public List<License> getUserLicenses(User user) {
        return licenseRepository.findByUser(user);
    }

    public Optional<License> getLicenseByKey(String licenseKey) {
        return licenseRepository.findByLicenseKey(licenseKey);
    }

    public List<License> findAllLicenses() {
        return licenseRepository.findAll();
    }

    // Keresés termék és státusz alapján
    public List<License> findLicensesByProductAndStatus(Product product, LicenseStatus status) {
        return licenseRepository.findByProductAndStatus(product, status);
    }

    @Transactional
    public void updateLicenseStatus(Long licenseId, LicenseStatus newStatus) {
        License license = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new IllegalArgumentException("Licenc nem található ID: " + licenseId));
        license.setStatus(newStatus);
        licenseRepository.save(license);
    }

    @Transactional
    public void deleteLicense(Long id) {
        licenseRepository.deleteById(id);
    }
}
