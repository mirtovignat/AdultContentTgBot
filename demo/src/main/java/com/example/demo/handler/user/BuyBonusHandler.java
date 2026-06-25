package com.example.demo.handler.user;

import com.example.demo.dto.StarsOffer;
import com.example.demo.handler.Handler;
import com.example.demo.keyboard.InlineKeyboardFactory;
import com.example.demo.service.StarsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BuyBonusHandler implements Handler {

    private final StarsService starsService;
    private final InlineKeyboardFactory keyboardFactory;

    @Override
    public List<Object> handle(Update update) {
        if (!update.hasMessage() || update.getMessage().getText() == null) {
            return List.of();
        }

        String text = update.getMessage().getText();
        if (!"Купить бонусы".equals(text)) {
            return List.of();
        }

        Long chatId = update.getMessage().getChatId();
        List<StarsOffer> offers = starsService.getOffers();

        StringBuilder sb = new StringBuilder("⭐️ Купите бонусы за Telegram Stars:\n\n");
        for (int i = 0; i < offers.size(); i++) {
            StarsOffer offer = offers.get(i);
            sb.append(i + 1).append(". ")
                    .append(offer.stars()).append(" ⭐️ = ")
                    .append(offer.bonus()).append(" бонусов\n");
        }
        sb.append("\nНажмите на кнопку с нужным предложением.");

        SendMessage msg = new SendMessage();
        msg.setChatId(chatId.toString());
        msg.setText(sb.toString());
        msg.setReplyMarkup(keyboardFactory.getStarsOffersKeyboard(offers));
        return List.of(msg);
    }
}