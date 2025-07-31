package com.example.topfoodnow.service;

public interface JwtBlacklistService {
    void blacklistToken(String token);
    boolean isTokenBlacklisted(String jti);
}