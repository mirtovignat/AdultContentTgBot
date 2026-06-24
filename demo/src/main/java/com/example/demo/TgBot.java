package com.example.demo;

import com.example.demo.dispatcher.UpdateDispatcher;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TgBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.token}")
    private String token;

    @Value("${telegram.bot.username}")
    private String username;

    private final UpdateDispatcher dispatcher;

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        List<Object> responses = dispatcher.dispatch(update);
        for (Object response : responses) {
            try {
                if (response instanceof SendMessage sendMessage) {
                    execute(sendMessage);
                } else if (response instanceof SendPhoto sendPhoto) {
                    execute(sendPhoto);
                } else if (response instanceof EditMessageText editMessageText) {
                    execute(editMessageText);
                } else if (response instanceof AnswerCallbackQuery answerCallbackQuery) {
                    execute(answerCallbackQuery);
                }
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    @PostConstruct
    public void registerBot() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(this);
            setMyCommands();
        } catch (TelegramApiException e) {
            e.printStackTrace();
            throw new RuntimeException("Bot registration failed", e);
        }
    }

    private void setMyCommands() throws TelegramApiException {
        SetMyCommands setMyCommands = new SetMyCommands();
        setMyCommands.setCommands(List.of(
                new BotCommand("start", "Главное меню"),
                new BotCommand("help", "Помощь по командам"),
                new BotCommand("cancel", "Отменить действие")
        ));
        execute(setMyCommands);
    }
}