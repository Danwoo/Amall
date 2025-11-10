package project.amall.member.dto.response;

import lombok.Builder;
import lombok.Getter;
import project.amall.member.dto.MemberDto;

/**
 * 회원 응답 DTO
 *
 * 민감한 정보(패스워드)를 제외하고 반환
 */
@Getter
@Builder
public class MemberResponse {

    private String memberId;
    private String memberName;
    private String memberBirth;
    private String memberEmail;
    private String memberPhone;
    private Integer memberSex;
    private String memberRegDate;
    private String memberConnectDate;
    private String memberHash;
    private String memberMatchId;
    private String memberPostCode;
    private String memberAddress;
    private String memberDetailAddress;
    private String memberExtraAddress;

    /**
     * MemberDto를 MemberResponse로 변환
     */
    public static MemberResponse from(MemberDto dto) {
        if (dto == null) {
            return null;
        }

        return MemberResponse.builder()
                .memberId(dto.getMemberId())
                .memberName(dto.getMemberName())
                .memberBirth(dto.getMemberBirth())
                .memberEmail(dto.getMemberEmail())
                .memberPhone(dto.getMemberPhone())
                .memberSex(dto.getMemberSex())
                .memberRegDate(dto.getMemberRegDate())
                .memberConnectDate(dto.getMemberConnectDate())
                .memberHash(dto.getMemberHash())
                .memberMatchId(dto.getMemberMatchId())
                .memberPostCode(dto.getMemberPostCode())
                .memberAddress(dto.getMemberAddress())
                .memberDetailAddress(dto.getMemberDetailAddress())
                .memberExtraAddress(dto.getMemberExtraAddress())
                .build();
    }
}
