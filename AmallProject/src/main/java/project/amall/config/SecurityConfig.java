package project.amall.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;
import project.amall.config.security.CustomAuthenticationFailureHandler;
import project.amall.config.security.CustomAuthenticationSuccessHandler;
import project.amall.config.security.CustomUserDetailsService;

/**
 * Spring Security 설정 클래스
 *
 * Spring Boot 3.x / Spring Security 6.x 기준
 * - 인증/인가 설정
 * - 패스워드 암호화
 * - CSRF 보호
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final CustomAuthenticationSuccessHandler successHandler;
    private final CustomAuthenticationFailureHandler failureHandler;

    /**
     * 패스워드 암호화를 위한 BCryptPasswordEncoder Bean
     *
     * @return BCryptPasswordEncoder 인스턴스
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager Bean 등록
     *
     * @param authConfig AuthenticationConfiguration
     * @return AuthenticationManager
     * @throws Exception 설정 중 발생 가능한 예외
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Spring Security FilterChain 설정
     *
     * @param http HttpSecurity 객체
     * @return SecurityFilterChain
     * @throws Exception 설정 중 발생 가능한 예외
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 인증 규칙 설정
            .authorizeHttpRequests(auth -> auth
                // 정적 리소스는 모두 허용
                .requestMatchers(
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/fonts/**",
                    "/static/**"
                ).permitAll()

                // 회원가입, 로그인 페이지는 누구나 접근 가능
                .requestMatchers(
                    "/",
                    "/member/signUp",
                    "/member/login"
                ).permitAll()

                // 나머지 요청은 인증 필요
                .anyRequest().authenticated()
            )

            // 폼 기반 로그인 설정
            .formLogin(form -> form
                .loginPage("/member/login")           // 커스텀 로그인 페이지
                .loginProcessingUrl("/member/login")   // 로그인 처리 URL
                .usernameParameter("memberId")         // 사용자명 파라미터 (기본: username)
                .passwordParameter("memberPwd")        // 패스워드 파라미터 (기본: password)
                .successHandler(successHandler)        // 커스텀 성공 핸들러
                .failureHandler(failureHandler)        // 커스텀 실패 핸들러
                .permitAll()
            )

            // 로그아웃 설정
            .logout(logout -> logout
                .logoutUrl("/member/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )

            // 세션 관리
            .sessionManagement(session -> session
                .maximumSessions(1)                     // 동시 세션 1개로 제한
                .maxSessionsPreventsLogin(false)        // 새 로그인 시 기존 세션 만료
            )

            // CSRF 설정 (일단 활성화, 추후 API는 비활성화 가능)
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")     // API는 CSRF 제외 (추후 REST API용)
            );

        return http.build();
    }
}
