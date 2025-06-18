package com.example.cyberpajzs.controller;

import com.example.cyberpajzs.entity.User;
import com.example.cyberpajzs.service.ProductService;
import com.example.cyberpajzs.service.QuoteService;
import com.example.cyberpajzs.service.UserService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class QuoteController {

    private static final Logger logger = LoggerFactory.getLogger(QuoteController.class);

    private final ProductService productService;
    private final QuoteService quoteService;
    private final UserService userService;

    public QuoteController(ProductService productService, QuoteService quoteService, UserService userService) {
        this.productService = productService;
        this.quoteService = quoteService;
        this.userService = userService;
    }

    @GetMapping("/request-quote/{productId}")
    public String showQuoteRequestForm(@PathVariable Long productId, Model model, RedirectAttributes redirectAttributes) {
        logger.info("Árajánlatkérő űrlap kérése a termék ID-vel: {}", productId);

        Optional<com.example.cyberpajzs.entity.Product> productOptional = productService.findProductById(productId);

        if (productOptional.isPresent()) {
            com.example.cyberpajzs.entity.Product product = productOptional.get();
            logger.info("Termék megtalálva: {} (ID: {})", product.getName(), productId);
            model.addAttribute("product", product);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = null;

            if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
                String username = authentication.getName();
                Optional<User> currentUserOptional = userService.findUserByUsername(username);
                currentUser = currentUserOptional.orElse(null);
                logger.info("Bejelentkezett felhasználó: {}", username);
            } else {
                logger.info("Anonim (vendég) felhasználó.");
            }
            model.addAttribute("user", currentUser != null ? currentUser : new User());
            return "request-quote";
        } else {
            logger.warn("Termék (ID: {}) nem található az árajánlatkéréshez. Átirányítás a terméklistára.", productId);
            redirectAttributes.addFlashAttribute("error", "A megadott termék nem található.");
            return "redirect:/product-list";
        }
    }

    @PostMapping("/request-quote/{productId}")
    public String submitQuoteRequest(@PathVariable Long productId,
                                     @RequestParam String name,
                                     @RequestParam String email,
                                     @RequestParam(required = false) String phone,
                                     @RequestParam(required = false) String companyName,
                                     @RequestParam String message,
                                     RedirectAttributes redirectAttributes) {
        logger.info("Árajánlatkérés elküldése termék ID-vel: {}", productId);
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = null;

            if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
                String username = authentication.getName();
                Optional<User> currentUserOptional = userService.findUserByUsername(username);
                currentUser = currentUserOptional.orElse(null);
            }

            quoteService.createQuoteRequest(productId, currentUser, name, email, phone, companyName, message);
            logger.info("Árajánlatkérés sikeresen elküldve a termék ID-vel: {}", productId);
            redirectAttributes.addFlashAttribute("success", "Árajánlatkérésedet sikeresen elküldtük! Hamarosan felvesszük veled a kapcsolatot.");
            return "redirect:/product-details/" + productId;
        } catch (IllegalArgumentException e) {
            logger.error("Hiba az árajánlatkérés során: {}", e.getMessage());
            return "redirect:/request-quote/" + productId;
        } catch (Exception e) {
            logger.error("Ismeretlen hiba az árajánlatkérés elküldésekor termék ID-vel {}: {}", productId, e.getMessage(), e);
            return "redirect:/request-quote/" + productId;
        }
    }
}