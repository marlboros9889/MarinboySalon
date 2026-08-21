package com.marinboy.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** v2의 이미지 저장 위치를 한 곳에서 관리하는 클래스입니다. */
@Component
public class UploadDirectoryProvider {

    private final Path uploadDirectory;

    public UploadDirectoryProvider(@Value("${app.upload.directory}") String configuredDirectory) {
        // 상대경로는 실행 위치에 따라 달라지므로 시작 단계에서 차단합니다.
        Path selectedDirectory = Paths.get(configuredDirectory).normalize();
        if (!selectedDirectory.isAbsolute()) {
            throw new IllegalStateException("V2_UPLOAD_DIRECTORY는 절대경로여야 합니다.");
        }

        try {
            Files.createDirectories(selectedDirectory);
        } catch (Exception exception) {
            throw new IllegalStateException("v2 업로드 폴더를 만들 수 없습니다: " + selectedDirectory, exception);
        }

        uploadDirectory = selectedDirectory;
    }

    public Path getUploadDirectory() {
        return uploadDirectory;
    }

    public Path getServiceDirectory() {
        return uploadDirectory.resolve("services");
    }
}
