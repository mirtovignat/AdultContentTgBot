package com.example.demo.handler.user;

import com.example.demo.dto.UserProfileDto;
import com.example.demo.entity.User;
import com.example.demo.handler.Handler;
import com.example.demo.mapper.UserMapper;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProfileHandler implements Handler {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<Object> handle(Update update) {
        Long chatId = update.getMessage().getChatId();
        User user = userRepository.findByChatIdOrThrow(chatId);
        UserProfileDto userProfileDto = userMapper.toDto(user);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        sendMessage.setText(userProfileDto.toString());
        return List.of(sendMessage);
    }
}