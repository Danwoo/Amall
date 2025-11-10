package project.amall.member.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 매칭 응답 DTO
 */
@Getter
@Builder
public class MatchingResponse {

    private String myMemberId;
    private String myMemberName;
    private String partnerMemberId;
    private String partnerMemberName;
    private String matchingStatus;

    public static MatchingResponse of(String myId, String myName, String partnerId, String partnerName) {
        return MatchingResponse.builder()
                .myMemberId(myId)
                .myMemberName(myName)
                .partnerMemberId(partnerId)
                .partnerMemberName(partnerName)
                .matchingStatus("MATCHED")
                .build();
    }
}
