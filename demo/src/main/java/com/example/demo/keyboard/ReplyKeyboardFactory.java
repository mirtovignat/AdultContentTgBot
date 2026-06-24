package com.example.demo.keyboard;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

@Component
public class ReplyKeyboardFactory {
    public ReplyKeyboardMarkup createMainKeyboard() {

        KeyboardRow row1 = new KeyboardRow();
        row1.add("Категории");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("Профиль");
        row2.add("Бонус");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("Поддержка");

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();

        keyboard.setKeyboard(List.of(
                row1,
                row2,
                row3
        ));

        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(false);
        keyboard.setSelective(true);

        return keyboard;
    }

    public ReplyKeyboardMarkup createAdminMainKeyboard() {
        KeyboardRow row1 = new KeyboardRow();
        row1.add("Категории");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("Профиль");
        row2.add("Бонус");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("Админ панель");
        row3.add("Поддержка");

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setKeyboard(List.of(row1, row2, row3));
        keyboard.setResizeKeyboard(true);
        return keyboard;
    }

}
