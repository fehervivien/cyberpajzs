package com.example.cyberpajzs.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

@Controller
public class OrderConfirmationController {

    @GetMapping("/order-confirmation")
    public String showOrderConfirmation(Model model) {
        return "order-confirmation";
    }
}