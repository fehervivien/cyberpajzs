package com.example.cyberpajzs.controller;

import com.example.cyberpajzs.entity.Product;
import com.example.cyberpajzs.entity.User;
import com.example.cyberpajzs.entity.Order;
import com.example.cyberpajzs.entity.OrderType;
import com.example.cyberpajzs.service.ProductService;
import com.example.cyberpajzs.service.UserService;
import com.example.cyberpajzs.service.OrderService;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Arrays;
import java.util.Optional;


@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ProductService productService;
    private final UserService userService;
    private final OrderService orderService;

    public AdminController(ProductService productService, UserService userService, OrderService orderService) {
        this.productService = productService;
        this.userService = userService;
        this.orderService = orderService;
    }

    // --- Termékkezelés ---

    // Megjeleníti az összes termék listáját
    @GetMapping("/products")
    public String adminProducts(Model model) {
        model.addAttribute("products", productService.findAllProducts());
        return "admin/products"; // Az oldal, ami csak a listát mutatja
    }

    // Megjeleníti az ÜRES űrlapot új termék hozzáadásához
    @GetMapping("/products/new")
    public String showAddProductForm(Model model) {
        model.addAttribute("product", new Product()); // Üres termék objektum az űrlapnak
        return "admin/product-form"; // Az új sablon fájl
    }

    // Megjeleníti a MEGLÉVŐ termék szerkesztő űrlapját
    @GetMapping("/products/edit/{id}")
    public String showEditProductForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return productService.findProductById(id).map(product -> {
            model.addAttribute("product", product);
            return "admin/product-form"; // Az új sablon fájl
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("error", "Termék nem található ID: " + id);
            return "redirect:/admin/products"; // Vissza a listára, ha nem található a termék
        });
    }

    // Termék mentése (új hozzáadása vagy meglévő frissítése)
    @PostMapping("/products")
    public String saveProduct(@Valid @ModelAttribute("product") Product product, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            // Hiba esetén visszatérünk a form oldalra, a hibás adatokkal és üzenetekkel
            // Nem kell újra betölteni a terméklistát, mert ez a product-form.html
            return "admin/product-form";
        }
        try {
            productService.saveProduct(product);
            redirectAttributes.addFlashAttribute("success", "Termék sikeresen mentve!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Hiba a termék mentésekor: " + e.getMessage());
        }
        return "redirect:/admin/products"; // Sikeres mentés után vissza a terméklistára
    }

    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("success", "Termék sikeresen törölve!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Hiba a termék törlésekor: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    // --- Felhasználókezelés ---
    @GetMapping("/users")
    public String adminUsers(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        // Új felhasználó hozzáadásához üres objektum
        if (!model.containsAttribute("userToEdit")) {
            model.addAttribute("userToEdit", new User());
        }
        model.addAttribute("allRoles", Arrays.asList("ROLE_USER", "ROLE_ADMIN")); // Minden lehetséges szerepkör
        return "admin/users";
    }

    @GetMapping("/users/edit/{id}")
    public String editUser(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return userService.findUserById(id).map(user -> {
            model.addAttribute("userToEdit", user);
            model.addAttribute("users", userService.findAllUsers()); // Szükséges a lista megjelenítéséhez
            model.addAttribute("allRoles", Arrays.asList("ROLE_USER", "ROLE_ADMIN"));
            return "admin/users";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("error", "Felhasználó nem található ID: " + id);
            return "redirect:/admin/users";
        });
    }

    @PostMapping("/users")
    public String saveUser(@Valid @ModelAttribute("userToEdit") User user, BindingResult bindingResult, @RequestParam(value = "password", required = false) String password, @RequestParam(value = "roles", required = false) List<String> roles, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("users", userService.findAllUsers()); // Újra betöltjük a listát
            model.addAttribute("allRoles", Arrays.asList("ROLE_USER", "ROLE_ADMIN"));
            return "admin/users"; // Hiba esetén visszatérünk az űrlapra
        }
        try {
            if (user.getId() == null) { // Új felhasználó
                userService.registerNewUser(user.getUsername(), password, user.getEmail(), user.getFirstName(), user.getLastName());
            } else {
                userService.updateUser(user, password, roles);
            }
            redirectAttributes.addFlashAttribute("success", "Felhasználó sikeresen mentve!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            model.addAttribute("users", userService.findAllUsers()); // Újra betöltjük a listát
            model.addAttribute("allRoles", Arrays.asList("ROLE_USER", "ROLE_ADMIN"));
            return "admin/users"; // Hiba esetén visszatérünk az űrlapra
        }
        return "redirect:/admin/users";
    }


    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "Felhasználó sikeresen törölve!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Hiba a felhasználó törlésekor: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }


    // --- Rendeléskezelés ---
    @GetMapping("/orders")
    public String adminOrders(Model model) {
        model.addAttribute("orders", orderService.findAllOrders());
        return "admin/orders";
    }

    @GetMapping("/orders/{orderId}")
    public String adminOrderDetails(@PathVariable Long orderId, Model model, RedirectAttributes redirectAttributes) {
        Optional<Order> orderOptional = orderService.getOrderById(orderId);
        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            model.addAttribute("order", order);
            model.addAttribute("orderLicenses", orderService.getLicensesByOrderItem(order.getOrderItems()));
            model.addAttribute("allOrderStatuses", Arrays.asList("PENDING", "COMPLETED", "CANCELLED")); // Rendelés státuszok enum
            return "admin/order-details";
        } else {
            redirectAttributes.addFlashAttribute("error", "Rendelés nem található ID: " + orderId);
            return "redirect:/admin/orders";
        }
    }

    @PostMapping("/orders/update-status/{orderId}")
    public String updateOrderStatus(@PathVariable Long orderId, @RequestParam String newStatus, RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatus(orderId, newStatus);
            redirectAttributes.addFlashAttribute("statusSuccess", "Rendelés státusza sikeresen frissítve!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("statusError", e.getMessage());
        }
        return "redirect:/admin/orders/" + orderId;
    }
}