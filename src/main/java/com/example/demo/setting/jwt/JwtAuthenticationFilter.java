package com.example.demo.setting.jwt;

import com.example.demo.customer.repository.CustomerRepository;
import com.example.demo.store.repository.StoreRepository;
import com.example.demo.setting.util.TokenRedisService;
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

import java.io.IOException;
import java.util.Arrays;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomerRepository customerRepository;
    private final StoreRepository storeRepository;
    private final TokenRedisService tokenRedisService;

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

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        log.debug("[JWT Filter] 요청 URI: {}", requestURI);

        // 쿠키 정보 로깅
        if (request.getCookies() != null) {
            log.debug("[JWT Filter] 쿠키 개수: {}", request.getCookies().length);
            Arrays.stream(request.getCookies()).forEach(cookie ->
                log.debug("[JWT Filter] 쿠키: {}={}", cookie.getName(), cookie.getValue().substring(0, Math.min(20, cookie.getValue().length())) + "...")
            );
        } else {
            log.warn("[JWT Filter] 쿠키가 없습니다. URI: {}", requestURI);
        }

        String token = extractToken(request);
        log.debug("[JWT Filter] 추출된 토큰: {}", token != null ? "존재" : "없음");

        if (token != null) {
            // 1. 기본 JWT 검증 (서명, 만료시간 등)
            boolean isValidJwt = jwtTokenProvider.validateTokenWithoutRedisCheck(token);
            log.debug("[JWT Filter] 기본 JWT 검증 결과: {}", isValidJwt);

            // 2. Redis 무효화 리스트 확인
            boolean isTokenInvalidated = tokenRedisService.isTokenInvalidated(token);
            log.debug("[JWT Filter] 토큰 무효화 상태: {}", isTokenInvalidated);

            if (isValidJwt && !isTokenInvalidated) {
                // 토큰이 유효하고 무효화되지 않은 경우에만 인증 정보를 설정합니다.
                String userId = jwtTokenProvider.getUserId(token);
                String role = jwtTokenProvider.getRole(token);
                log.info("[JWT Filter] 유효한 토큰 - 사용자: {}, 역할: {}", userId, role);

                if (role != null && role.equals("ROLE_STORE")) {
                    storeRepository.findByOwnerEmail(userId).ifPresent(store -> {
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(store, null, null);
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        log.info("[JWT Filter] 매장 인증 완료: {}", store.getStoreName());
                    });
                } else if (role != null && role.equals("ROLE_CUSTOMER")) {
                    customerRepository.findByEmail(userId).ifPresent(customer -> {
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(customer, null, null);
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        log.info("[JWT Filter] 고객 인증 완료: {}", customer.getEmail());
                    });
                }
            } else {
                // 토큰이 유효하지 않거나 무효화된 경우
                if (!isValidJwt) {
                    log.warn("[JWT Filter] 유효하지 않은 JWT 토큰입니다. URI: {}", request.getRequestURI());
                } else if (isTokenInvalidated) {
                    log.warn("[JWT Filter] 🚫 무효화된 토큰 사용 시도 감지! URI: {}, 토큰: {}",
                        request.getRequestURI(), token.substring(0, Math.min(20, token.length())) + "...");
                }
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or Invalidated JWT Token");
                return; // 필터 체인 중단
            }
        } else {
            log.debug("[JWT Filter] 토큰이 없습니다. URI: {}", requestURI);
        }

        filterChain.doFilter(request, response);
    }
}