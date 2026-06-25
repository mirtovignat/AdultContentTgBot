package com.example.demo.handler.user;

import com.example.demo.entity.Role;
import com.example.demo.handler.Handler;
import com.example.demo.service.UserService;
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

    @Value("${admin.chat-id:1210667437}")
    private Long adminChatId;

    private final Map<Long, UserState> userStates = new ConcurrentHashMap<>();
    private final UserService userService;

    @Override
    public List<Object> handle(Update update) {
        if (!update.hasMessage() || update.getMessage().getText() == null) {
            return List.of();
        }

        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();

        if ("Поддержка".equals(text)) {
            var user = userService.getByChatId(chatId);
            if (user.getRole() == Role.ADMIN) {
                return List.of(createForbiddenMessage(chatId));
            }

            userStates.put(chatId, UserState.SUPPORT);
            SendMessage instruction = new SendMessage();
            instruction.setChatId(chatId.toString());
            instruction.setText("📩 Напишите ваше сообщение, и я передам его администратору.\n(Для отмены отправьте /cancel)");
            return List.of(instruction);
        }

        if (userStates.getOrDefault(chatId, UserState.NONE) == UserState.SUPPORT) {
            SendMessage confirm = new SendMessage();
            confirm.setChatId(chatId.toString());
            confirm.setText("✅ Ваше сообщение отправлено администратору.");

            SendMessage adminMsg = new SendMessage();
            adminMsg.setChatId(adminChatId.toString());
            String firstName = update.getMessage().getFrom().getFirstName();
            String username = update.getMessage().getFrom().getUserName();
            String userInfo = (username != null ? "@" + username : firstName);
            adminMsg.setText("✉️ Сообщение от " + userInfo + " (ID: " + chatId + "):\n\n" + text);

            userStates.remove(chatId);
            return List.of(confirm, adminMsg);
        }

        return List.of();
    }

    public boolean isInSupportMode(Long chatId) {
        return userStates.getOrDefault(chatId, UserState.NONE) == UserState.SUPPORT;
    }

    public void cancelSupport(Long chatId) {
        userStates.remove(chatId);
    }

    private SendMessage createForbiddenMessage(Long chatId) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId.toString());
        msg.setText("❌ Эта функция недоступна для администратора.");
        return msg;
    }
}