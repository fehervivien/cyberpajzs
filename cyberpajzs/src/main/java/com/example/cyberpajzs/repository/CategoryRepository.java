package com.example.cyberpajzs.repository;

import com.example.cyberpajzs.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}