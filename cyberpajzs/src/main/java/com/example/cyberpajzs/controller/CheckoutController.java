package com.example.cyberpajzs.controller;

import com.example.cyberpajzs.entity.OrderType;
import com.example.cyberpajzs.entity.User;
import com.example.cyberpajzs.service.CartService;
import com.example.cyberpajzs.service.OrderService;
import com.example.cyberpajzs.service.UserService;
import org.springframework.security.authentication.AnonymousAuthenticationToken; // Importálva
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        User currentUser = null;

        // Ellenőrizzük, hogy a felhasználó be van-e jelentkezve (nem anonim)
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
            String username = authentication.getName();
            Optional<User> currentUserOptional = userService.findUserByUsername(username);
            currentUser = currentUserOptional.orElse(null); // Ha valamiért nem található, akkor is null
        }
        // Ha be van jelentkezve, előtöltjük az adatait, különben üres User objektum
        model.addAttribute("user", currentUser != null ? currentUser : new User());

        model.addAttribute("cartItems", cartService.getCartItems());
        model.addAttribute("cartTotal", cartService.calculateCartTotal());

        return "checkout";
    }

    @PostMapping("/checkout/place-order")
    public String placeOrder(@RequestParam String orderType,
                             @RequestParam(required = false) String companyName,
                             @RequestParam(required = false) String taxNumber,
                             @RequestParam String firstName,
                             @RequestParam String lastName,
                             @RequestParam String email,
                             @RequestParam(required = false) String phone,
                             @RequestParam String address,
                             @RequestParam String city,
                             @RequestParam String zipCode,
                             @RequestParam String country,
                             RedirectAttributes redirectAttributes) {

        User currentUser = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Ha a felhasználó be van jelentkezve (nem anonim), akkor lekérjük az User objektumát
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
            String username = authentication.getName();
            currentUser = userService.findUserByUsername(username).orElse(null);
        }

        try {
            // Validáció céges vásárlás esetén
            if (OrderType.COMPANY.name().equals(orderType)) {
                if (companyName == null || companyName.trim().isEmpty()) {
                    throw new IllegalArgumentException("Céges vásárlás esetén a cégnév megadása kötelező.");
                }
                if (taxNumber == null || taxNumber.trim().isEmpty()) {
                    throw new IllegalArgumentException("Céges vásárlás esetén az adószám megadása kötelező.");
                }
            }

            orderService.placeOrder(
                    //lehet null, ha a felhasználó nincs bejelentkezve
                    currentUser,
                    OrderType.valueOf(orderType),
                    companyName,
                    taxNumber,
                    firstName,
                    lastName,
                    email,
                    phone,
                    address,
                    city,
                    zipCode,
                    country
            );
            redirectAttributes.addFlashAttribute("success", "Sikeresen leadtad a megrendelésedet!");
            return "redirect:/order-confirmation";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/checkout";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/checkout";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ismeretlen hiba történt a megrendelés leadásakor: " + e.getMessage());
            return "redirect:/checkout";
        }
    }
}