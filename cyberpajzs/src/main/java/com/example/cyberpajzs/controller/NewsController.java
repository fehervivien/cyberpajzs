package com.example.cyberpajzs.controller;

import com.example.cyberpajzs.entity.NewsArticle;
import com.example.cyberpajzs.service.NewsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Optional;

@Controller
public class NewsController {

    private final NewsService newsService;

    // Konstruktor injekció
    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @GetMapping("/news")
    public String listNews(Model model) {
        model.addAttribute("newsArticles", newsService.findAllNewsArticlesOrderedByPublicationDateDesc());
        return "news";
    }

    @GetMapping("/news/{id}")
    public String showNewsArticleDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<NewsArticle> newsArticle = newsService.findNewsArticleById(id);
        if (newsArticle.isPresent()) {
            model.addAttribute("newsArticle", newsArticle.get());
            return "news-details";
        } else {
            redirectAttributes.addFlashAttribute("error", "A hírcikk nem található.");
            return "redirect:/news";
        }
    }
}
