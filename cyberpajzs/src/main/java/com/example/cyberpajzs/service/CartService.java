package com.example.cyberpajzs.service;

import com.example.cyberpajzs.entity.CartItem;
import com.example.cyberpajzs.entity.Product;
import com.example.cyberpajzs.entity.User;
import com.example.cyberpajzs.repository.CartItemRepository;
import com.example.cyberpajzs.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    // HttpSession kulcs a vendégkosárhoz
    private static final String GUEST_CART_SESSION_KEY = "guestCart";

    public CartService(CartItemRepository cartItemRepository, ProductRepository productRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public List<CartItem> addProductToCart(Long productId, int quantity, User user, HttpSession session) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Termék nem található ID: " + productId));

        if (user != null) {
            // Bejelentkezett felhasználó kosara
            Optional<CartItem> existingCartItem = cartItemRepository.findByUserAndProduct(user, product);
            if (existingCartItem.isPresent()) {
                CartItem cartItem = existingCartItem.get();
                cartItem.setQuantity(cartItem.getQuantity() + quantity);
                cartItem.setPriceAtOrder(product.getPrice());
                cartItemRepository.save(cartItem);
            } else {
                CartItem newCartItem = new CartItem();
                newCartItem.setUser(user);
                newCartItem.setProduct(product);
                newCartItem.setQuantity(quantity);
                newCartItem.setPriceAtOrder(product.getPrice());
                cartItemRepository.save(newCartItem);
            }
            return cartItemRepository.findByUser(user);
        } else {
            // Vendég kosara (HttpSession alapú)
            List<CartItem> guestCart = getGuestCart(session); // <-- ITT HÍVJUK A HIÁNYZÓ METÓDUST
            Optional<CartItem> existingGuestCartItem = guestCart.stream()
                    .filter(item -> item.getProduct().getId().equals(productId))
                    .findFirst();

            if (existingGuestCartItem.isPresent()) {
                CartItem cartItem = existingGuestCartItem.get();
                cartItem.setQuantity(cartItem.getQuantity() + quantity);
                cartItem.setPriceAtOrder(product.getPrice());
            } else {
                CartItem newCartItem = new CartItem();
                newCartItem.setProduct(product);
                newCartItem.setQuantity(quantity);
                newCartItem.setPriceAtOrder(product.getPrice());
                guestCart.add(newCartItem);
            }
            session.setAttribute(GUEST_CART_SESSION_KEY, guestCart);
            return guestCart;
        }
    }

    @Transactional
    public List<CartItem> updateCartItemQuantity(Long productId, int quantity, User user, HttpSession session) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Termék nem található ID: " + productId));

        if (user != null) {
            // Bejelentkezett felhasználó kosara
            CartItem cartItem = cartItemRepository.findByUserAndProduct(user, product)
                    .orElseThrow(() -> new IllegalArgumentException("Kosár elem nem található."));
            cartItem.setQuantity(quantity);
            cartItem.setPriceAtOrder(product.getPrice());
            cartItemRepository.save(cartItem);
            return cartItemRepository.findByUser(user);
        } else {
            // Vendég kosara
            List<CartItem> guestCart = getGuestCart(session);
            CartItem cartItem = guestCart.stream()
                    .filter(item -> item.getProduct().getId().equals(productId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Kosár elem nem található a vendég kosárban."));
            cartItem.setQuantity(quantity);
            cartItem.setPriceAtOrder(product.getPrice());
            session.setAttribute(GUEST_CART_SESSION_KEY, guestCart);
            return guestCart;
        }
    }

    @Transactional
    public List<CartItem> removeProductFromCart(Long productId, User user, HttpSession session) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Termék nem található ID: " + productId));

        if (user != null) {
            // Bejelentkezett felhasználó kosara
            cartItemRepository.deleteByUserAndProduct(user, product);
            return cartItemRepository.findByUser(user);
        } else {
            // Vendég kosara
            List<CartItem> guestCart = getGuestCart(session);
            guestCart.removeIf(item -> item.getProduct().getId().equals(productId));
            session.setAttribute(GUEST_CART_SESSION_KEY, guestCart);
            return guestCart;
        }
    }

    public List<CartItem> getCartItems(User user, HttpSession session) {
        if (user != null) {
            return cartItemRepository.findByUser(user);
        } else {
            return getGuestCart(session);
        }
    }

    public BigDecimal calculateCartTotal(User user, HttpSession session) {
        List<CartItem> cartItems = getCartItems(user, session);
        return cartItems.stream()
                .map(item -> {
                    BigDecimal price = item.getPriceAtOrder();
                    if (price == null) {
                        // Visszaeső logika, ha valamiért hiányzik az ár. Már nem igazán kéne ide jutni.
                        Product product = productRepository.findById(item.getProduct().getId())
                                .orElseThrow(() -> new IllegalStateException("Termék nem található a kosár elemhez: " + item.getProduct().getId()));
                        return product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    }
                    return price.multiply(BigDecimal.valueOf(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    public void clearCart(User user, HttpSession session) {
        if (user != null) {
            cartItemRepository.deleteByUser(user);
        } else {
            session.removeAttribute(GUEST_CART_SESSION_KEY);
        }
    }

    public void mergeSessionCartToUserCart(User user, HttpSession session) {
        if (user == null) {
            return;
        }

        List<CartItem> guestCart = getGuestCart(session);
        if (guestCart.isEmpty()) {
            return;
        }

        List<CartItem> userCart = cartItemRepository.findByUser(user);
        Set<Long> userProductIds = userCart.stream()
                .map(item -> item.getProduct().getId())
                .collect(Collectors.toSet());

        for (CartItem guestItem : guestCart) {
            Product product = guestItem.getProduct();
            if (userProductIds.contains(product.getId())) {
                Optional<CartItem> existingUserItemOpt = userCart.stream()
                        .filter(item -> item.getProduct().getId().equals(product.getId()))
                        .findFirst();
                existingUserItemOpt.ifPresent(userItem -> {
                    userItem.setQuantity(userItem.getQuantity() + guestItem.getQuantity());
                    userItem.setPriceAtOrder(product.getPrice());
                    cartItemRepository.save(userItem);
                });
            } else {
                CartItem newUserItem = new CartItem();
                newUserItem.setUser(user);
                newUserItem.setProduct(product);
                newUserItem.setQuantity(guestItem.getQuantity());
                newUserItem.setPriceAtOrder(product.getPrice());
                cartItemRepository.save(newUserItem);
            }
        }
        session.removeAttribute(GUEST_CART_SESSION_KEY);
    }

    private List<CartItem> getGuestCart(HttpSession session) {
        List<CartItem> guestCart = (List<CartItem>) session.getAttribute(GUEST_CART_SESSION_KEY);
        if (guestCart == null) {
            guestCart = new ArrayList<>();
            session.setAttribute(GUEST_CART_SESSION_KEY, guestCart);
        }
        return guestCart;
    }
}
