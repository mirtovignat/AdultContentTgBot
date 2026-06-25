package com.example.demo.handler;

import com.example.demo.data_format.TimeFormater;
import com.example.demo.dto.BonusResult;
import com.example.demo.keyboard.InlineKeyboardFactory;
import com.example.demo.service.BonusService;
import com.example.demo.service.InvoiceSender;
import com.example.demo.service.StarsPaymentService;
import com.example.demo.service.StarsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.invoices.SendInvoice;
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
    private final StarsService starsService;
    private final StarsPaymentService starsPaymentService;
    private final InvoiceSender invoiceSender; // ✅ ВОТ ЭТО ТЫ ЗАБЫЛ

    @Override
    public List<Object> handle(Update update) {

        if (!update.hasCallbackQuery()) {
            return List.of();
        }

        CallbackQuery callback = update.getCallbackQuery();
        String data = callback.getData();

        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(callback.getId());

        Long chatId = callback.getMessage().getChatId();
        Integer messageId = callback.getMessage().getMessageId();

        if (data.startsWith("BUY_STARS_")) {

            int stars = Integer.parseInt(data.replace("BUY_STARS_", ""));
            int bonus = starsService.getBonusForStars(stars);

            String payload = starsPaymentService.generatePayload(bonus);

            SendInvoice invoice = invoiceSender.createStarsInvoice(
                    chatId,
                    "Покупка бонусов",
                    bonus + " бонусов за " + stars + " ⭐",
                    stars,
                    payload
            );

            return List.of(answer, invoice);
        }

        switch (data) {

            case "BACK_TO_MAIN" -> {
                answer.setText("Возврат в меню");
                return List.of(answer);
            }

            case "UPDATE_TIME" -> {
                long left = bonusService.getLeftSeconds(chatId);

                EditMessageText edit = new EditMessageText();
                edit.setChatId(chatId.toString());
                edit.setMessageId(messageId);

                edit.setText(timeFormater.formatTime(left));
                return List.of(answer, edit);
            }

            case "GET" -> {
                BonusResult result = bonusService.claimBonus(chatId);

                EditMessageText edit = new EditMessageText();
                edit.setChatId(chatId.toString());
                edit.setMessageId(messageId);

                if (result.allowed()) {
                    edit.setText("🎁 бонус получен!");
                    answer.setText("OK");
                } else {
                    edit.setText("⏳ ещё нельзя");
                    answer.setText("WAIT");
                }

                return List.of(answer, edit);
            }
        }

        return List.of(answer);
    }
}