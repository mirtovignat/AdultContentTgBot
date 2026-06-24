package com.example.demo.handler;

import com.example.demo.state.UserState;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class SupportHandler implements Handler {

    // chat_id администратора (твой)
    @Value("${admin.chat-id:1210667437}")
    private Long adminChatId;

    // Хранилище состояний пользователей
    private final Map<Long, UserState> userStates = new ConcurrentHashMap<>();

    @Override
    public List<Object> handle(Update update) {
        if (!update.hasMessage() || update.getMessage().getText() == null) {
            return List.of();
        }

        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();

        // Если пользователь нажал кнопку "Поддержка" – переключаем состояние
        if ("Поддержка".equals(text)) {
            userStates.put(chatId, UserState.SUPPORT);
            SendMessage instruction = new SendMessage();
            instruction.setChatId(chatId.toString());
            instruction.setText("📩 Напишите ваше сообщение, и я передам его администратору.\n(Для отмены отправьте /cancel)");
            return List.of(instruction);
        }

        // Если пользователь в состоянии SUPPORT – пересылаем его сообщение админу
        if (userStates.getOrDefault(chatId, UserState.NONE) == UserState.SUPPORT) {
            // Отправляем подтверждение пользователю
            SendMessage confirm = new SendMessage();
            confirm.setChatId(chatId.toString());
            confirm.setText("✅ Ваше сообщение отправлено администратору.");

            // Отправляем сообщение админу
            SendMessage adminMsg = new SendMessage();
            adminMsg.setChatId(adminChatId.toString());
            // Узнаем имя пользователя (можно из базы, но упростим – используем first_name)
            String firstName = update.getMessage().getFrom().getFirstName();
            String username = update.getMessage().getFrom().getUserName();
            String userInfo = (username != null ? "@" + username : firstName);
            adminMsg.setText("✉️ Сообщение от " + userInfo + " (ID: " + chatId + "):\n\n" + text);

            // Сбрасываем состояние
            userStates.remove(chatId);

            return List.of(confirm, adminMsg);
        }

        // Если пользователь не в состоянии SUPPORT и это не команда "Поддержка" – ничего не делаем
        return List.of();
    }

    public boolean isInSupportMode(Long chatId) {
        return userStates.getOrDefault(chatId, UserState.NONE) == UserState.SUPPORT;
    }

    public void cancelSupport(Long chatId) {
        userStates.remove(chatId);
    }
}