package com.anime.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    public String store(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) {
            throw new com.anime.exception.BusinessException("file is empty");
        }

        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.'));
        }

        String fileName = UUID.randomUUID() + ext;
        Path dir = Path.of(uploadDir, subDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(dir);
            Path target = dir.resolve(fileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new com.anime.exception.BusinessException("file save failed");
        }

        // 返回前端可访问路径
        return "/uploads/" + subDir.replace("\\", "/") + "/" + fileName;
    }
}

