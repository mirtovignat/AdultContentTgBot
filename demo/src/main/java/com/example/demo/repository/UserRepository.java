package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {"files"})
    Optional<User> findByChatId(Long chatId);

    boolean existsByChatId(Long chatId);

    default User findByChatIdOrThrow(Long chatId) {
        return findByChatId(chatId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}