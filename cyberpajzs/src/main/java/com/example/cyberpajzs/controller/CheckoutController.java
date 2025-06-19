package com.example.cyberpajzs.controller;

import com.example.cyberpajzs.entity.Order;
import com.example.cyberpajzs.entity.OrderType;
import com.example.cyberpajzs.entity.User;
import com.example.cyberpajzs.service.CartService;
import com.example.cyberpajzs.service.OrderService;
import com.example.cyberpajzs.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.Optional;

@Controller
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;
    private final UserService userService;

    public CheckoutController(CartService cartService, OrderService orderService, UserService userService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.userService = userService;
    }

    // Segédmetódus a felhasználó lekéréséhez
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        if (username.equals("anonymousUser")) {
            return null;
        }
        return userService.findUserByUsername(username).orElse(null);
    }

    @GetMapping("/checkout")
    public String showCheckout(Model model, HttpSession session) {
        User currentUser = getCurrentUser(); // Felhasználó lekérése

        model.addAttribute("cartItems", cartService.getCartItems(currentUser, session));
        model.addAttribute("cartTotal", cartService.calculateCartTotal(currentUser, session));
        model.addAttribute("orderTypes", OrderType.values());

        // Ha be van jelentkezve, töltsük ki a felhasználó adataival az űrlapot
        if (currentUser != null) {
            model.addAttribute("firstName", currentUser.getFirstName());
            model.addAttribute("lastName", currentUser.getLastName());
            model.addAttribute("email", currentUser.getEmail());
            model.addAttribute("phone", currentUser.getPhone());
            model.addAttribute("address", currentUser.getAddress());
            model.addAttribute("city", currentUser.getCity());
            model.addAttribute("zipCode", currentUser.getZipCode());
            model.addAttribute("country", currentUser.getCountry());
        }

        return "checkout";
    }

    @PostMapping("/checkout/place-order")
    public String placeOrder(@RequestParam String orderType,
                             @RequestParam(required = false) String companyName,
                             @RequestParam(required = false) String taxNumber,
                             @RequestParam String firstName,
                             @RequestParam String lastName,
                             @RequestParam String email,
                             @RequestParam String phone,
                             @RequestParam String address,
                             @RequestParam String city,
                             @RequestParam String zipCode,
                             @RequestParam String country,
                             RedirectAttributes redirectAttributes,
                             HttpSession session) {

        User currentUser = getCurrentUser(); // Felhasználó lekérése (lehet NULL vendég esetén)

        // ITT NINCS MÁR KIKÉNYSZERÍTVE A BEJELENTKEZÉS

        try {
            OrderType selectedOrderType = OrderType.valueOf(orderType.toUpperCase());
            Order order = orderService.placeOrder(
                    currentUser, // Átadjuk a felhasználót (lehet null)
                    selectedOrderType,
                    companyName,
                    taxNumber,
                    firstName, lastName, email, phone,
                    address, city, zipCode, country,
                    session
            );
            redirectAttributes.addFlashAttribute("success", "Rendelésedet sikeresen leadtuk! Rendelés ID: " + order.getId());
            return "redirect:/order-confirmation";
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/checkout";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Hiba történt a rendelés feldolgozása során.");
            return "redirect:/checkout";
        }
    }
}
