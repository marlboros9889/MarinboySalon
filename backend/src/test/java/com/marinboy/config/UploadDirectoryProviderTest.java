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
        // 정상 절대 경로는 정규화된 업로드 기준 경로로 그대로 사용해야 합니다.
        UploadDirectoryProvider provider = new UploadDirectoryProvider(temporaryDirectory.toString());

        assertThat(provider.getUploadDirectory()).isEqualTo(temporaryDirectory.normalize());
    }

    @Test
    void rejectsWorkingDirectoryRelativePath() {
        // 실행 위치에 따라 달라지는 상대 경로를 거부해야 재시작 후 파일 유실을 예방할 수 있습니다.
        assertThatThrownBy(() -> new UploadDirectoryProvider("../uploads"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("절대 경로");
    }
}
