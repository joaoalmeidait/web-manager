package com.webmanager.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    private static final String SECRET_KEY = "mysupersecretkeymysupersecretkeymysupersecretkey";
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());


    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .signWith(key).expiration(new Date(System.currentTimeMillis() + 86400000))
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean isTokenValid(String token, String email) {
        final String username = extractUsername(token);
        return username.equals(email);
    }
}
