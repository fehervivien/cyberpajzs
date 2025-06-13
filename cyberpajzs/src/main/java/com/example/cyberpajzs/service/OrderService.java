package com.example.cyberpajzs.service;

import com.example.cyberpajzs.entity.CartItem;
import com.example.cyberpajzs.entity.Order;
import com.example.cyberpajzs.entity.OrderItem;
import com.example.cyberpajzs.entity.User;
import com.example.cyberpajzs.repository.OrderItemRepository;
import com.example.cyberpajzs.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartService cartService;
    private final LicenseService licenseService;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                        CartService cartService, LicenseService licenseService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartService = cartService;
        this.licenseService = licenseService;
    }

    @Transactional
    public Order placeOrder(User user, String firstName, String lastName, String email, String phone,
                            String address, String city, String zipCode, String country) {

        List<CartItem> cartItems = cartService.getCartItems();
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("A kosár üres, nem lehet megrendelést leadni.");
        }

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");

        order.setFirstName(firstName);
        order.setLastName(lastName);
        order.setEmail(email);
        order.setPhone(phone);
        order.setAddress(address);
        order.setCity(city);
        order.setZipCode(zipCode);
        order.setCountry(country);

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtOrder(cartItem.getProduct().getPrice());

            orderItems.add(orderItem);
            totalAmount = totalAmount.add(orderItem.getPriceAtOrder().multiply(BigDecimal.valueOf(orderItem.getQuantity())));

            for (int i = 0; i < cartItem.getQuantity(); i++) {
                licenseService.assignLicenseToUser(
                        cartItem.getProduct(),
                        user,
                        orderItem,
                        LocalDateTime.now()
                );
            }
        }

        order.setTotalAmount(totalAmount);
        order.setOrderItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        cartService.clearCart();

        return savedOrder;
    }

    public List<Order> getUserOrders(User user) {
        return orderRepository.findByUser(user);
    }

    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    public List<Order> findAllOrders() {
        return orderRepository.findAll();
    }

    // Rendelés státuszának frissítése
    @Transactional
    public void updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Rendelés nem található ID: " + orderId));
        order.setStatus(newStatus);
        orderRepository.save(order);
    }
}