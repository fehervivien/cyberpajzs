package com.example.cyberpajzs.service;

import com.example.cyberpajzs.entity.Product;
import com.example.cyberpajzs.entity.QuoteRequest;
import com.example.cyberpajzs.entity.User;
import com.example.cyberpajzs.repository.ProductRepository;
import com.example.cyberpajzs.repository.QuoteRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class QuoteService {

    private final QuoteRequestRepository quoteRequestRepository;
    private final ProductService productService; // A ProductService használata a Product lekéréséhez

    public QuoteService(QuoteRequestRepository quoteRequestRepository, ProductService productService) {
        this.quoteRequestRepository = quoteRequestRepository;
        this.productService = productService;
    }

    @Transactional
    public QuoteRequest createQuoteRequest(Long productId, User user, String name, String email, String phone, String companyName, String message) {
        Product product = productService.findProductById(productId)
                .orElseThrow(() -> new IllegalArgumentException("A megadott termék nem található ID: " + productId));

        QuoteRequest quoteRequest = new QuoteRequest();
        quoteRequest.setProduct(product);
        quoteRequest.setUser(user); // Lehet null, ha vendég felhasználó
        quoteRequest.setName(name);
        quoteRequest.setEmail(email);
        quoteRequest.setPhone(phone);
        quoteRequest.setCompanyName(companyName);
        quoteRequest.setMessage(message);
        quoteRequest.setRequestDate(LocalDateTime.now());
        quoteRequest.setStatus("PENDING"); // Kezdeti státusz

        return quoteRequestRepository.save(quoteRequest);
    }

    // Admin felülethez szükséges metódusok (későbbiekben implementálható)
    public QuoteRequest getQuoteRequestById(Long id) {
        return quoteRequestRepository.findById(id).orElse(null);
    }

    public void updateQuoteRequestStatus(Long id, String status) {
        QuoteRequest request = quoteRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ajánlatkérés nem található ID: " + id));
        request.setStatus(status);
        quoteRequestRepository.save(request);
    }
}