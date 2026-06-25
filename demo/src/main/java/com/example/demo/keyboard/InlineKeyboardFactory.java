package com.example.demo.keyboard;

import com.example.demo.dto.StarsOffer;
import com.example.demo.entity.BonusButton;
import com.example.demo.entity.Category;
import com.example.demo.entity.PromoCode;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
public class InlineKeyboardFactory {

    public InlineKeyboardMarkup getCategoriesChoice() {
        return new InlineKeyboardMarkup(List.of(
                List.of(createButton("Random", Category.RANDOM.name())),
                List.of(createButton("Boobs", Category.BOOBS.name())),
                List.of(createButton("Pussy", Category.PUSSY.name())),
                List.of(createButton("Upskirt", Category.UPSKIRT.name())),
                List.of(createButton("Mom's", Category.MOMS.name())),
                List.of(createButton("Alt", Category.ALT.name())),
                List.of(createButton("Packs", Category.PACKS.name()))
        ));
    }

    public InlineKeyboardMarkup getBonusButton() {
        return new InlineKeyboardMarkup(List.of(
                List.of(createButton("Забрать", BonusButton.GET.name()))
        ));
    }

    public InlineKeyboardMarkup getTimerUpdateButton() {
        return new InlineKeyboardMarkup(List.of(
                List.of(createButton("🔄 Обновить", BonusButton.UPDATE_TIME.name()))
        ));
    }

    public InlineKeyboardMarkup getBonusWithUpdateButton() {
        return new InlineKeyboardMarkup(List.of(
                List.of(createButton("Забрать", BonusButton.GET.name())),
                List.of(createButton("🔄 Обновить", BonusButton.UPDATE_TIME.name()))
        ));
    }

    private InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

    public InlineKeyboardMarkup getAdminMainMenu() {
        return new InlineKeyboardMarkup(List.of(
                List.of(createButton("📝 Создать промокод", "ADMIN_CREATE_PROMO")),
                List.of(createButton("📋 Список промокодов", "ADMIN_LIST_PROMO")),
                List.of(createButton("➕ Начислить бонус", "ADMIN_ADD_BONUS")),
                List.of(createButton("📊 Статистика", "ADMIN_STATS")),
                List.of(createButton("🔙 Назад", "ADMIN_MAIN"))
        ));
    }

    public InlineKeyboardMarkup getAdminPromoCodeList(List<PromoCode> promoCodes) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (PromoCode promo : promoCodes) {
            InlineKeyboardButton deleteBtn = new InlineKeyboardButton();
            deleteBtn.setText("❌ Удалить " + promo.getCode());
            deleteBtn.setCallbackData("ADMIN_DELETE_" + promo.getId());
            rows.add(List.of(deleteBtn));
        }
        rows.add(List.of(createButton("🔙 Назад", "ADMIN_MAIN")));
        return new InlineKeyboardMarkup(rows);
    }

    public InlineKeyboardMarkup getStarsOffersKeyboard(List<StarsOffer> offers) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (StarsOffer offer : offers) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(offer.stars() + " ⭐️ → " + offer.bonus() + " бонусов");
            inlineKeyboardButton.setCallbackData("BUY_STARS_" + offer.stars());
            rows.add(List.of(inlineKeyboardButton));
        }
        rows.add(List.of(createButton("🔙 Назад", "BACK_TO_MAIN")));
        return new InlineKeyboardMarkup(rows);
    }

    public InlineKeyboardMarkup getUploadCategoryKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (Category category : Category.values()) {
            rows.add(List.of(createButton(category.name(), "UPLOAD_CATEGORY_" + category.name())));
        }
        rows.add(List.of(createButton("🔙 Отмена", "UPLOAD_CANCEL")));
        return new InlineKeyboardMarkup(rows);
    }
}