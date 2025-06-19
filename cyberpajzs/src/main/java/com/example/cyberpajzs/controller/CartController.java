package com.example.cyberpajzs.controller;

import com.example.cyberpajzs.service.CartService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/cart")
    public String viewCart(Model model) {
        model.addAttribute("cartItems", cartService.getCartItems());
        model.addAttribute("cartTotal", cartService.calculateCartTotal());
        return "cart";
    }

    @PostMapping("/cart/add")
    public String addProductToCart(@RequestParam("productId") Long productId,
                                   @RequestParam("quantity") int quantity,
                                   RedirectAttributes redirectAttributes) {
        try {
            cartService.addProductToCart(productId, quantity); // productId átadása
            redirectAttributes.addFlashAttribute("success", "Termék hozzáadva a kosárhoz!");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Hiba történt a termék kosárhoz adásakor.");
        }
        // Átirányítás vissza a termék részletei oldalra
        return "redirect:/product-details/" + productId;
    }

    @PostMapping("/cart/update")
    public String updateCartItemQuantity(@RequestParam("productId") Long productId,
                                         @RequestParam("newQuantity") int newQuantity,
                                         RedirectAttributes redirectAttributes) {
        try {
            cartService.updateCartItemQuantity(productId, newQuantity);
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
                                        RedirectAttributes redirectAttributes) {
        try {
            cartService.removeProductFromCart(productId);
            redirectAttributes.addFlashAttribute("success", "Termék eltávolítva a kosárból.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Hiba történt a termék eltávolításakor.");
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/clear")
    public String clearCart(RedirectAttributes redirectAttributes) {
        try {
            cartService.clearCart();
            redirectAttributes.addFlashAttribute("success", "A kosár kiürítve.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Hiba történt a kosár kiürítésekor.");
        }
        return "redirect:/cart";
    }
}
