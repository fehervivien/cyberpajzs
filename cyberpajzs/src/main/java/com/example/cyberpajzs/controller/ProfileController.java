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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String showProfile(Model model, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // A bejelentkezett felhasználó neve

        Optional<User> userOptional = userService.findUserByUsername(username);

        if (userOptional.isPresent()) {
            model.addAttribute("user", userOptional.get());
        } else {
            // Ez elvileg nem fordulhat elő, ha a felhasználó be van jelentkezve
            redirectAttributes.addFlashAttribute("error", "Hiba: A felhasználói profil nem található.");
            return "redirect:/login";
        }
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute("user") User user,
                                @RequestParam(value = "newPassword", required = false) String newPassword,
                                RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<User> currentUserOptional = userService.findUserByUsername(username);
        if (currentUserOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Hiba: A felhasználói profil nem található.");
            return "redirect:/login";
        }

        user.setId(currentUserOptional.get().getId());

        try {
            userService.updateProfile(user, newPassword);
            redirectAttributes.addFlashAttribute("success", "Profil sikeresen frissítve!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Hiba történt a profil frissítése során.");
        }
        return "redirect:/profile";
    }
}