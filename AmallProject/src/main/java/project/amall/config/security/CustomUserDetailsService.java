package project.amall.config.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import project.amall.member.dto.MemberDto;
import project.amall.member.mapper.MemberMapper;

import java.util.Collections;

/**
 * Spring Security UserDetailsService 구현체
 *
 * 데이터베이스에서 사용자 정보를 조회하여 인증에 사용
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberMapper memberMapper;

    /**
     * 사용자 ID로 사용자 정보 조회
     *
     * @param username 사용자 ID (memberId)
     * @return UserDetails 객체
     * @throws UsernameNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // DB에서 사용자 조회
        MemberDto member = memberMapper.reloadMemberData(username);

        if (member == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
        }

        // UserDetails 객체 생성 및 반환
        // 기본 권한: ROLE_USER
        return User.builder()
                .username(member.getMemberId())
                .password(member.getMemberPwd())  // BCrypt로 암호화된 패스워드
                .authorities(Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
