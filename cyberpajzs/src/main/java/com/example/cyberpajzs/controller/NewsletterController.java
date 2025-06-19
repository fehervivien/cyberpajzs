package com.example.cyberpajzs.controller;

import com.example.cyberpajzs.service.NewsletterService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller // Spring kontrollerként jelölve
public class NewsletterController {

    private final NewsletterService newsletterService;

    // Konstruktor injekció a szolgáltatáshoz
    public NewsletterController(NewsletterService newsletterService) {
        this.newsletterService = newsletterService;
    }

    /**
     * Kezeli a hírlevél feliratkozási űrlapról érkező POST kéréseket.
     *
     * @param email A feliratkozni kívánt e-mail cím.
     * @param redirectAttributes Az átirányításokhoz használt Flash attribútumok.
     * @return Átirányítás a főoldalra, siker/hiba üzenettel.
     */
    @PostMapping("/newsletter/subscribe") // POST kérés leképezése az URL-re
    public String subscribeToNewsletter(@RequestParam String email, RedirectAttributes redirectAttributes) {
        try {
            newsletterService.subscribe(email); // Hívjuk a szolgáltatási réteget
            // Sikeres üzenet hozzáadása Flash attribútumként
            redirectAttributes.addFlashAttribute("success", "Sikeresen feliratkoztál a hírlevelünkre!");
        } catch (IllegalArgumentException e) {
            // Hibaüzenet hozzáadása, ha az e-mail már fel van iratkozva
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            // Általános hibaüzenet
            redirectAttributes.addFlashAttribute("error", "Hiba történt a feliratkozás során.");
        }
        // Átirányítás a főoldalra
        return "redirect:/";
    }
}
