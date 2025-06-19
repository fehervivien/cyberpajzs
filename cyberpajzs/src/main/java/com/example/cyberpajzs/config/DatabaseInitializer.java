package com.example.cyberpajzs.config;

import com.example.cyberpajzs.entity.NewsArticle;
import com.example.cyberpajzs.entity.Product;
import com.example.cyberpajzs.entity.User;
import com.example.cyberpajzs.repository.NewsArticleRepository;
import com.example.cyberpajzs.repository.ProductRepository;
import com.example.cyberpajzs.repository.UserRepository;
import com.example.cyberpajzs.repository.LicenseRepository; // UJ IMPORT
import com.example.cyberpajzs.service.LicenseService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProductRepository productRepository;
    private final NewsArticleRepository newsArticleRepository;
    private final LicenseService licenseService;
    private final LicenseRepository licenseRepository;

    public DatabaseInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder,
                               ProductRepository productRepository, NewsArticleRepository newsArticleRepository,
                               LicenseService licenseService, LicenseRepository licenseRepository) { // UJ PARAMÉTER
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.productRepository = productRepository;
        this.newsArticleRepository = newsArticleRepository;
        this.licenseService = licenseService;
        this.licenseRepository = licenseRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Ellenőrizzük az admin felhasználót
        Optional<User> adminUserOptional = userRepository.findByUsername("admin");

        if (adminUserOptional.isEmpty()) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@cyberpajzs.hu");
            adminUser.setFirstName("Admin");
            adminUser.setLastName("Felhasználó");
            adminUser.setPassword(passwordEncoder.encode("password"));

            adminUser.setPhone("+36 30 123 4567");
            adminUser.setAddress("Kossuth Lajos utca 1.");
            adminUser.setCity("Budapest");
            adminUser.setZipCode("1011");
            adminUser.setCountry("Magyarország");

            Set<String> roles = new HashSet<>();
            roles.add("ROLE_USER");
            roles.add("ROLE_ADMIN");
            adminUser.setRoles(roles);

            userRepository.save(adminUser);
            System.out.println("Alapértelmezett admin felhasználó létrehozva: admin / password");
        } else {
            User adminUser = adminUserOptional.get();
            boolean changed = false;
            Set<String> currentRoles = adminUser.getRoles();

            if (!currentRoles.contains("ROLE_USER")) {
                currentRoles.add("ROLE_USER");
                changed = true;
            }
            if (!currentRoles.contains("ROLE_ADMIN")) {
                currentRoles.add("ROLE_ADMIN");
                changed = true;
            }

            if (adminUser.getPhone() == null || adminUser.getPhone().isEmpty()) {
                adminUser.setPhone("+36 30 123 4567");
                changed = true;
            }
            if (adminUser.getAddress() == null || adminUser.getAddress().isEmpty()) {
                adminUser.setAddress("Kossuth Lajos utca 1.");
                changed = true;
            }
            if (adminUser.getCity() == null || adminUser.getCity().isEmpty()) {
                adminUser.setCity("Budapest");
                changed = true;
            }
            if (adminUser.getZipCode() == null || adminUser.getZipCode().isEmpty()) {
                adminUser.setZipCode("1011");
                changed = true;
            }
            if (adminUser.getCountry() == null || adminUser.getCountry().isEmpty()) {
                adminUser.setCountry("Magyarország");
                changed = true;
            }


            if (changed) {
                adminUser.setRoles(currentRoles);
                userRepository.save(adminUser);
                System.out.println("Admin felhasználó szerepkörei és/vagy adatai frissítve.");
            } else {
                System.out.println("Admin felhasználó már létezik és szerepkörei rendben vannak.");
            }
        }

        // Ellenőrizzük, hogy vannak-e már termékek ÉS licencek
        // Ha valamilyen termék létezik, de nincs hozzá licenc, akkor újra generálunk mindent.
        if (productRepository.count() == 0 || licenseRepository.count() == 0) { // <-- licenseRepository.count() hívás
            System.out.println("Termékek és/vagy licencek inicializálása...");

            // Biztosítjuk, hogy tiszta lappal induljunk a termékek és licencek terén
            licenseRepository.deleteAll();
            productRepository.deleteAll();

            Product testProduct = new Product();
            testProduct.setName("Cyberpajzs Pro");
            testProduct.setDescription("A Cyberpajzs Pro egy fejlett biztonsági szoftver, amely átfogó védelmet nyújt a digitális fenyegetések ellen. Tartalmaz valós idejű vírusirtót, tűzfalat, adathalászat elleni védelmet és VPN-t.");
            testProduct.setPrice(new BigDecimal("15990.00"));
            testProduct.setStock(100);
            testProduct.setDurationMonths(12);
            testProduct.setDeviceLimit(5);
            testProduct.setImageUrl("https://placehold.co/600x400/a8e0f9/2a2a2a?text=Cyberpajzs_Pro_termek_kep");
            testProduct.setLicenseInfo("Ez egy digitális licenckulcs, amelyet emailben küldünk el a vásárlást követően. A kulcs 5 eszközre érvényes 12 hónapig.");
            Product savedProduct1 = productRepository.save(testProduct);

            licenseService.generateLicensesForProduct(savedProduct1, 20);
            System.out.println("Cyberpajzs Pro termék és licencek létrehozva.");

            Product anotherTestProduct = new Product();
            anotherTestProduct.setName("CyberGuard Otthoni");
            anotherTestProduct.setDescription("Könnyen használható, mégis hatékony védelem otthoni felhasználóknak. Alapvető vírusvédelem és böngésző biztonság.");
            anotherTestProduct.setPrice(new BigDecimal("8990.00"));
            anotherTestProduct.setStock(50);
            anotherTestProduct.setDurationMonths(6);
            anotherTestProduct.setDeviceLimit(2);
            anotherTestProduct.setImageUrl("https://placehold.co/600x400/9fe2bf/2a2a2a?text=CyberGuard_Otthoni");
            anotherTestProduct.setLicenseInfo("6 hónapos licenc 2 eszközre. Ideális otthoni használatra.");
            Product savedProduct2 = productRepository.save(anotherTestProduct);

            licenseService.generateLicensesForProduct(savedProduct2, 10);
            System.out.println("CyberGuard Otthoni termék és licencek létrehozva.");

        } else {
            System.out.println("Termékek és licencek már léteznek az adatbázisban.");
        }

        // Teszt hírcikkek inicializálása
        if (newsArticleRepository.count() == 0) {
            NewsArticle news1 = new NewsArticle();
            news1.setTitle("Új generációs tűzfal érkezett!");
            news1.setContent("Büszkén jelentjük be, hogy webshopunk kínálata bővült a forradalmi 'ShieldGuard 2025' tűzfallal. Ez a szoftver mesterséges intelligencia alapú védelemmel óvja hálózatát a legújabb fenyegetések ellen. Ne hagyja ki!");
            news1.setPublicationDate(LocalDateTime.now().minusDays(5));
            newsArticleRepository.save(news1);

            NewsArticle news2 = new NewsArticle();
            news2.setTitle("Nyári akciók a Cyberpajzsnál!");
            news2.setContent("Készülj fel a nyári utazásokra és a távoli munkára biztonságban! Akár 30% kedvezmény a kiválasztott VPN szolgáltatásokra és mobilbiztonsági alkalmazásokra. Látogass el a termékek oldalra!");
            news2.setPublicationDate(LocalDateTime.now().minusDays(10));
            newsArticleRepository.save(news2);

            NewsArticle news3 = new NewsArticle();
            news3.setTitle("Adathalászat elleni tippek a Cyberpajzstól");
            news3.setContent("Az adathalászat (phishing) továbbra is az egyik leggyakoribb online fenyegetés. Blogbejegyzésünkben összegyűjtöttük a legjobb tippeket, hogyan ismerheti fel és kerülheti el az adathalász támadásokat. Legyen naprakész és biztonságban!");
            news3.setPublicationDate(LocalDateTime.now().minusDays(15));
            newsArticleRepository.save(news3);

            System.out.println("Alapértelmezett hírcikkek létrehozva.");
        } else {
            System.out.println("Hírcikkek már léteznek az adatbázisban.");
        }
    }
}
