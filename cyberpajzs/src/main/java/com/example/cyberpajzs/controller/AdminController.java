package com.example.cyberpajzs.controller;

import com.example.cyberpajzs.entity.Product;
import com.example.cyberpajzs.entity.Order;
import com.example.cyberpajzs.entity.User;
import com.example.cyberpajzs.service.ProductService;
import com.example.cyberpajzs.service.UserService;
import com.example.cyberpajzs.service.OrderService;
import com.example.cyberpajzs.service.LicenseService; // Importálva a LicenseService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ProductService productService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final OrderService orderService;
    private final LicenseService licenseService; // Hozzáadva

    @Autowired
    public AdminController(ProductService productService, UserService userService,
                           PasswordEncoder passwordEncoder, OrderService orderService,
                           LicenseService licenseService) { // Konstruktor frissítése
        this.productService = productService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.orderService = orderService;
        this.licenseService = licenseService; // Injektálás
    }

    // --- Termékkezelés ---
    @GetMapping("/products")
    public String showAdminProducts(Model model) {
        model.addAttribute("products", productService.findAllProducts());
        if (!model.containsAttribute("product")) {
            model.addAttribute("product", new Product());
        }
        model.addAttribute("hasLicenseInfoField", true);
        return "admin/products";
    }

    @GetMapping("/products/edit/{id}")
    public String editProduct(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Product> productOptional = productService.findProductById(id);
        if (productOptional.isPresent()) {
            model.addAttribute("product", productOptional.get());
            model.addAttribute("products", productService.findAllProducts());
            model.addAttribute("hasLicenseInfoField", true);
            return "admin/products";
        } else {
            redirectAttributes.addFlashAttribute("error", "A termék nem található.");
            return "redirect:/admin/products";
        }
    }

    @PostMapping("/products")
    public String saveOrUpdateProduct(@ModelAttribute Product product, RedirectAttributes redirectAttributes) {
        try {
            productService.saveProduct(product);
            redirectAttributes.addFlashAttribute("success", "Termék sikeresen mentve!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Hiba történt a termék mentésekor: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProductById(id);
            redirectAttributes.addFlashAttribute("success", "Termék sikeresen törölve!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Hiba történt a termék törlésekor: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }


    // --- Felhasználókezelés ---
    @GetMapping("/users")
    public String showAdminUsers(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        if (!model.containsAttribute("userToEdit")) {
            model.addAttribute("userToEdit", new User());
        }
        model.addAttribute("allRoles", Arrays.asList("ROLE_USER", "ROLE_ADMIN"));
        return "admin/users";
    }

    @GetMapping("/users/edit/{id}")
    public String editUser(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<User> userOptional = userService.findUserById(id);
        if (userOptional.isPresent()) {
            model.addAttribute("userToEdit", userOptional.get());
            model.addAttribute("users", userService.findAllUsers());
            model.addAttribute("allRoles", Arrays.asList("ROLE_USER", "ROLE_ADMIN"));
            return "admin/users";
        } else {
            redirectAttributes.addFlashAttribute("error", "A felhasználó nem található.");
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/users")
    public String saveOrUpdateUser(@ModelAttribute("userToEdit") User user,
                                   @RequestParam(value = "password", required = false) String password,
                                   @RequestParam(value = "roles", required = false) List<String> roles,
                                   RedirectAttributes redirectAttributes) {
        try {
            if (user.getId() == null) {
                if (password == null || password.isEmpty()) {
                    throw new IllegalArgumentException("Az új felhasználóhoz jelszó megadása kötelező.");
                }
                userService.registerNewUser(user.getUsername(), password, user.getEmail(), user.getFirstName(), user.getLastName());
                if (roles != null) {
                    User newUser = userService.findUserByUsername(user.getUsername())
                            .orElseThrow(() -> new RuntimeException("Regisztrált felhasználó nem található."));
                    Set<String> newRoles = new HashSet<>(roles);
                    if (!newRoles.contains("ROLE_USER")) {
                        newRoles.add("ROLE_USER");
                    }
                    newUser.setRoles(newRoles);
                    userService.saveUser(newUser);
                }

                redirectAttributes.addFlashAttribute("success", "Felhasználó sikeresen hozzáadva!");
            } else {
                User existingUser = userService.findUserById(user.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Felhasználó nem található ID: " + user.getId()));

                existingUser.setEmail(user.getEmail());
                existingUser.setFirstName(user.getFirstName());
                existingUser.setLastName(user.getLastName());

                if (password != null && !password.isEmpty()) {
                    existingUser.setPassword(passwordEncoder.encode(password));
                }

                if (roles != null) {
                    Set<String> newRoles = new HashSet<>(roles);
                    if (!newRoles.contains("ROLE_USER")) {
                        newRoles.add("ROLE_USER");
                    }
                    existingUser.setRoles(newRoles);
                } else {
                    existingUser.setRoles(new HashSet<>(Arrays.asList("ROLE_USER")));
                }

                userService.saveUser(existingUser);
                redirectAttributes.addFlashAttribute("success", "Felhasználó sikeresen frissítve!");
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("userToEdit", user);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Hiba történt a felhasználó mentésekor: " + e.getMessage());
            redirectAttributes.addFlashAttribute("userToEdit", user);
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUserById(id);
            redirectAttributes.addFlashAttribute("success", "Felhasználó sikeresen törölve!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Hiba történt a felhasználó törlésekor: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // --- Rendeléskezelés ---

    @GetMapping("/orders")
    public String showAdminOrders(Model model) {
        model.addAttribute("orders", orderService.findAllOrders());
        return "admin/orders";
    }

    @GetMapping("/orders/{orderId}")
    public String showAdminOrderDetails(@PathVariable Long orderId, Model model, RedirectAttributes redirectAttributes) {
        Optional<Order> orderOptional = orderService.getOrderById(orderId);
        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            model.addAttribute("order", order);
            // Lekérjük a rendeléshez tartozó licenceket is
            model.addAttribute("orderLicenses", licenseService.getLicensesByOrderItem(order.getOrderItems())); // Ezt a metódust hozzáadjuk a LicenseService-be
            model.addAttribute("allOrderStatuses", Arrays.asList("PENDING", "COMPLETED", "CANCELLED")); // Rendelés státuszok
            return "admin/order-details";
        } else {
            redirectAttributes.addFlashAttribute("error", "A rendelés nem található.");
            return "redirect:/admin/orders";
        }
    }

    @PostMapping("/orders/update-status/{orderId}")
    public String updateOrderStatus(@PathVariable Long orderId,
                                    @RequestParam String newStatus,
                                    RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatus(orderId, newStatus); // Ezt a metódust hozzáadjuk az OrderService-be
            redirectAttributes.addFlashAttribute("statusSuccess", "Rendelés státusza sikeresen frissítve!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("statusError", "Hiba történt a státusz frissítésekor: " + e.getMessage());
        }
        return "redirect:/admin/orders/" + orderId; // Vissza az adott rendelés részleteihez
    }
}