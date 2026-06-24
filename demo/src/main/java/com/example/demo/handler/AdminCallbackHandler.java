package com.example.demo.handler;

import com.example.demo.keyboard.InlineKeyboardFactory;
import com.example.demo.service.AdminService;
import com.example.demo.service.PromoCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AdminCallbackHandler implements Handler {

    private final PromoCodeService promoCodeService;
    private final AdminService adminService;
    private final InlineKeyboardFactory keyboardFactory;
    private final AdminHandler adminHandler;

    @Override
    public List<Object> handle(Update update) {
        if (!update.hasCallbackQuery()) return List.of();

        CallbackQuery callback = update.getCallbackQuery();
        String data = callback.getData();
        Long chatId = callback.getMessage().getChatId();
        Integer messageId = callback.getMessage().getMessageId();

        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(callback.getId());
        answer.setShowAlert(false);

        if (!data.startsWith("ADMIN_")) return List.of(answer);

        String[] parts = data.split("_", 2);
        String command = parts[0];
        String payload = parts.length > 1 ? parts[1] : "";

        switch (command) {
            case "ADMIN":
                switch (payload) {
                    case "MAIN":
                        return List.of(answer, showAdminMenu(chatId, messageId));

                    case "CREATE_PROMO":
                        adminHandler.startCreatingPromoCode(chatId);
                        answer.setText("Введите код промокода");
                        return List.of(answer, editMessage(chatId, messageId, "✏️ Введите код промокода"));

                    case "ADD_BONUS":
                        adminHandler.startAddingBonus(chatId);
                        answer.setText("Введите ID пользователя");
                        return List.of(answer, editMessage(chatId, messageId, "✏️ Введите ID пользователя (chatId)"));

                    case "STATS": {
                        var stats = adminService.getStatistics();
                        String text = String.format(
                                "📊 Статистика\n\n" +
                                        "👥 Пользователей: %d\n" +
                                        "🎁 Всего бонусов: %d\n" +
                                        "🎫 Промокодов: %d (активных %d, использовано %d)",
                                stats.totalUsers(),
                                stats.totalBonuses(),
                                stats.totalPromoCodes(),
                                stats.activePromoCodes(),
                                stats.usedPromoCodes()
                        );
                        return List.of(answer, editMessage(chatId, messageId, text));
                    }

                    case "LIST_PROMO": {
                        var promoCodes = promoCodeService.getAllActive();
                        if (promoCodes.isEmpty()) {
                            return List.of(answer, editMessage(chatId, messageId, "Нет активных промокодов."));
                        }
                        StringBuilder sb = new StringBuilder("🎫 Список промокодов:\n\n");
                        for (var promo : promoCodes) {
                            sb.append("🔹 ").append(promo.getCode())
                                    .append(" (+").append(promo.getBonusAmount()).append(" бонусов)")
                                    .append("\n");
                        }
                        var keyboard = keyboardFactory.getAdminPromoCodeList(promoCodes);
                        EditMessageText edit = new EditMessageText();
                        edit.setChatId(chatId.toString());
                        edit.setMessageId(messageId);
                        edit.setText(sb.toString());
                        edit.setReplyMarkup(keyboard);
                        return List.of(answer, edit);
                    }

                    default:
                        return List.of(answer);
                }

            case "ADMIN_DELETE":
                try {
                    Long id = Long.parseLong(payload);
                    promoCodeService.deletePromoCode(id);
                    answer.setText("✅ Промокод удалён");
                    return List.of(answer, editMessage(chatId, messageId, "Промокод удалён."));
                } catch (NumberFormatException e) {
                    answer.setText("❌ Неверный ID");
                    return List.of(answer);
                }

            default:
                return List.of(answer);
        }
    }

    private EditMessageText editMessage(Long chatId, Integer messageId, String text) {
        EditMessageText edit = new EditMessageText();
        edit.setChatId(chatId.toString());
        edit.setMessageId(messageId);
        edit.setText(text);
        return edit;
    }

    private EditMessageText showAdminMenu(Long chatId, Integer messageId) {
        EditMessageText edit = new EditMessageText();
        edit.setChatId(chatId.toString());
        edit.setMessageId(messageId);
        edit.setText("👑 Админ-панель");
        edit.setReplyMarkup(keyboardFactory.getAdminMainMenu());
        return edit;
    }
}