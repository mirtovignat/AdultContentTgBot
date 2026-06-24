package com.example.demo.handler;

import com.example.demo.data_format.TimeFormater;
import com.example.demo.keyboard.InlineKeyboardFactory;
import com.example.demo.service.BonusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BonusHandler implements Handler {

    private final BonusService bonusService;
    private final InlineKeyboardFactory keyboardFactory;
    private final TimeFormater timeFormater;

    @Override
    public List<Object> handle(Update update) {
        if (!update.hasMessage() || update.getMessage().getText() == null) {
            return List.of();
        }

        String text = update.getMessage().getText();
        if (!"Бонус".equals(text)) {
            return List.of();
        }

        Long chatId = update.getMessage().getChatId();
        long leftSeconds = bonusService.getLeftSeconds(chatId);

        SendMessage sendMessage = getSendMessage(chatId, leftSeconds);

        return List.of(sendMessage);
    }

    private SendMessage getSendMessage(Long chatId, long leftSeconds) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());

        if (leftSeconds == 0) {
            sendMessage.setText("🎁 Бонус доступен! Нажмите «Забрать»");
            sendMessage.setReplyMarkup(keyboardFactory.getBonusWithUpdateButton());
        } else {
            String time = timeFormater.formatTime(leftSeconds);
            sendMessage.setText("⏳ Бонус недоступен\nОсталось: " + time);
            sendMessage.setReplyMarkup(keyboardFactory.getTimerUpdateButton());
        }
        return sendMessage;
    }

}