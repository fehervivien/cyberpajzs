package com.example.cyberpajzs.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OrderConfirmationController {

    @GetMapping("/order-confirmation")
    public String showOrderConfirmation(Model model) {
        return "order-confirmation";
    }
}
