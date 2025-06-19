package com.example.cyberpajzs.service;

import com.example.cyberpajzs.entity.NewsletterSubscriber;
import com.example.cyberpajzs.repository.NewsletterSubscriberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service // Spring szolgáltatás komponensként jelölve
public class NewsletterService {

    private final NewsletterSubscriberRepository newsletterSubscriberRepository;

    // Konstruktor injekció a repository-hoz
    public NewsletterService(NewsletterSubscriberRepository newsletterSubscriberRepository) {
        this.newsletterSubscriberRepository = newsletterSubscriberRepository;
    }

    /**
     * Felhasználó feliratkoztatása a hírlevélre.
     * Ellenőrzi, hogy az e-mail cím már létezik-e, ha igen, kivételt dob.
     *
     * @param email A feliratkozó e-mail címe.
     * @return A mentett NewsletterSubscriber entitás.
     * @throws IllegalArgumentException Ha az e-mail cím már fel van iratkozva.
     */
    @Transactional // A tranzakciókezelés biztosítása
    public NewsletterSubscriber subscribe(String email) {
        // Ellenőrizzük, hogy az e-mail cím már fel van-e iratkozva
        Optional<NewsletterSubscriber> existingSubscriber = newsletterSubscriberRepository.findByEmail(email);

        if (existingSubscriber.isPresent()) {
            // Ha már létezik, dobjunk kivételt
            throw new IllegalArgumentException("Ez az e-mail cím már fel van iratkozva a hírlevélre.");
        }

        // Hozzuk létre az új feliratkozó entitást
        NewsletterSubscriber subscriber = new NewsletterSubscriber();
        subscriber.setEmail(email);
        subscriber.setSubscriptionDate(LocalDateTime.now()); // Beállítjuk az aktuális feliratkozási dátumot

        // Mentjük az entitást az adatbázisba
        return newsletterSubscriberRepository.save(subscriber);
    }

    /**
     * Visszaadja az összes hírlevél feliratkozót.
     * @return Feliratkozók listája.
     */
    public List<NewsletterSubscriber> findAllSubscribers() {
        return newsletterSubscriberRepository.findAll();
    }

    /**
     * Feliratkozó törlése ID alapján.
     * @param id A törlendő feliratkozó ID-je.
     */
    @Transactional
    public void unsubscribe(Long id) {
        newsletterSubscriberRepository.deleteById(id);
    }

    /**
     * Felhasználó leiratkoztatása e-mail cím alapján.
     *
     * @param email A leiratkozó e-mail címe.
     */
    @Transactional
    public void unsubscribeByEmail(String email) {
        Optional<NewsletterSubscriber> subscriber = newsletterSubscriberRepository.findByEmail(email);
        subscriber.ifPresent(newsletterSubscriberRepository::delete);
    }
}
