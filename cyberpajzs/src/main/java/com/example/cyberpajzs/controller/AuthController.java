package com.example.cyberpajzs.controller;

import com.example.cyberpajzs.service.UserService; // Fontos: Importáld a UserService-t
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Fontos: Importáld a RedirectAttributes-t

@Controller
public class AuthController {

    private final UserService userService; // Hozzáadva: UserService dependencia

    @Autowired // A Spring automatikusan injektálja a UserService-t a konstruktoron keresztül
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // Kezeli a bejelentkezési oldal megjelenítését (GET kérésre)
    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; // Megjeleníti a login.html sablont
    }

    // Kezeli a regisztrációs oldal megjelenítését (GET kérésre)
    @GetMapping("/register")
    public String showRegisterPage() {
        return "register"; // Megjeleníti a register.html sablont
    }

    // Kezeli a regisztrációs űrlap elküldését (POST kérésre)
    @PostMapping("/register")
    public String registerUser(@RequestParam String username, // Az űrlapról érkező "username" paraméter
                               @RequestParam String password, // Az űrlapról érkező "password" paraméter
                               @RequestParam String email,    // Az űrlapról érkező "email" paraméter
                               @RequestParam String firstName, // Az űrlapról érkező "firstName" paraméter
                               @RequestParam String lastName,  // Az űrlapról érkező "lastName" paraméter
                               RedirectAttributes redirectAttributes) { // Használjuk az üzenetek átadására redirect után

        try {
            // Felhasználó regisztrálása a UserService segítségével
            userService.registerNewUser(username, password, email, firstName, lastName);
            // Sikeres regisztráció esetén success üzenet és átirányítás a login oldalra
            redirectAttributes.addFlashAttribute("success", "Sikeresen regisztráltál! Most már bejelentkezhetsz.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            // Hiba esetén (pl. már létező felhasználónév/email) error üzenet és visszairányítás a regisztrációs oldalra
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        } catch (Exception e) {
            // Egyéb, váratlan hiba esetén általános hibaüzenet
            redirectAttributes.addFlashAttribute("error", "Ismeretlen hiba történt a regisztráció során.");
            return "redirect:/register";
        }
    }
}