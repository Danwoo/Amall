package project.amall.member.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.amall.common.response.ApiResponse;
import project.amall.common.util.MemberHashGenerator;
import project.amall.member.dto.MemberDto;
import project.amall.member.dto.request.MatchingRequest;
import project.amall.member.dto.request.MemberSignUpRequest;
import project.amall.member.dto.request.MemberUpdateRequest;
import project.amall.member.dto.response.MemberResponse;
import project.amall.member.dto.response.MatchingResponse;
import project.amall.member.service.MatchingService;
import project.amall.member.service.MemberService;

/**
 * 회원 관리 RESTful API Controller
 *
 * 새로운 표준 API 엔드포인트 제공
 * - 유효성 검증 (@Valid)
 * - 표준 응답 형식 (ApiResponse)
 * - 예외 처리 자동화 (GlobalExceptionHandler)
 */
@Slf4j
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;
    private final MatchingService matchingService;

    /**
     * 회원가입
     *
     * POST /api/members
     *
     * @param request 회원가입 요청 (유효성 검증 포함)
     * @return 생성된 회원 정보
     */
    @PostMapping
    public ResponseEntity<ApiResponse<MemberResponse>> signUp(
            @Valid @RequestBody MemberSignUpRequest request) {

        log.info("회원가입 요청: memberId={}", request.getMemberId());

        // ID 중복 체크
        if (!memberService.isIdAvailable(request.getMemberId())) {
            throw new project.amall.common.exception.BusinessException(
                    project.amall.common.exception.code.ErrorCode.MEMBER_ALREADY_EXISTS
            );
        }

        // DTO 변환
        MemberDto memberDto = new MemberDto();
        memberDto.setMemberId(request.getMemberId());
        memberDto.setMemberName(request.getMemberName());
        memberDto.setMemberBirth(request.getMemberBirth());
        memberDto.setMemberPwd(request.getMemberPwd());
        memberDto.setMemberEmail(request.getMemberEmail());
        memberDto.setMemberPhone(request.getMemberPhone());
        memberDto.setMemberSex(request.getMemberSex());
        memberDto.setMemberPostCode(request.getMemberPostCode());
        memberDto.setMemberAddress(request.getMemberAddress());
        memberDto.setMemberDetailAddress(request.getMemberDetailAddress());
        memberDto.setMemberExtraAddress(request.getMemberExtraAddress());

        // 회원 해시 생성
        String memberHash = MemberHashGenerator.generate(request.getMemberId());
        memberDto.setMemberHash(memberHash);

        // 회원가입 처리
        try {
            memberService.signUpMember(memberDto);
            log.info("회원가입 성공: memberId={}, hash={}", request.getMemberId(), memberHash);
        } catch (Exception e) {
            log.error("회원가입 실패", e);
            throw new RuntimeException("회원가입에 실패했습니다.", e);
        }

        // 응답 생성
        MemberDto created = memberService.reloadMemberData(request.getMemberId());
        MemberResponse response = MemberResponse.from(created);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("회원가입이 완료되었습니다.", response));
    }

    /**
     * 회원 정보 조회
     *
     * GET /api/members/{memberId}
     *
     * @param memberId 회원 ID
     * @return 회원 정보
     */
    @GetMapping("/{memberId}")
    public ResponseEntity<ApiResponse<MemberResponse>> getMember(
            @PathVariable String memberId) {

        log.info("회원 조회: memberId={}", memberId);

        MemberDto member = memberService.getMemberOrThrow(memberId);
        MemberResponse response = MemberResponse.from(member);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 회원 정보 수정
     *
     * PUT /api/members/{memberId}
     *
     * @param memberId 회원 ID
     * @param request 수정 요청
     * @return 수정된 회원 정보
     */
    @PutMapping("/{memberId}")
    public ResponseEntity<ApiResponse<MemberResponse>> updateMember(
            @PathVariable String memberId,
            @Valid @RequestBody MemberUpdateRequest request) {

        log.info("회원 정보 수정: memberId={}", memberId);

        // 기존 회원 조회
        MemberDto member = memberService.getMemberOrThrow(memberId);

        // 수정 내용 반영
        if (request.getMemberName() != null) {
            member.setMemberName(request.getMemberName());
        }
        if (request.getMemberPwd() != null) {
            member.setMemberPwd(request.getMemberPwd());
        }
        if (request.getMemberEmail() != null) {
            member.setMemberEmail(request.getMemberEmail());
        }
        if (request.getMemberPhone() != null) {
            member.setMemberPhone(request.getMemberPhone());
        }
        if (request.getMemberPostCode() != null) {
            member.setMemberPostCode(request.getMemberPostCode());
        }
        if (request.getMemberAddress() != null) {
            member.setMemberAddress(request.getMemberAddress());
        }
        if (request.getMemberDetailAddress() != null) {
            member.setMemberDetailAddress(request.getMemberDetailAddress());
        }
        if (request.getMemberExtraAddress() != null) {
            member.setMemberExtraAddress(request.getMemberExtraAddress());
        }

        // 업데이트
        memberService.updateMember(member);

        // 최신 정보 조회
        MemberDto updated = memberService.reloadMemberData(memberId);
        MemberResponse response = MemberResponse.from(updated);

        return ResponseEntity.ok(ApiResponse.success("회원 정보가 수정되었습니다.", response));
    }

    /**
     * 회원 탈퇴
     *
     * DELETE /api/members/{memberId}
     *
     * @param memberId 회원 ID
     * @return 성공 메시지
     */
    @DeleteMapping("/{memberId}")
    public ResponseEntity<ApiResponse<Void>> deleteMember(
            @PathVariable String memberId) {

        log.info("회원 탈퇴: memberId={}", memberId);

        MemberDto member = memberService.getMemberOrThrow(memberId);
        memberService.deleteMember(member);

        return ResponseEntity.ok(ApiResponse.success("회원 탈퇴가 완료되었습니다."));
    }

    /**
     * ID 중복 체크
     *
     * GET /api/members/check-id?memberId=xxx
     *
     * @param memberId 체크할 ID
     * @return 사용 가능 여부
     */
    @GetMapping("/check-id")
    public ResponseEntity<ApiResponse<Boolean>> checkIdAvailability(
            @RequestParam String memberId) {

        log.info("ID 중복 체크: memberId={}", memberId);

        boolean available = memberService.isIdAvailable(memberId);

        String message = available
                ? "사용 가능한 ID입니다."
                : "이미 사용 중인 ID입니다.";

        return ResponseEntity.ok(ApiResponse.success(message, available));
    }

    /**
     * 커플 매칭 요청 전송
     *
     * POST /api/members/{memberId}/matching/request
     *
     * @param memberId 내 ID
     * @param request 매칭 요청 (상대방 해시 코드)
     * @return 성공 메시지
     */
    @PostMapping("/{memberId}/matching/request")
    public ResponseEntity<ApiResponse<Void>> sendMatchingRequest(
            @PathVariable String memberId,
            @Valid @RequestBody MatchingRequest request) {

        log.info("매칭 요청: from={}, to={}", memberId, request.getMemberMatchHash());

        // 내 정보 조회
        MemberDto myMember = memberService.getMemberOrThrow(memberId);
        String myHash = myMember.getMemberHash();

        // 매칭 요청 전송
        matchingService.sendMatchingRequest(myHash, request.getMemberMatchHash());

        return ResponseEntity.ok(ApiResponse.success("매칭 요청이 전송되었습니다."));
    }

    /**
     * 커플 매칭 수락
     *
     * POST /api/members/{memberId}/matching/accept/{alarmId}
     *
     * @param memberId 내 ID
     * @param alarmId 알람 ID
     * @return 매칭 결과
     */
    @PostMapping("/{memberId}/matching/accept/{alarmId}")
    public ResponseEntity<ApiResponse<MatchingResponse>> acceptMatching(
            @PathVariable String memberId,
            @PathVariable String alarmId) {

        log.info("매칭 수락: memberId={}, alarmId={}", memberId, alarmId);

        // 내 정보 조회
        MemberDto myMember = memberService.getMemberOrThrow(memberId);
        String myHash = myMember.getMemberHash();

        // 매칭 수락
        MatchingResponse response = matchingService.acceptMatching(myHash, alarmId);

        return ResponseEntity.ok(ApiResponse.success("매칭이 완료되었습니다.", response));
    }

    /**
     * 커플 매칭 해제
     *
     * DELETE /api/members/{memberId}/matching
     *
     * @param memberId 회원 ID
     * @return 성공 메시지
     */
    @DeleteMapping("/{memberId}/matching")
    public ResponseEntity<ApiResponse<Void>> cancelMatching(
            @PathVariable String memberId) {

        log.info("매칭 해제: memberId={}", memberId);

        matchingService.cancelMatching(memberId);

        return ResponseEntity.ok(ApiResponse.success("매칭이 해제되었습니다."));
    }
}
