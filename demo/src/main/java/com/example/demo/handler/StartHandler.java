package com.example.demo.handler;

import com.example.demo.entity.User;
import com.example.demo.entity.Role;
import com.example.demo.keyboard.ReplyKeyboardFactory;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StartHandler implements Handler {
    private final ReplyKeyboardFactory replyKeyboardFactory;
    private final UserService userService;

    @Override
    public List<Object> handle(Update update) {
        Long chatId = update.getMessage().getChatId();
        userService.register(update);
        User user = userService.getByChatId(chatId);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        sendMessage.setText("Главное меню");
        if (user.getRole() == Role.ADMIN) {
            sendMessage.setText("Главное меню (админ-режим)");
            sendMessage.setReplyMarkup(replyKeyboardFactory.createAdminMainKeyboard());
        } else {
            sendMessage.setReplyMarkup(replyKeyboardFactory.createMainKeyboard());
        }
        return List.of(sendMessage);
    }
}