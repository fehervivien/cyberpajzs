package com.example.cyberpajzs.service;

import com.example.cyberpajzs.entity.License;
import com.example.cyberpajzs.entity.Product;
import com.example.cyberpajzs.entity.User;
import com.example.cyberpajzs.entity.OrderItem;
import com.example.cyberpajzs.repository.LicenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LicenseService {

    private final LicenseRepository licenseRepository;
    private final ProductService productService;

    public LicenseService(LicenseRepository licenseRepository, ProductService productService) {
        this.licenseRepository = licenseRepository;
        this.productService = productService;
    }

    public License addLicenseKey(String licenseKey, Long productId) {
        if (licenseRepository.findByLicenseKey(licenseKey).isPresent()) {
            throw new IllegalArgumentException("Ez a licenckulcs már szerepel a rendszerben: " + licenseKey);
        }

        Product product = productService.findProductById(productId)
                .orElseThrow(() -> new IllegalArgumentException("A megadott termék nem található ID: " + productId));

        License newLicense = new License();
        newLicense.setLicenseKey(licenseKey);
        newLicense.setProduct(product);
        newLicense.setStatus("AVAILABLE");

        License savedLicense = licenseRepository.save(newLicense);

        productService.updateProductStock(product, 1);

        return savedLicense;
    }

    @Transactional
    public License assignLicenseToUser(Product product, User user, OrderItem orderItem, LocalDateTime issueDate) {
        License licenseToAssign = licenseRepository.findFirstByProductAndStatus(product, "AVAILABLE")
                .orElseThrow(() -> new IllegalStateException("Nincs elérhető licenckulcs a(z) " + product.getName() + " termékhez."));

        licenseToAssign.setUser(user);
        licenseToAssign.setOrderItem(orderItem);
        licenseToAssign.setIssueDate(issueDate);
        licenseToAssign.setExpiryDate(issueDate.plusMonths(product.getDurationMonths()));
        licenseToAssign.setStatus("ACTIVE");

        License assignedLicense = licenseRepository.save(licenseToAssign);

        productService.updateProductStock(product, -1);

        return assignedLicense;
    }

    public List<License> getUserLicenses(User user) {
        return licenseRepository.findByUser(user);
    }

    public long getAvailableLicenseCountForProduct(Product product) {
        return licenseRepository.countByProductAndStatus(product, "AVAILABLE");
    }

    public List<License> getLicensesByOrderItem(List<OrderItem> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> orderItemIds = orderItems.stream()
                .map(OrderItem::getId)
                .collect(Collectors.toList());
        return licenseRepository.findByOrderItemIdIn(orderItemIds);
    }
}