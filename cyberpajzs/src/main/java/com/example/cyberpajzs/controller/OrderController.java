package com.example.cyberpajzs.controller;

import com.example.cyberpajzs.entity.Order;
import com.example.cyberpajzs.entity.User;
import com.example.cyberpajzs.service.OrderService;
import com.example.cyberpajzs.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

@Controller
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    public OrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping("/orders")
    public String showOrderHistory(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        User currentUser = userService.findUserByUsername(currentUsername)
                .orElseThrow(() -> new IllegalStateException("Bejelentkezett felhasználó nem található."));

        List<Order> orders = orderService.getUserOrders(currentUser);
        model.addAttribute("orders", orders);

        return "order-history";
    }

    @GetMapping("/orders/{orderId}")
    public String showOrderDetails(@PathVariable Long orderId, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        User currentUser = userService.findUserByUsername(currentUsername)
                .orElseThrow(() -> new IllegalStateException("Bejelentkezett felhasználó nem található."));

        Optional<Order> orderOptional = orderService.getOrderById(orderId);

        if (orderOptional.isPresent() && orderOptional.get().getUser().getId().equals(currentUser.getId())) {
            model.addAttribute("order", orderOptional.get());
            return "order-details";
        } else {
            return "redirect:/orders?error=orderNotFound";
        }
    }
}