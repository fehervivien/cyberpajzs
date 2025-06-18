package com.example.cyberpajzs.config;

import com.example.cyberpajzs.entity.Product;
import com.example.cyberpajzs.entity.User;
import com.example.cyberpajzs.repository.ProductRepository;
import com.example.cyberpajzs.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Component
public class DatabaseInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProductRepository productRepository;

    public DatabaseInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder, ProductRepository productRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@cyberpajzs.hu");
            adminUser.setFirstName("Admin");
            adminUser.setLastName("Felhasználó");
            adminUser.setPassword(passwordEncoder.encode("password"));

            Set<String> roles = new HashSet<>();
            roles.add("ROLE_USER");
            roles.add("ROLE_ADMIN");
            adminUser.setRoles(roles);

            userRepository.save(adminUser);
            System.out.println("Alapértelmezett admin felhasználó létrehozva: admin / password");
        }

        if (productRepository.count() == 0) {
            Product testProduct = new Product();
            testProduct.setName("Cyberpajzs Pro");
            testProduct.setDescription("A Cyberpajzs Pro egy fejlett biztonsági szoftver, amely átfogó védelmet nyújt a digitális fenyegetések ellen. Tartalmaz valós idejű vírusirtót, tűzfalat, adathalászat elleni védelmet és VPN-t.");
            testProduct.setPrice(new BigDecimal("15990.00"));
            testProduct.setStock(100);
            testProduct.setDurationMonths(12);
            testProduct.setDeviceLimit(5);
            testProduct.setImageUrl("https://placehold.co/600x400/a8e0f9/2a2a2a?text=Cyberpajzs_Pro_termek_kep");
            testProduct.setLicenseInfo("Ez egy digitális licenckulcs, amelyet emailben küldünk el a vásárlást követően. A kulcs 5 eszközre érvényes 12 hónapig.");
            productRepository.save(testProduct);
            System.out.println("Alapértelmezett teszt termék létrehozva: Cyberpajzs Pro");

            Product anotherTestProduct = new Product();
            anotherTestProduct.setName("CyberGuard Otthoni");
            anotherTestProduct.setDescription("Könnyen használható, mégis hatékony védelem otthoni felhasználóknak. Alapvető vírusvédelem és böngésző biztonság.");
            anotherTestProduct.setPrice(new BigDecimal("8990.00"));
            anotherTestProduct.setStock(50);
            anotherTestProduct.setDurationMonths(6);
            anotherTestProduct.setDeviceLimit(2);
            anotherTestProduct.setImageUrl("https://placehold.co/600x400/9fe2bf/2a2a2a?text=CyberGuard_Otthoni");
            anotherTestProduct.setLicenseInfo("6 hónapos licenc 2 eszközre. Ideális otthoni használatra.");
            productRepository.save(anotherTestProduct);
            System.out.println("Alapértelmezett teszt termék létrehozva: CyberGuard Otthoni");
        }
    }
}