package project.amall.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 커플 매칭 요청 DTO
 */
@Data
public class MatchingRequest {

    @NotBlank(message = "상대방의 매칭 코드는 필수입니다.")
    private String memberMatchHash;
}
