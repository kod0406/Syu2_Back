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

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomerRepository customerRepository;
    private final StoreRepository storeRepository;

    // 인증이 필요하지 않은 URL 패턴 목록 (SecurityConfig와 일치하게 유지)
    private final List<RequestMatcher> permitAllMatchers = Arrays.asList(
            new AntPathRequestMatcher("/greeting"),
            new AntPathRequestMatcher("/login/**"),
            new AntPathRequestMatcher("/oauth2/**"),
            new AntPathRequestMatcher("/api/auth/refresh-token"),
            new AntPathRequestMatcher("/api/v1/kakao-pay/ready"),
            new AntPathRequestMatcher("/error"),
            new AntPathRequestMatcher("/favicon.ico"),
            new AntPathRequestMatcher("/logo.png"),
            new AntPathRequestMatcher("/_next/**"),
            new AntPathRequestMatcher("/static/**"),
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
        if (isPermitAllUrl(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = extractToken(request);
        // API 경로에 대해서는 토큰이 없거나 유효하지 않으면 직접 401 응답

        log.info("JWT token토큰토큰아: {}", token);

        if (request.getRequestURI().startsWith("/api/")) {
            if (token == null || !jwtTokenProvider.validateToken(token)) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"message\": \"API 요청에 유효한 인증 토큰이 필요합니다.\"}");
                return;
            }
        }
        if (token != null && jwtTokenProvider.validateToken(token)) {
            String userId = jwtTokenProvider.getUserId(token);
            String role = jwtTokenProvider.getRole(token);
            if(role != null && role.equals("ROLE_STORE")) {
                storeRepository.findByOwnerEmail(userId).ifPresent(store -> {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(store, null, null);
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                });
            }
            else if(role != null && role.equals("ROLE_CUSTOMER")) {
                customerRepository.findByEmail(userId).ifPresent(customer -> {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(customer, null, null);
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                });
            } else {
                SecurityContextHolder.clearContext();
            }
        } else {
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }
}