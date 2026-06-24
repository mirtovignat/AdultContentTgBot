package com.example.demo.handler;

import com.example.demo.data_format.TimeFormater;
import com.example.demo.dto.BonusResult;
import com.example.demo.keyboard.InlineKeyboardFactory;
import com.example.demo.service.BonusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CallbackHandler implements Handler {

    private final BonusService bonusService;
    private final InlineKeyboardFactory keyboardFactory;
    private final TimeFormater timeFormater;

    @Override
    public List<Object> handle(Update update) {
        if (!update.hasCallbackQuery()) {
            return List.of();
        }

        CallbackQuery callback = update.getCallbackQuery();
        String data = callback.getData();

        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(callback.getId());
        answer.setShowAlert(false);

        Long chatId = callback.getMessage().getChatId();
        Integer messageId = callback.getMessage().getMessageId();

        if ("UPDATE_TIME".equals(data)) {
            long leftSeconds = bonusService.getLeftSeconds(chatId);
            EditMessageText editMessage = getEditMessageText(leftSeconds, chatId, messageId);
            return List.of(answer, editMessage);
        }

        if ("GET".equals(data)) {
            BonusResult result = bonusService.claimBonus(chatId);

            EditMessageText editMessage = new EditMessageText();
            editMessage.setChatId(chatId.toString());
            editMessage.setMessageId(messageId);

            if (result.allowed()) {
                long leftSeconds = bonusService.getLeftSeconds(chatId);
                String time = timeFormater.formatTime(leftSeconds);
                editMessage.setText("🎁 Бонус получен!\nСледующий через: " + time);
                editMessage.setReplyMarkup(keyboardFactory.getTimerUpdateButton());
                answer.setText("Бонус зачислен!");
            } else {
                String time = timeFormater.formatTime(result.secondsLeft());
                editMessage.setText("⏳ Бонус недоступен\nОсталось: " + time);
                editMessage.setReplyMarkup(keyboardFactory.getTimerUpdateButton());
                answer.setText("Бонус ещё не доступен");
            }

            return List.of(answer, editMessage);
        }

        return List.of(answer);
    }

    private EditMessageText getEditMessageText(long leftSeconds, Long chatId, Integer messageId) {
        String time = timeFormater.formatTime(leftSeconds);

        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(chatId.toString());
        editMessage.setMessageId(messageId);

        if (leftSeconds == 0) {
            editMessage.setText("🎁 Бонус доступен! Нажмите «Забрать»");
            editMessage.setReplyMarkup(keyboardFactory.getBonusWithUpdateButton());
        } else {
            editMessage.setText("⏳ Бонус недоступен\nОсталось: " + time);
            editMessage.setReplyMarkup(keyboardFactory.getTimerUpdateButton());
        }
        return editMessage;
    }

}