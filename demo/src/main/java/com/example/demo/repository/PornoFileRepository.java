package com.example.demo.repository;

import com.example.demo.entity.Category;
import com.example.demo.entity.PornFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PornoFileRepository extends JpaRepository<PornFile, Long> {

    // Новый метод для получения файлов по категории с лимитом
    @Query("SELECT p FROM PornFile p WHERE p.category = :category ORDER BY p.uploadedAt DESC")
    List<PornFile> findByCategory(@Param("category") Category category, int limit);
}