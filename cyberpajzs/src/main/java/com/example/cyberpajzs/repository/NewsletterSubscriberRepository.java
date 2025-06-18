package com.example.cyberpajzs.repository;

import com.example.cyberpajzs.entity.NewsletterSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NewsletterSubscriberRepository extends JpaRepository<NewsletterSubscriber, Long> {
    // Ezzel a metódussal ellenőrizhetjük, hogy az e-mail cím már fel van-e iratkozva
    Optional<NewsletterSubscriber> findByEmail(String email);
}