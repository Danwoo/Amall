package project.amall.common.util;

import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * 회원 해시 코드 생성 유틸리티
 *
 * 회원 매칭용 고유 해시 코드 생성
 * (주의: 패스워드 암호화용이 아님!)
 */
@Slf4j
public class MemberHashGenerator {

    /**
     * 회원 ID로 해시 코드 생성 (MD5)
     *
     * @deprecated MD5는 보안상 권장되지 않음. 추후 UUID 기반으로 변경 예정
     * @param memberId 회원 ID
     * @return 해시 코드
     */
    @Deprecated
    public static String generateWithMD5(String memberId) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(memberId.getBytes());
            byte[] byteData = md.digest();

            StringBuilder sb = new StringBuilder();
            for (byte b : byteData) {
                sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }

            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            log.error("MD5 알고리즘을 찾을 수 없습니다.", e);
            // Fallback: UUID 사용
            return generateWithUUID();
        }
    }

    /**
     * UUID 기반 해시 코드 생성 (권장)
     *
     * @return UUID 해시 코드
     */
    public static String generateWithUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 회원 ID로 해시 코드 생성 (기본 메서드)
     *
     * 현재는 MD5를 사용하지만, 추후 UUID로 변경 가능
     *
     * @param memberId 회원 ID
     * @return 해시 코드
     */
    public static String generate(String memberId) {
        // 현재는 기존 로직과의 호환성을 위해 MD5 사용
        // 추후 다음과 같이 변경 가능:
        // return generateWithUUID();
        return generateWithMD5(memberId);
    }
}
