package project.amall.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * MemberHashGenerator 테스트
 */
@DisplayName("MemberHashGenerator 테스트")
class MemberHashGeneratorTest {

    @Test
    @DisplayName("MD5 해시 생성 - 일관성 체크")
    void generateWithMD5_Consistency() {
        // given
        String memberId = "testuser123";

        // when
        String hash1 = MemberHashGenerator.generateWithMD5(memberId);
        String hash2 = MemberHashGenerator.generateWithMD5(memberId);

        // then
        assertThat(hash1).isEqualTo(hash2);
        assertThat(hash1).hasSize(32);  // MD5는 32자
    }

    @Test
    @DisplayName("UUID 해시 생성 - 유일성 체크")
    void generateWithUUID_Uniqueness() {
        // when
        String hash1 = MemberHashGenerator.generateWithUUID();
        String hash2 = MemberHashGenerator.generateWithUUID();

        // then
        assertThat(hash1).isNotEqualTo(hash2);
        assertThat(hash1).hasSize(32);  // UUID에서 하이픈 제거 = 32자
    }

    @Test
    @DisplayName("기본 generate 메서드")
    void generate() {
        // given
        String memberId = "user001";

        // when
        String hash = MemberHashGenerator.generate(memberId);

        // then
        assertThat(hash).isNotNull();
        assertThat(hash).isNotEmpty();
        assertThat(hash).hasSize(32);
    }
}
