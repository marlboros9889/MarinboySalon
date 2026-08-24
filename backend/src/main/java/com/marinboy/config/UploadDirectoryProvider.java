package com.marinboy.config;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** 업로드 저장 경로를 한 번만 검증해 저장과 조회가 항상 같은 폴더를 사용하게 합니다. */
@Component
public class UploadDirectoryProvider {

    private final Path uploadDirectory;

    public UploadDirectoryProvider(@Value("${app.upload.directory}") String configuredDirectory) {
        // 설정 누락을 서버 시작 때 바로 발견해 파일이 임의의 실행 폴더에 저장되지 않게 합니다.
        if (configuredDirectory == null || configuredDirectory.isBlank()) {
            throw new IllegalStateException("UPLOAD_DIRECTORY가 비어 있습니다.");
        }

        Path configuredPath = Path.of(configuredDirectory).normalize();
        // 절대 경로만 허용해 IDE·JAR·재시작 위치가 달라도 같은 업로드 폴더를 사용합니다.
        if (!configuredPath.isAbsolute()) {
            throw new IllegalStateException("UPLOAD_DIRECTORY는 절대 경로여야 합니다: " + configuredDirectory);
        }

        this.uploadDirectory = configuredPath;
    }

    public Path getUploadDirectory() {
        return uploadDirectory;
    }
}
