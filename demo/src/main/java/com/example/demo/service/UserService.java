package com.example.demo.service;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public boolean isRegistered(Long chatId) {
        return userRepository.existsByChatId(chatId);
    }

    public User getByChatId(Long chatId) {
        return userRepository.findByChatId(chatId)
                .orElseGet(() -> {
                    User user = new User();
                    user.setChatId(chatId);
                    user.setBonuses(0L);
                    user.setReferralsCount(0L);
                    user.setRole(Role.USER);
                    return userRepository.save(user);
                });
    }

    public void register(Update update) {

        Long chatId = extractChatId(update);

        if (isRegistered(chatId)) {
            return;
        }

        var tgUser = update.getMessage().getFrom();

        User user = new User();
        user.setChatId(chatId);
        user.setBonuses(0L);
        user.setChatId(chatId);
        user.setTelegramId(tgUser.getId());
        user.setUsername(tgUser.getUserName());
        user.setFirstName(tgUser.getFirstName());
        user.setLastName(tgUser.getLastName());
        user.setBonuses(0L);
        user.setReferralsCount(0L);

        userRepository.save(user);
    }

    private Long extractChatId(Update update) {
        return update.getMessage().getChatId();
    }

}