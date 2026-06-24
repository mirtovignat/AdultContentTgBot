package com.example.demo.handler;

import com.example.demo.keyboard.InlineKeyboardFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CategoryHandler implements Handler {

    private final InlineKeyboardFactory inlineKeyboardFactory;

    @Override
    public List<Object> handle(Update update) {
        Long chatId = update.getMessage().getChatId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        sendMessage.setText("Выберите категорию");
        sendMessage.setReplyMarkup(inlineKeyboardFactory.getCategoriesChoice());
        return List.of(sendMessage);
    }

}