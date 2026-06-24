package com.example.demo.handler;

import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public interface Handler {

    List<Object> handle(Update update);

}