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

import java.io.IOException;
import java.util.Arrays;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomerRepository customerRepository;
    private final StoreRepository storeRepository;


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
        String token = extractToken(request);
        log.info("JWT token토큰토큰아: {}", token);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            String userId = jwtTokenProvider.getUserId(token);
            String role = jwtTokenProvider.getRole(token);

            if(role.equals("ROLE_STORE")) {
                storeRepository.findByOwnerEmail(userId).ifPresent(store -> {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(store, null, null);
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                });
            }
            else if(role.equals("ROLE_CUSTOMER")) {
                customerRepository.findByEmail(userId).ifPresent(customer -> {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(customer, null, null);
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                });
            }
        }
        filterChain.doFilter(request, response);
    }
}