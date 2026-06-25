package com.example.demo.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import com.example.demo.service.PromoCodeService;
import com.example.demo.state.UserState;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class PromoCodeHandler implements Handler {

    private final PromoCodeService promoCodeService;

    private final Map<Long, UserState> userStates =
            new ConcurrentHashMap<>();

    @Override
    public List<Object> handle(Update update) {

        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return List.of();
        }

        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();

        if ("Промокод".equals(text)) {

            userStates.put(chatId, UserState.PROMO_CODE);

            return List.of(
                    createMessage(
                            chatId,
                            "Введите промокод"
                    )
            );
        }

        if (userStates.get(chatId) == UserState.PROMO_CODE) {

            promoCodeService.activatePromoCode(
                    chatId,
                    text.trim()
            );

            userStates.remove(chatId);

            return List.of(
                    createMessage(
                            chatId,
                            "✅ Промокод успешно активирован"
                    )
            );
        }

        return List.of();
    }

    public boolean isWaitingPromoCode(Long chatId) {
        return userStates.get(chatId) == UserState.PROMO_CODE;
    }

    public void cancel(Long chatId) {
        userStates.remove(chatId);
    }

    private SendMessage createMessage(
            Long chatId,
            String text
    ) {

        SendMessage message = new SendMessage();

        message.setChatId(chatId.toString());
        message.setText(text);

        return message;
    }
}