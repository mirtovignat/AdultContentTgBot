package com.example.demo.handler.user;

import com.example.demo.entity.Role;
import com.example.demo.exception.BusinessException;
import com.example.demo.handler.Handler;
import com.example.demo.service.PromoCodeService;
import com.example.demo.service.UserService;
import com.example.demo.state.UserState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class PromoCodeHandler implements Handler {

    private final PromoCodeService promoCodeService;
    private final UserService userService;
    private final Map<Long, UserState> userStates = new ConcurrentHashMap<>();

    @Override
    public List<Object> handle(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return List.of();
        }

        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();

        if ("Промокод".equals(text)) {
            var user = userService.getByChatId(chatId);
            if (user.getRole() == Role.ADMIN) {
                return List.of(createForbiddenMessage(chatId));
            }

            userStates.put(chatId, UserState.PROMO_CODE);
            return List.of(createMessage(chatId, "📝 Введите промокод:"));
        }

        if (userStates.get(chatId) == UserState.PROMO_CODE) {
            try {
                promoCodeService.activatePromoCode(chatId, text.trim());
                userStates.remove(chatId);
                return List.of(createMessage(chatId, "✅ Промокод успешно активирован!"));
            } catch (BusinessException e) {
                return List.of(createMessage(chatId, "❌ " + e.getMessage()));
            } catch (Exception e) {
                userStates.remove(chatId);
                return List.of(createMessage(chatId, "❌ Произошла ошибка. Попробуйте позже."));
            }
        }

        return List.of();
    }

    public boolean isWaitingPromoCode(Long chatId) {
        return userStates.get(chatId) == UserState.PROMO_CODE;
    }

    public void cancel(Long chatId) {
        userStates.remove(chatId);
    }

    private SendMessage createMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        return message;
    }

    private SendMessage createForbiddenMessage(Long chatId) {
        return createMessage(chatId, "❌ Эта функция недоступна для администратора.");
    }
}