package com.example.cyberpajzs.controller;

import com.example.cyberpajzs.service.CartService;
import com.example.cyberpajzs.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CartController {

    private final CartService cartService;
    private final ProductService productService;

    @Autowired
    public CartController(CartService cartService, ProductService productService) {
        this.cartService = cartService;
        this.productService = productService;
    }

    @GetMapping("/cart")
    public String showCart(Model model) {
        model.addAttribute("cartItems", cartService.getCartItems());
        model.addAttribute("cartTotal", cartService.calculateCartTotal());
        return "cart"; // Fontos: Győződj meg róla, hogy a fájl pontosan cart-view.html!
    }

    // Módosított addToCart metódus: @RequestParam-ot használ a productId-hez
    @PostMapping("/cart/add") // <-- Itt változott: nincs {productId} a PATH-ban
    public String addToCart(@RequestParam("productId") Long productId, // <-- Itt változott: @RequestParam
                            @RequestParam(defaultValue = "1") int quantity) {
        productService.findProductById(productId).ifPresent(product -> {
            cartService.addProductToCart(product, quantity);
        });
        return "redirect:/cart";
    }

    // A többi metódusod is módosulhat, ha az URL struktúra a controllerben path variable-t használ, de az űrlap request paramétert küld.
    // Nézzük át az űrlapokat is (cart.html), de egyelőre ezt javítsuk!
    @PostMapping("/cart/update") // <-- Valószínűleg itt is @RequestParam kellene
    public String updateCartItemQuantity(@RequestParam("productId") Long productId, @RequestParam int quantity) {
        // Mielőtt meghívnád a service-t, meg kell győződnöd, hogy a product létezik,
        // és a service a productId-t használja. A service-edben lévő findByUserAndProductId metódus jó.
        cartService.updateProductQuantity(productId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove") // <-- Valószínűleg itt is @RequestParam kellene
    public String removeCartItem(@RequestParam("productId") Long productId) {
        cartService.removeProductFromCart(productId);
        return "redirect:/cart";
    }

}