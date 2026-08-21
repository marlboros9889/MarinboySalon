package com.marinboy.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

// 실행 폴더가 바뀌어도 업로드 위치가 달라지지 않도록 절대 경로 규칙을 검증합니다.
class UploadDirectoryProviderTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void acceptsAbsoluteUploadDirectory() {
        UploadDirectoryProvider provider = new UploadDirectoryProvider(temporaryDirectory.toString());

        assertThat(provider.getUploadDirectory()).isEqualTo(temporaryDirectory.normalize());
    }

    @Test
    void rejectsWorkingDirectoryRelativePath() {
        assertThatThrownBy(() -> new UploadDirectoryProvider("../uploads"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("절대 경로");
    }
}
