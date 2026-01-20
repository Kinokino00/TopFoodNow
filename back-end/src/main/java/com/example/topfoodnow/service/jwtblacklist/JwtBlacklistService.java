package com.example.topfoodnow.service.jwtblacklist;

public interface JwtBlacklistService {
    void blacklistToken(String token);
    boolean isTokenBlacklisted(String jti);
}