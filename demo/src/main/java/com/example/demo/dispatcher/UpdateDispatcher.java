package com.example.demo.dispatcher;

import com.example.demo.handler.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UpdateDispatcher {

    private final StartHandler startHandler;
    private final CategoryHandler categoryHandler;
    private final ProfileHandler profileHandler;
    private final BonusHandler bonusHandler;
    private final SupportHandler supportHandler;
    private final CallbackHandler callbackHandler;
    private final AdminHandler adminHandler;
    private final AdminCallbackHandler adminCallbackHandler;
    private final HelpHandler helpHandler;

    public List<Object> dispatch(Update update) {
        if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();
            if (data != null && data.startsWith("ADMIN_")) {
                return adminCallbackHandler.handle(update);
            } else {
                return callbackHandler.handle(update);
            }
        }

        if (!update.hasMessage() || update.getMessage().getText() == null) {
            return List.of();
        }

        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        if ("/cancel".equals(text)) {
            supportHandler.cancelSupport(chatId);
            SendMessage response = new SendMessage();
            response.setChatId(chatId.toString());
            response.setText("Режим поддержки отменён.");
            return List.of(response);
        }

        List<Object> result = switch (text) {
            case "/start" -> startHandler.handle(update);
            case "/help" -> helpHandler.handle(update);
            case "Категории" -> categoryHandler.handle(update);
            case "Профиль" -> profileHandler.handle(update);
            case "Бонус" -> bonusHandler.handle(update);
            case "Админ панель" -> adminHandler.handle(update);
            case "Поддержка" -> supportHandler.handle(update);
            default -> null;
        };

        if (result != null) {
            return result;
        }

        if (text.startsWith("/")) {
            return helpHandler.handle(update);
        }

        if (supportHandler.isInSupportMode(chatId)) {
            return supportHandler.handle(update);
        }

        return List.of();
    }
}