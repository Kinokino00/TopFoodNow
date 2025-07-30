package com.example.topfoodnow.filter;

import com.example.topfoodnow.service.JwtBlacklistService;
import com.example.topfoodnow.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final JwtBlacklistService jwtBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;
        String jti = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
                jti = jwtUtil.extractJti(jwt); // 提取 JTI
            } catch (ExpiredJwtException e) {
                // Token 過期處理
                logger.warn("JWT token 已過期: {}", e.getMessage(), e);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 設置 401 狀態碼
                response.getWriter().write("JWT token 已過期。");
                return;
            } catch (SignatureException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
                logger.warn("JWT token 解析失敗或無效: {}", e.getMessage(), e);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 設置 401 狀態碼
                response.getWriter().write("JWT token 無效。");
                return;
            } catch (Exception e) {
                logger.warn("JWT token 解析時發生未知錯誤: {}", e.getMessage(), e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 設置 500 狀態碼
                response.getWriter().write("伺服器內部錯誤，請重試。");
                return;
            }
        }

        // 如果提取到用戶名，且當前 SecurityContext 中沒有認證信息
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 驗證 token 有效性
            if (jwtUtil.validateToken(jwt, userDetails)) {
                // 檢查 Token 是否在黑名單中
                if (jti != null && jwtBlacklistService.isTokenBlacklisted(jti)) {
                    logger.warn("嘗試使用已吊銷的 Token，JTI: {}", jti);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 設置 401 狀態碼
                    response.getWriter().write("此 Token 已被吊銷。");
                    return;
                }

                // 創建認證對象
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 將認證信息設置到 SecurityContext 中
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        chain.doFilter(request, response);
    }
}