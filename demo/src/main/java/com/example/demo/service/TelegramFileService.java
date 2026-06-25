package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Service
public class TelegramFileService {

    @Value("${telegram.bot.token}")
    private String botToken;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot%s/";

    private String getFilePath(String fileId) throws TelegramApiException {
        String url = String.format(TELEGRAM_API_URL + "getFile", botToken);
        Map<String, String> request = Map.of("file_id", fileId);
        var response = restTemplate.postForObject(url, request, Map.class);
        if (response == null || !Boolean.TRUE.equals(response.get("ok"))) {
            throw new TelegramApiException("Не удалось получить путь к файлу");
        }
        Map<String, Object> result = (Map<String, Object>) response.get("result");
        return (String) result.get("file_path");
    }

    public InputStream downloadFileAsStream(String fileId) throws TelegramApiException {
        try {
            String filePath = getFilePath(fileId);
            String downloadUrl = String.format(TELEGRAM_API_URL + "file/bot%s/" + filePath, botToken);
            return restTemplate.execute(downloadUrl, HttpMethod.GET, null, new ResponseExtractor<InputStream>() {
                @Override
                public InputStream extractData(ClientHttpResponse response) throws IOException {
                    return response.getBody();
                }
            });
        } catch (Exception e) {
            throw new TelegramApiException("Ошибка скачивания файла", e);
        }
    }

    public long getFileSize(String fileId) throws TelegramApiException {
        String url = String.format(TELEGRAM_API_URL + "getFile", botToken);
        Map<String, String> request = Map.of("file_id", fileId);
        var response = restTemplate.postForObject(url, request, Map.class);
        if (response == null || !Boolean.TRUE.equals(response.get("ok"))) {
            throw new TelegramApiException("Не удалось получить информацию о файле");
        }
        Map<String, Object> result = (Map<String, Object>) response.get("result");
        return ((Number) result.get("file_size")).longValue();
    }
}