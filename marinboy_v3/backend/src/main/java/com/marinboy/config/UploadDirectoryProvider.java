package com.marinboy.config;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** 업로드 저장 경로를 한 번만 검증해 저장과 조회가 항상 같은 폴더를 사용하게 합니다. */
@Component
public class UploadDirectoryProvider {

    private final Path uploadDirectory;

    public UploadDirectoryProvider(@Value("${app.upload.directory}") String configuredDirectory) {
        if (configuredDirectory == null || configuredDirectory.isBlank()) {
            throw new IllegalStateException("UPLOAD_DIRECTORY가 비어 있습니다.");
        }

        Path configuredPath = Path.of(configuredDirectory).normalize();
        if (!configuredPath.isAbsolute()) {
            throw new IllegalStateException("UPLOAD_DIRECTORY는 절대 경로여야 합니다: " + configuredDirectory);
        }

        this.uploadDirectory = configuredPath;
    }

    public Path getUploadDirectory() {
        return uploadDirectory;
    }
}
