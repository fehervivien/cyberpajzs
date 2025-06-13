package com.example.cyberpajzs.controller;

import com.example.cyberpajzs.entity.User;
import com.example.cyberpajzs.service.CartService;
import com.example.cyberpajzs.service.OrderService;
import com.example.cyberpajzs.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @GetMapping("/checkout")
    public String showCheckout(Model model, RedirectAttributes redirectAttributes) {
        if (cartService.getCartItems().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "A kosarad üres. Nincs mit megrendelni.");
            return "redirect:/cart";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<User> currentUserOptional = userService.findUserByUsername(username);

        if (currentUserOptional.isPresent()) {
            User currentUser = currentUserOptional.get();
            model.addAttribute("user", currentUser);
        } else {
            redirectAttributes.addFlashAttribute("error", "Hiba: Bejelentkezett felhasználó nem található.");
            return "redirect:/login";
        }

        model.addAttribute("cartItems", cartService.getCartItems());
        model.addAttribute("cartTotal", cartService.calculateCartTotal());

        return "checkout";
    }

    @PostMapping("/checkout/place-order")
    public String placeOrder(@RequestParam String firstName,
                             @RequestParam String lastName,
                             @RequestParam String email,
                             @RequestParam(required = false) String phone,
                             @RequestParam String address,
                             @RequestParam String city,
                             @RequestParam String zipCode,
                             @RequestParam String country,
                             RedirectAttributes redirectAttributes) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userService.findUserByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Hiba: Bejelentkezett felhasználó nem található a rendelés leadásakor."));

        try {
            orderService.placeOrder(currentUser, firstName, lastName, email, phone, address, city, zipCode, country);
            redirectAttributes.addFlashAttribute("success", "Sikeresen leadtad a megrendelésedet!");
            return "redirect:/order-confirmation";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/checkout";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ismeretlen hiba történt a megrendelés leadásakor: " + e.getMessage());
            return "redirect:/checkout";
        }
    }
}