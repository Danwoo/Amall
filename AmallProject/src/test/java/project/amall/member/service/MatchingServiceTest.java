package project.amall.member.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import project.amall.alarm.dto.AlarmDto;
import project.amall.alarm.service.AlarmService;
import project.amall.common.exception.BusinessException;
import project.amall.common.exception.code.ErrorCode;
import project.amall.member.dto.MemberDto;
import project.amall.member.mapper.MemberMapper;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * MatchingService 단위 테스트
 *
 * Mockito를 사용한 순수 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MatchingService 테스트")
class MatchingServiceTest {

    @InjectMocks
    private MatchingService matchingService;

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private AlarmService alarmService;

    @Test
    @DisplayName("매칭 요청 성공")
    void sendMatchingRequest_Success() {
        // given
        String myHash = "hash1";
        String yourHash = "hash2";

        MemberDto myDto = createMember("user1", null);
        MemberDto yourDto = createMember("user2", null);

        given(memberMapper.getDtoUseHash(myHash)).willReturn(myDto);
        given(memberMapper.getDtoUseHash(yourHash)).willReturn(yourDto);
        given(alarmService.CheckSend(anyString(), anyString())).willReturn(null);

        // when
        matchingService.sendMatchingRequest(myHash, yourHash);

        // then
        then(alarmService).should(times(1))
                .sendMatchingMassage(anyString(), eq("user1"), eq("user2"));
    }

    @Test
    @DisplayName("이미 매칭된 상태에서 요청 시 예외 발생")
    void sendMatchingRequest_AlreadyMatched() {
        // given
        String myHash = "hash1";
        String yourHash = "hash2";

        MemberDto myDto = createMember("user1", "partner1");  // 이미 매칭됨

        given(memberMapper.getDtoUseHash(myHash)).willReturn(myDto);

        // when & then
        assertThatThrownBy(() -> matchingService.sendMatchingRequest(myHash, yourHash))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_ALREADY_MATCHED);
    }

    @Test
    @DisplayName("존재하지 않는 상대방에게 요청 시 예외 발생")
    void sendMatchingRequest_PartnerNotFound() {
        // given
        String myHash = "hash1";
        String yourHash = "invalid";

        MemberDto myDto = createMember("user1", null);

        given(memberMapper.getDtoUseHash(myHash)).willReturn(myDto);
        given(memberMapper.getDtoUseHash(yourHash)).willReturn(null);  // 상대방 없음

        // when & then
        assertThatThrownBy(() -> matchingService.sendMatchingRequest(myHash, yourHash))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_MEMBER_HASH);
    }

    @Test
    @DisplayName("자기 자신에게 요청 시 예외 발생")
    void sendMatchingRequest_SelfMatching() {
        // given
        String myHash = "hash1";
        String yourHash = "hash1";

        MemberDto myDto = createMember("user1", null);

        given(memberMapper.getDtoUseHash(myHash)).willReturn(myDto);
        given(memberMapper.getDtoUseHash(yourHash)).willReturn(myDto);

        // when & then
        assertThatThrownBy(() -> matchingService.sendMatchingRequest(myHash, yourHash))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SELF_MATCHING_NOT_ALLOWED);
    }

    @Test
    @DisplayName("이미 매칭 요청을 보낸 경우 예외 발생")
    void sendMatchingRequest_AlreadySent() {
        // given
        String myHash = "hash1";
        String yourHash = "hash2";

        MemberDto myDto = createMember("user1", null);
        MemberDto yourDto = createMember("user2", null);
        AlarmDto existingAlarm = new AlarmDto();  // 이미 알람 존재

        given(memberMapper.getDtoUseHash(myHash)).willReturn(myDto);
        given(memberMapper.getDtoUseHash(yourHash)).willReturn(yourDto);
        given(alarmService.CheckSend("user1", "user2")).willReturn(existingAlarm);

        // when & then
        assertThatThrownBy(() -> matchingService.sendMatchingRequest(myHash, yourHash))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MATCHING_REQUEST_ALREADY_SENT);
    }

    @Test
    @DisplayName("매칭 수락 성공")
    void acceptMatching_Success() {
        // given
        String myHash = "hash1";
        String alarmId = "hash1hash2";

        MemberDto myDto = createMember("user1", null);
        myDto.setMemberName("User One");

        MemberDto yourDto = createMember("user2", null);
        yourDto.setMemberName("User Two");

        given(memberMapper.getDtoUseHash(myHash)).willReturn(myDto);
        given(memberMapper.getDtoUseHash("hash2")).willReturn(yourDto);

        // when
        var result = matchingService.acceptMatching(myHash, alarmId);

        // then
        assertThat(result.getMyMemberId()).isEqualTo("user1");
        assertThat(result.getPartnerMemberId()).isEqualTo("user2");
        assertThat(result.getMatchingStatus()).isEqualTo("MATCHED");

        then(memberMapper).should(times(1)).updateMatchId("user1", "user2");
        then(memberMapper).should(times(1)).updateMatchId("user2", "user1");
        then(alarmService).should(times(1)).deleteAllAlarm("user1", "user2");
    }

    // Helper 메서드
    private MemberDto createMember(String memberId, String matchId) {
        MemberDto dto = new MemberDto();
        dto.setMemberId(memberId);
        dto.setMemberMatchId(matchId);
        dto.setMemberHash("hash_" + memberId);
        return dto;
    }
}
