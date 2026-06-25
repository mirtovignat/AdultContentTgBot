package com.example.demo.handler.admin;

import com.example.demo.handler.Handler;
import com.example.demo.keyboard.InlineKeyboardFactory;
import com.example.demo.keyboard.ReplyKeyboardFactory;
import com.example.demo.service.AdminService;
import com.example.demo.service.PromoCodeService;
import com.example.demo.state.AdminState;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class AdminHandler implements Handler {

    private final AdminService adminService;
    private final PromoCodeService promoCodeService;
    private final InlineKeyboardFactory inlineKeyboardFactory;
    private final ReplyKeyboardFactory replyKeyboardFactory;

    private final Map<Long, AdminSession> sessions = new ConcurrentHashMap<>();

    @Override
    public List<Object> handle(Update update) {
        if (!update.hasMessage() || update.getMessage().getText() == null) {
            return List.of();
        }

        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();

        if ("Админ панель".equals(text)) {
            return showAdminMenu(chatId);
        }

        AdminSession session = sessions.get(chatId);
        if (session != null && session.getState() != AdminState.NONE) {
            return processAdminDialog(chatId, text, session);
        }

        return List.of();
    }

    private List<Object> showAdminMenu(Long chatId) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId.toString());
        msg.setText("👑 Админ-панель\nВыберите действие:");
        msg.setReplyMarkup(inlineKeyboardFactory.getAdminMainMenu());
        return List.of(msg);
    }

    private List<Object> processAdminDialog(Long chatId, String text, AdminSession session) {
        switch (session.getState()) {
            case CREATING_PROMOCODE_WAITING_CODE:
                session.setTempCode(text);
                session.setState(AdminState.CREATING_PROMOCODE_WAITING_BONUS);
                return List.of(createSendMessage(chatId, "Введите количество бонусов (число):"));

            case CREATING_PROMOCODE_WAITING_BONUS:
                try {
                    long bonus = Long.parseLong(text);
                    session.setTempBonus(bonus);
                    session.setState(AdminState.CREATING_PROMOCODE_WAITING_EXPIRY);
                    return List.of(createSendMessage(chatId, "Введите срок действия в днях (или напишите 'бессрочно'):"));
                } catch (NumberFormatException e) {
                    return List.of(createSendMessage(chatId, "❌ Некорректное число. Попробуйте снова."));
                }

            case CREATING_PROMOCODE_WAITING_EXPIRY:
                LocalDateTime expiresAt = null;
                if (!"бессрочно".equalsIgnoreCase(text)) {
                    try {
                        long days = Long.parseLong(text);
                        if (days <= 0) throw new NumberFormatException();
                        expiresAt = LocalDateTime.now().plusDays(days);
                    } catch (NumberFormatException e) {
                        return List.of(createSendMessage(chatId, "❌ Введите положительное число дней или 'бессрочно'."));
                    }
                }
                session.setTempExpiresAt(expiresAt);
                session.setState(AdminState.CREATING_PROMOCODE_WAITING_LIMIT);
                return List.of(createSendMessage(chatId, "Введите лимит использований (или 'безлимитно'):"));

            case CREATING_PROMOCODE_WAITING_LIMIT:
                Long usageLimit = null;
                if (!"безлимитно".equalsIgnoreCase(text)) {
                    try {
                        long limit = Long.parseLong(text);
                        if (limit <= 0) throw new NumberFormatException();
                        usageLimit = limit;
                    } catch (NumberFormatException e) {
                        return List.of(createSendMessage(chatId, "❌ Введите положительное число или 'безлимитно'."));
                    }
                }
                try {
                    var promo = promoCodeService.createPromoCode(
                            session.getTempCode(),
                            session.getTempBonus(),
                            session.getTempExpiresAt(),
                            usageLimit
                    );
                    sessions.remove(chatId);
                    return List.of(createSendMessage(chatId,
                            "✅ Промокод создан!\n\n" +
                                    "Код: " + promo.getCode() + "\n" +
                                    "Бонус: " + promo.getBonusAmount() + "\n" +
                                    "Срок: " + (promo.getExpiresAt() == null ? "бессрочный" : promo.getExpiresAt().toString()) + "\n" +
                                    "Лимит: " + (promo.getUsageLimit() == null ? "безлимитный" : promo.getUsageLimit())
                    ));
                } catch (Exception e) {
                    return List.of(createSendMessage(chatId, "❌ Ошибка: " + e.getMessage()));
                }

            case ADDING_BONUS_WAITING_USER:
                try {
                    Long targetChatId = Long.parseLong(text);
                    session.setTempTargetChatId(targetChatId);
                    session.setState(AdminState.ADDING_BONUS_WAITING_AMOUNT);
                    return List.of(createSendMessage(chatId, "Введите количество бонусов для начисления:"));
                } catch (NumberFormatException e) {
                    return List.of(createSendMessage(chatId, "❌ Некорректный ID. Попробуйте снова."));
                }

            case ADDING_BONUS_WAITING_AMOUNT:
                try {
                    long amount = Long.parseLong(text);
                    Long targetChatId = session.getTempTargetChatId();
                    adminService.addBonuses(targetChatId, amount);
                    sessions.remove(chatId);
                    return List.of(createSendMessage(chatId,
                            "✅ Начислено " + amount + " бонус(ов) пользователю с ID " + targetChatId));
                } catch (NumberFormatException e) {
                    return List.of(createSendMessage(chatId, "❌ Некорректное число."));
                }

            default:
                sessions.remove(chatId);
                return List.of(createSendMessage(chatId, "❓ Неизвестная команда. Начните заново."));
        }
    }

    private SendMessage createSendMessage(Long chatId, String text) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId.toString());
        msg.setText(text);
        return msg;
    }

    @Setter
    @Getter
    private static class AdminSession {
        private AdminState state = AdminState.NONE;
        private String tempCode;
        private Long tempBonus;
        private LocalDateTime tempExpiresAt;
        private Long tempTargetChatId;

    }

    public void startCreatingPromoCode(Long chatId) {
        AdminSession session = sessions.computeIfAbsent(chatId, k -> new AdminSession());
        session.setState(AdminState.CREATING_PROMOCODE_WAITING_CODE);
    }

    public void startAddingBonus(Long chatId) {
        AdminSession session = sessions.computeIfAbsent(chatId, k -> new AdminSession());
        session.setState(AdminState.ADDING_BONUS_WAITING_USER);
    }
}