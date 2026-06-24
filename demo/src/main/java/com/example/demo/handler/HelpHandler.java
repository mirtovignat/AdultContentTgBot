package com.example.demo.handler;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HelpHandler implements Handler {

    private final UserService userService;

    @Override
    public List<Object> handle(Update update) {
        Long chatId = update.getMessage().getChatId();
        User user = userService.getByChatId(chatId);
        boolean isAdmin = user.getRole() == Role.ADMIN;

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("🤖 Доступные команды:\n\n");
        stringBuilder.append("/start – Главное меню\n");
        stringBuilder.append("/help – Помощь\n");
        stringBuilder.append("/cancel – Отменить действие\n");
        stringBuilder.append("Категории – Выбор категории\n");
        stringBuilder.append("Профиль – Ваш профиль\n");
        stringBuilder.append("Бонус – Забрать бонус\n");
        stringBuilder.append("Поддержка – Написать админу\n");
        if (isAdmin) {
            stringBuilder.append("\n👑 Админ-панель (кнопка в меню)\n");
        }

        SendMessage msg = new SendMessage();
        msg.setChatId(chatId.toString());
        msg.setText(stringBuilder.toString());
        return List.of(msg);
    }
}