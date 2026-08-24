package com.marinboy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.marinboy.config.UploadDirectoryProvider;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

/** 이미지 도구가 허용 형식만 저장하고 정리까지 독립적으로 수행하는지 확인합니다. */
class ServiceImageToolTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void savesAllowedImageWithServerGeneratedNameAndDeletesIt() {
        ServiceImageTool tool = new ServiceImageTool(
                new UploadDirectoryProvider(temporaryDirectory.toString()));
        MockMultipartFile image = new MockMultipartFile(
                "image", "customer-name.png", "image/png", new byte[] {1, 2, 3});

        String imageUrl = tool.save(image);
        Path savedPath = temporaryDirectory.resolve("services").resolve(
                imageUrl.substring("/uploads/services/".length()));

        assertThat(imageUrl).startsWith("/uploads/services/").endsWith(".png");
        assertThat(savedPath).exists();
        tool.deleteNow(List.of(imageUrl));
        assertThat(Files.exists(savedPath)).isFalse();
    }

    @Test
    void rejectsNonImageUploadBeforeWritingFile() {
        ServiceImageTool tool = new ServiceImageTool(
                new UploadDirectoryProvider(temporaryDirectory.toString()));
        MockMultipartFile textFile = new MockMultipartFile(
                "image", "memo.txt", "text/plain", "not-an-image".getBytes());

        assertThatThrownBy(() -> tool.save(textFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미지 파일만");
    }
}
