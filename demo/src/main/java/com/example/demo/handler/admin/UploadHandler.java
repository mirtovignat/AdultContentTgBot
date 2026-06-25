package com.example.demo.handler.admin;

import com.example.demo.entity.Category;
import com.example.demo.entity.FileType;
import com.example.demo.entity.Role;
import com.example.demo.entity.PornFile;
import com.example.demo.handler.Handler;
import com.example.demo.keyboard.InlineKeyboardFactory;
import com.example.demo.service.PornFileService;
import com.example.demo.service.TelegramFileService;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class UploadHandler implements Handler {

    private final PornFileService pornFileService;
    private final UserService userService;
    private final TelegramFileService telegramFileService;
    private final InlineKeyboardFactory keyboardFactory;

    private final Map<Long, UploadSession> sessions = new ConcurrentHashMap<>();

    @Override
    public List<Object> handle(Update update) {
        Long chatId = update.getMessage().getChatId();

        var user = userService.getByChatId(chatId);
        if (user.getRole() != Role.ADMIN) {
            return List.of(createMessage(chatId, "❌ Только для администратора."));
        }

        if (update.hasMessage() && update.getMessage().hasText() && "Загрузить файл".equals(update.getMessage().getText())) {
            sessions.put(chatId, new UploadSession());
            return List.of(createMessage(chatId, "📤 Отправьте фото или видео для загрузки."));
        }

        if (update.hasMessage()) {
            UploadSession session = sessions.get(chatId);
            if (session == null) return List.of();

            Message msg = update.getMessage();

            if (msg.hasPhoto()) {
                var photo = msg.getPhoto().get(msg.getPhoto().size() - 1);
                session.setFileId(photo.getFileId());
                session.setFileType(FileType.IMAGE);
                session.setFileName("photo_" + System.currentTimeMillis() + ".jpg");
            } else if (msg.hasVideo()) {
                var video = msg.getVideo();
                session.setFileId(video.getFileId());
                session.setFileType(FileType.VIDEO);
                session.setFileName(video.getFileName() != null ? video.getFileName() : "video_" + System.currentTimeMillis() + ".mp4");
            } else {
                return List.of(createMessage(chatId, "❌ Пожалуйста, отправьте фото или видео."));
            }

            return showCategorySelection(chatId);
        }

        if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();
            if (data.startsWith("UPLOAD_CATEGORY_")) {
                Category category = Category.valueOf(data.replace("UPLOAD_CATEGORY_", ""));
                UploadSession session = sessions.get(chatId);
                if (session == null || session.getFileId() == null) {
                    return List.of(createMessage(chatId, "❌ Сессия истекла. Начните заново."));
                }

                try {
                    InputStream inputStream = telegramFileService.downloadFileAsStream(session.getFileId());
                    long fileSize = telegramFileService.getFileSize(session.getFileId());

                    Long userId = userService.getByChatId(chatId).getId();

                    PornFile savedFile = pornFileService.saveFile(
                            inputStream,
                            session.getFileName(),
                            session.getFileType(),
                            category,
                            userId,
                            fileSize
                    );

                    sessions.remove(chatId);
                    return List.of(createMessage(chatId, "✅ Файл успешно загружен!\n" +
                            "Имя: " + savedFile.getFileName() + "\n" +
                            "Категория: " + savedFile.getCategory()));
                } catch (Exception e) {
                    e.printStackTrace();
                    return List.of(createMessage(chatId, "❌ Ошибка загрузки: " + e.getMessage()));
                }
            } else if ("UPLOAD_CANCEL".equals(data)) {
                sessions.remove(chatId);
                return List.of(createMessage(chatId, "❌ Загрузка отменена."));
            }
        }

        return List.of();
    }

    private List<Object> showCategorySelection(Long chatId) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId.toString());
        msg.setText("Выберите категорию для загружаемого файла:");
        msg.setReplyMarkup(keyboardFactory.getUploadCategoryKeyboard());
        return List.of(msg);
    }

    private SendMessage createMessage(Long chatId, String text) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId.toString());
        msg.setText(text);
        return msg;
    }

    private static class UploadSession {
        private String fileId;
        private FileType fileType;
        private String fileName;
        public String getFileId() { return fileId; }
        public void setFileId(String fileId) { this.fileId = fileId; }
        public FileType getFileType() { return fileType; }
        public void setFileType(FileType fileType) { this.fileType = fileType; }
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
    }
}