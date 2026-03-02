package com.example.topfoodnow.service.jwtblacklist;

import com.example.topfoodnow.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtBlacklistService {
  private final JwtUtil jwtUtil;
  private final RedisTemplate<String, String> redisTemplate;

  private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

  /**
   * 將 JWT Token 加入黑名單
   * 將 Token 的 JTI 作為鍵，並設置其過期時間與原始 JWT 相同
   * @param token 要加入黑名單的 JWT 字串
   */
  @Override
  public void blacklistToken(String token) {
    try {
      String jti = jwtUtil.extractJti(token);
      Date expiration = jwtUtil.extractExpiration(token);
      Date now = new Date();

      if (jti != null && expiration != null && expiration.after(now)) {
        // 計算 Token 距離過期還有多少時間，以便 Redis 設置相同的 TTL
        long remainingTime = expiration.getTime() - now.getTime();
        if (remainingTime > 0) {
          // 將 Token 的 JTI 寫入 Redis
          redisTemplate.opsForValue().set(BLACKLIST_PREFIX + jti, "blacklisted", remainingTime, TimeUnit.MILLISECONDS);
          log.info("Token with JTI: {} blacklisted until {}", jti, expiration);
        }
      }
    } catch (Exception e) {
      log.error("Failed to blacklist token: {}", e.getMessage(), e);
    }
  }

  /**
   * 檢查給定的 JTI 是否在黑名單中
   * @param jti JWT ID
   * @return 如果在黑名單中則返回 true，否則返回 false
   */
  @Override
  public boolean isTokenBlacklisted(String jti) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + jti));
  }
}
