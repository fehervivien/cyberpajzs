package com.example.cyberpajzs.controller;

import com.example.cyberpajzs.entity.User; // Importálva
import com.example.cyberpajzs.service.CartService;
import com.example.cyberpajzs.service.UserService; // Importálva
import org.springframework.security.core.Authentication; // Importálva
import org.springframework.security.core.context.SecurityContextHolder; // Importálva
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.Optional; // Importálva

@Controller
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    public CartController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    // Segédmetódus a felhasználó lekéréséhez
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        if (username.equals("anonymousUser")) {
            return null; // Anonim felhasználó esetén null
        }
        return userService.findUserByUsername(username).orElse(null);
    }

    @GetMapping("/cart")
    public String viewCart(Model model, HttpSession session) {
        User currentUser = getCurrentUser(); // Felhasználó lekérése
        model.addAttribute("cartItems", cartService.getCartItems(currentUser, session)); // currentUser és session átadva
        model.addAttribute("cartTotal", cartService.calculateCartTotal(currentUser, session)); // currentUser és session átadva
        return "cart";
    }

    @PostMapping("/cart/add")
    public String addProductToCart(@RequestParam("productId") Long productId,
                                   @RequestParam("quantity") int quantity,
                                   RedirectAttributes redirectAttributes,
                                   HttpSession session) {
        User currentUser = getCurrentUser(); // Felhasználó lekérése
        try {
            cartService.addProductToCart(productId, quantity, currentUser, session); // currentUser és session átadva
            redirectAttributes.addFlashAttribute("success", "Termék hozzáadva a kosárhoz!");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Hiba történt a termék kosárhoz adásakor.");
        }
        return "redirect:/product-details/" + productId;
    }

    @PostMapping("/cart/update")
    public String updateCartItemQuantity(@RequestParam("productId") Long productId,
                                         @RequestParam("newQuantity") int newQuantity,
                                         RedirectAttributes redirectAttributes,
                                         HttpSession session) {
        User currentUser = getCurrentUser(); // Felhasználó lekérése
        try {
            cartService.updateCartItemQuantity(productId, newQuantity, currentUser, session); // currentUser és session átadva
            redirectAttributes.addFlashAttribute("success", "Kosár frissítve!");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Hiba történt a kosár frissítésekor.");
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove")
    public String removeProductFromCart(@RequestParam("productId") Long productId,
                                        RedirectAttributes redirectAttributes,
                                        HttpSession session) {
        User currentUser = getCurrentUser(); // Felhasználó lekérése
        try {
            cartService.removeProductFromCart(productId, currentUser, session); // currentUser és session átadva
            redirectAttributes.addFlashAttribute("success", "Termék eltávolítva a kosárból.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Hiba történt a termék eltávolításakor.");
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/clear")
    public String clearCart(RedirectAttributes redirectAttributes,
                            HttpSession session) {
        User currentUser = getCurrentUser(); // Felhasználó lekérése
        try {
            cartService.clearCart(currentUser, session); // currentUser és session átadva
            redirectAttributes.addFlashAttribute("success", "A kosár kiürítve.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Hiba történt a kosár kiürítésekor.");
        }
        return "redirect:/cart";
    }
}
