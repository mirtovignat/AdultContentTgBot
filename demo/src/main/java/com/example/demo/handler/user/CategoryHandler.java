package com.example.demo.handler.user;

import com.example.demo.entity.Category;
import com.example.demo.entity.FileType;
import com.example.demo.entity.PornFile;
import com.example.demo.handler.Handler;
import com.example.demo.keyboard.InlineKeyboardFactory;
import com.example.demo.service.PornFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CategoryHandler implements Handler {

    private final InlineKeyboardFactory keyboardFactory;
    private final PornFileService pornFileService;

    @Override
    public List<Object> handle(Update update) {
        // 1. Показать кнопки категорий при тексте "Категории"
        if (update.hasMessage() && update.getMessage().hasText() && "Категории".equals(update.getMessage().getText())) {
            Long chatId = update.getMessage().getChatId();
            SendMessage msg = new SendMessage();
            msg.setChatId(chatId.toString());
            msg.setText("Выберите категорию:");
            msg.setReplyMarkup(keyboardFactory.getCategoriesChoice());
            return List.of(msg);
        }

        // 2. Обработка выбора категории через callback
        if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();
            try {
                Category category = Category.valueOf(data);
                Long chatId = update.getCallbackQuery().getMessage().getChatId();

                List<PornFile> files = pornFileService.getFilesByCategory(category, 5);
                if (files.isEmpty()) {
                    SendMessage msg = new SendMessage();
                    msg.setChatId(chatId.toString());
                    msg.setText("В этой категории пока нет файлов.");
                    return List.of(msg);
                }

                List<Object> responses = new ArrayList<>();

                // Ответ на callback (убираем часы загрузки)
                AnswerCallbackQuery answer = new AnswerCallbackQuery();
                answer.setCallbackQueryId(update.getCallbackQuery().getId());
                answer.setText("Загружаем...");
                answer.setShowAlert(false);
                responses.add(answer);

                // Для каждого файла создаём SendPhoto или SendVideo
                for (PornFile file : files) {
                    try (InputStream inputStream = pornFileService.downloadFile(file)) {
                        InputFile inputFile = new InputFile(inputStream, file.getFileName());

                        if (file.getFileType() == FileType.IMAGE) {
                            SendPhoto sendPhoto = new SendPhoto();
                            sendPhoto.setChatId(chatId.toString());
                            sendPhoto.setPhoto(inputFile);
                            responses.add(sendPhoto);
                        } else {
                            SendVideo sendVideo = new SendVideo();
                            sendVideo.setChatId(chatId.toString());
                            sendVideo.setVideo(inputFile);
                            responses.add(sendVideo);
                        }
                    } catch (Exception e) {
                        // Если ошибка, отправляем сообщение
                        SendMessage errorMsg = new SendMessage();
                        errorMsg.setChatId(chatId.toString());
                        errorMsg.setText("Не удалось загрузить файл: " + file.getFileName());
                        responses.add(errorMsg);
                    }
                }

                return responses;

            } catch (IllegalArgumentException e) {
                // Неизвестная категория – ничего не делаем
                return List.of();
            }
        }

        return List.of();
    }
}