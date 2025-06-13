package com.example.cyberpajzs.controller;

import com.example.cyberpajzs.entity.User;
import com.example.cyberpajzs.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String showProfile(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        User currentUser = userService.findUserByUsername(currentUsername)
                .orElseThrow(() -> new IllegalStateException("Bejelentkezett felhasználó nem található."));

        model.addAttribute("user", currentUser);
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        User currentUser = userService.findUserByUsername(currentUsername)
                .orElseThrow(() -> new IllegalStateException("Bejelentkezett felhasználó nem található."));

        try {
            // Frissítjük a felhasználó adatait (email, firstName, lastName).
            // A felhasználónév nem módosítható.
            currentUser.setEmail(user.getEmail());
            currentUser.setFirstName(user.getFirstName());
            currentUser.setLastName(user.getLastName());
            userService.saveUser(currentUser); // Menti a frissített felhasználót

            redirectAttributes.addFlashAttribute("success", "Profil sikeresen frissítve!");
            return "redirect:/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Hiba történt a profil frissítése során: " + e.getMessage());
            return "redirect:/profile";
        }
    }
}