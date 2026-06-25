package com.example.demo.service;

import com.example.demo.entity.Category;
import com.example.demo.entity.FileType;
import com.example.demo.entity.PornFile;
import com.example.demo.entity.User;
import com.example.demo.repository.PornoFileRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PornFileService {

    private final PornoFileRepository pornoFileRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    public PornFile saveFile(InputStream inputStream, String fileName, FileType fileType, Category category, Long userId, long size) throws IOException {
        String extension = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.')) : "";
        String objectKey = UUID.randomUUID() + extension;
        String contentType = fileType == FileType.IMAGE ? "image/jpeg" : "video/mp4";

        storageService.upload(inputStream, objectKey, contentType, size);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        PornFile pornFile = new PornFile();
        pornFile.setUser(user);
        pornFile.setFileName(fileName);
        pornFile.setFilePath(objectKey);
        pornFile.setFileType(fileType);
        pornFile.setCategory(category);
        pornFile.setSizeBytes(size);

        return pornoFileRepository.save(pornFile);
    }
    @Transactional
    public PornFile saveFile(MultipartFile file, Long userId, Category category) throws IOException {
        String originalFileName = file.getOriginalFilename();
        String extension = originalFileName != null && originalFileName.contains(".")
                ? originalFileName.substring(originalFileName.lastIndexOf('.'))
                : "";
        String objectKey = UUID.randomUUID() + extension;

        storageService.upload(file.getBytes(), objectKey, file.getContentType());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        PornFile pornFile = new PornFile();
        pornFile.setUser(user);
        pornFile.setFileName(originalFileName);
        pornFile.setFilePath(objectKey);
        pornFile.setFileType(detectFileType(file));
        pornFile.setCategory(category);
        pornFile.setSizeBytes(file.getSize());

        return pornoFileRepository.save(pornFile);
    }

    public InputStream downloadFile(PornFile pornFile) {
        return storageService.download(pornFile.getFilePath());
    }

    public InputStream downloadFile(String objectKey) {
        return storageService.download(objectKey);
    }

    @Transactional
    public void deleteFile(PornFile pornFile) {
        storageService.delete(pornFile.getFilePath());
        pornoFileRepository.delete(pornFile);
    }

    public List<PornFile> getFilesByCategory(Category category, int limit) {
        return pornoFileRepository.findByCategory(category, limit);
    }

    public String getFileUrl(PornFile pornFile) {
        return storageService.getFileUrl(pornFile.getFilePath());
    }

    private FileType detectFileType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null && contentType.startsWith("image")) {
            return FileType.IMAGE;
        } else {
            return FileType.VIDEO;
        }
    }
}