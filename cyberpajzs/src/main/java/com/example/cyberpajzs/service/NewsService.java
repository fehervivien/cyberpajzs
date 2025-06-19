package com.example.cyberpajzs.service;

import com.example.cyberpajzs.entity.NewsArticle;
import com.example.cyberpajzs.repository.NewsArticleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NewsService {

    private final NewsArticleRepository newsArticleRepository;

    // Konstruktor injekció a repository-hoz
    public NewsService(NewsArticleRepository newsArticleRepository) {
        this.newsArticleRepository = newsArticleRepository;
    }

    public List<NewsArticle> findAllNewsArticlesOrderedByPublicationDateDesc() {
        List<NewsArticle> articles = newsArticleRepository.findAll();
        articles.sort((a1, a2) -> a2.getPublicationDate().compareTo(a1.getPublicationDate()));
        return articles;
    }


    public Optional<NewsArticle> findNewsArticleById(Long id) {
        return newsArticleRepository.findById(id);
    }

    // Tranzakciókezelés biztosítása
    @Transactional
    public NewsArticle saveNewsArticle(NewsArticle newsArticle) {
        // Ha új a hírcikk, beállítjuk a publikálás dátumát
        if (newsArticle.getId() == null) {
            newsArticle.setPublicationDate(LocalDateTime.now());
        }
        return newsArticleRepository.save(newsArticle);
    }


    @Transactional
    public void deleteNewsArticle(Long id) {
        if (!newsArticleRepository.existsById(id)) {
            throw new IllegalArgumentException("A hírcikk nem található, ID: " + id);
        }
        newsArticleRepository.deleteById(id);
    }
}
