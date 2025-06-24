package com.example.demo.setting.jwt;

import com.example.demo.customer.repository.CustomerRepository;
import com.example.demo.store.repository.StoreRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.io.IOException;
import java.util.List;
import java.util.Arrays;
//TODO: JwtAuthenticationFilter는 현재 어떤 API든 토큰 없이 호출 가능하기에 배포 직전에 이를 수정해야함
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomerRepository customerRepository;
    private final StoreRepository storeRepository;

    // 인증이 필요하지 않은 URL 패턴 목록 (SecurityConfig와 일치하게 유지)
    private final List<RequestMatcher> permitAllMatchers = Arrays.asList(
            new AntPathRequestMatcher("/api/stores/login"),
            new AntPathRequestMatcher("/api/customer/login"), // 고객 로그인 API
            new AntPathRequestMatcher("/api/oauth2/kakao/login"),
            new AntPathRequestMatcher("/api/oauth2/naver/login"),
            new AntPathRequestMatcher("/OAuth2/login/kakao"), // 카카오 로그인 콜백
            new AntPathRequestMatcher("/login/naver"), // 네이버 로그인 콜백
            new AntPathRequestMatcher("/owner/login"), //프론트엔드 상점 로그인 화면
            new AntPathRequestMatcher("/customer/login"), //프론트엔드 고객 로그인 화면
            new AntPathRequestMatcher("/api/auth/refresh-token"),
            new AntPathRequestMatcher("/api/v1/kakao-pay/ready"),
            new AntPathRequestMatcher("/v3/api-docs/**"),  // Swagger API 문서
            new AntPathRequestMatcher("/swagger-ui/**"),
            new AntPathRequestMatcher("/error"),
            new AntPathRequestMatcher("/manifest.json"),
            new AntPathRequestMatcher("/robots.txt"),
            new AntPathRequestMatcher("/**", "OPTIONS") // OPTIONS 메소드는 허용
    );


    private String extractToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> "access_token".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private boolean isPermitAllUrl(HttpServletRequest request) {
        return permitAllMatchers.stream()
                .anyMatch(matcher -> matcher.matches(request));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 인증이 필요 없는 경로는 필터를 통과시킵니다.
        if (isPermitAllUrl(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);

        if (token != null) {
            if (jwtTokenProvider.validateToken(token)) {
                // 토큰이 유효하면 인증 정보를 설정합니다.
                String userId = jwtTokenProvider.getUserId(token);
                String role = jwtTokenProvider.getRole(token);

                if (role != null && role.equals("ROLE_STORE")) {
                    storeRepository.findByOwnerEmail(userId).ifPresent(store -> {
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(store, null, null);
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    });
                } else if (role != null && role.equals("ROLE_CUSTOMER")) {
                    customerRepository.findByEmail(userId).ifPresent(customer -> {
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(customer, null, null);
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    });
                }
            } else {
                // 토큰이 유효하지 않은 경우(만료 등), 401 에러를 응답합니다.
                log.warn("유효하지 않은 JWT 토큰입니다. URI: {}", request.getRequestURI());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT Token");
                return; // 필터 체인 중단
            }
        }

        filterChain.doFilter(request, response);
    }
}