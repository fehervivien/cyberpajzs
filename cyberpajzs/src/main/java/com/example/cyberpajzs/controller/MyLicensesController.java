package com.example.cyberpajzs.controller;

import com.example.cyberpajzs.entity.User;
import com.example.cyberpajzs.service.LicenseService;
import com.example.cyberpajzs.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;

@Controller
public class MyLicensesController {

    private final LicenseService licenseService;
    private final UserService userService;

    public MyLicensesController(LicenseService licenseService, UserService userService) {
        this.licenseService = licenseService;
        this.userService = userService;
    }

    @GetMapping("/my-licenses")
    public String showMyLicenses(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User currentUser = userService.findUserByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Bejelentkezett felhasználó nem található."));

        model.addAttribute("licenses", licenseService.getUserLicenses(currentUser));
        return "my-licenses";
    }
}