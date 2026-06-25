package com.example.demo.service;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.invoices.SendInvoice;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;

import java.util.List;

@Component
public class InvoiceSender {

    public SendInvoice createStarsInvoice(Long chatId,
                                          String title,
                                          String description,
                                          int stars,
                                          String payload) {

        List<LabeledPrice> prices = List.of(
                new LabeledPrice("Stars", stars)
        );

        SendInvoice invoice = new SendInvoice();
        invoice.setChatId(chatId.toString());
        invoice.setTitle(title);
        invoice.setDescription(description);
        invoice.setPayload(payload);
        invoice.setCurrency("XTR");
        invoice.setPrices(prices);
        invoice.setProviderToken("STARS");

        return invoice;
    }
}