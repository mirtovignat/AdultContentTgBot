package com.example.demo.dispatcher;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.handler.CallbackHandler;
import com.example.demo.handler.admin.AdminCallbackHandler;
import com.example.demo.handler.admin.AdminHandler;
import com.example.demo.handler.admin.UploadHandler;
import com.example.demo.handler.user.*;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.StarsPaymentService;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.payments.SuccessfulPayment;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UpdateDispatcher {

    private final StartHandler startHandler;
    private final CategoryHandler categoryHandler;
    private final ProfileHandler profileHandler;
    private final BonusHandler bonusHandler;
    private final SupportHandler supportHandler;
    private final PromoCodeHandler promoCodeHandler;
    private final BuyBonusHandler buyBonusHandler;
    private final CallbackHandler callbackHandler;
    private final AdminHandler adminHandler;
    private final AdminCallbackHandler adminCallbackHandler;
    private final HelpHandler helpHandler;
    private final UserService userService;
    private final UserRepository userRepository;
    private final StarsPaymentService starsPaymentService;
    private final UploadHandler uploadHandler;

    public List<Object> dispatch(Update update) {
        if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();
            if (data != null && data.startsWith("ADMIN_")) {
                return adminCallbackHandler.handle(update);
            } else {
                return callbackHandler.handle(update);
            }
        }

        if (!update.hasMessage()) {
            return List.of();
        }

        // 1. Обработка успешного платежа (ДО проверки текста)
        if (update.getMessage().getSuccessfulPayment() != null) {
            SuccessfulPayment payment = update.getMessage().getSuccessfulPayment();
            try {
                String payload = starsPaymentService.extractPayload(payment);
                int bonusAmount = starsPaymentService.extractBonusFromPayload(payload);
                starsPaymentService.addBonuses(update.getMessage().getChatId(), bonusAmount);

                SendMessage msg = new SendMessage();
                msg.setChatId(update.getMessage().getChatId().toString());
                msg.setText("✅ Оплата прошла успешно! Вам начислено " + bonusAmount + " бонусов.");
                return List.of(msg);
            } catch (Exception e) {
                SendMessage msg = new SendMessage();
                msg.setChatId(update.getMessage().getChatId().toString());
                msg.setText("❌ Ошибка при обработке платежа: " + e.getMessage());
                return List.of(msg);
            }
        }

        // 2. Проверка текста
        if (update.getMessage().getText() == null) {
            return List.of();
        }

        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        // 3. Отмена
        if ("/cancel".equals(text)) {
            supportHandler.cancelSupport(chatId);
            promoCodeHandler.cancel(chatId);
            adminHandler.cancel(chatId);
            SendMessage response = new SendMessage();
            response.setChatId(chatId.toString());
            response.setText("Действие отменено.");
            return List.of(response);
        }

        // 4. Определяем роль
        Role role = Role.USER;
        try {
            role = userService.getByChatId(chatId).getRole();
        } catch (Exception ignored) {}

        // 5. Обработка команд
        List<Object> result = dispatchCommand(text, update, role, chatId);
        if (result != null) {
            return result;
        }

        // 6. Проверка диалогов
        if (adminHandler.isInAdminDialog(chatId)) {
            return adminHandler.handle(update);
        }

        if (text.startsWith("/")) {
            return helpHandler.handle(update);
        }

        if (supportHandler.isInSupportMode(chatId)) {
            return supportHandler.handle(update);
        }

        if (promoCodeHandler.isWaitingPromoCode(chatId)) {
            return promoCodeHandler.handle(update);
        }

        return List.of();
    }

    private List<Object> dispatchCommand(String command, Update update, Role role, Long chatId) {
        return switch (command) {
            case "/start" -> startHandler.handle(update);
            case "/help" -> helpHandler.handle(update);
            case "Категории" -> categoryHandler.handle(update);
            case "Профиль" -> profileHandler.handle(update);

            case "Бонус" -> {
                if (role == Role.ADMIN) {
                    yield List.of(createForbiddenMessage(chatId));
                }
                yield bonusHandler.handle(update);
            }

            case "Промокод" -> {
                if (role == Role.ADMIN) {
                    yield List.of(createForbiddenMessage(chatId));
                }
                yield promoCodeHandler.handle(update);
            }

            case "Купить бонусы" -> {
                if (role == Role.ADMIN) {
                    yield List.of(createForbiddenMessage(chatId));
                }
                yield buyBonusHandler.handle(update);
            }

            case "Поддержка" -> {
                if (role == Role.ADMIN) {
                    yield List.of(createForbiddenMessage(chatId));
                }
                yield supportHandler.handle(update);
            }

            case "Админ панель" -> {
                if (role != Role.ADMIN) {
                    yield List.of(createForbiddenMessage(chatId));
                }
                yield adminHandler.handle(update);
            }

            case "Загрузить файл" -> {
                if (role != Role.ADMIN) {
                    yield List.of(createForbiddenMessage(chatId));
                }
                yield uploadHandler.handle(update);
            }

            default -> null;
        };
    }

    private SendMessage createForbiddenMessage(Long chatId) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId.toString());
        msg.setText("❌ Эта функция недоступна для вашей роли.");
        return msg;
    }
}