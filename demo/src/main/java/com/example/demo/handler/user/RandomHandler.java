package com.example.demo.handler.user;

import com.example.demo.handler.Handler;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class RandomHandler implements Handler {
    @Override
    public List<Object> handle(Update update) {
        return List.of();
    }
}
