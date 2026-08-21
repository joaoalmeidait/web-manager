package com.webmanager.security;

import com.webmanager.BaseTest;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class JwtServiceTest extends BaseTest {

    @Autowired
    private JwtService jwtService;

    @Test
    void shouldGenerateValidToken() {
        String token = jwtService.generateToken("test@email.com");

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("test@email.com");
    }

    @Test
    void shouldValidateTokenWithCorrectEmail() {
        String token = jwtService.generateToken("test@email.com");

        assertThat(jwtService.isTokenValid(token, "test@email.com")).isTrue();
    }

    @Test
    void shouldNotValidateTokenWithWrongEmail() {
        String token = jwtService.generateToken("test@email.com");

        assertThat(jwtService.isTokenValid(token, "other@email.com")).isFalse();
    }

    @Test
    void shouldRejectExpiredToken() {
        String expiredToken = jwtService.generateExpiredToken("test@email.com");

        assertThatThrownBy(() -> jwtService.extractUsername(expiredToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void shouldRejectTamperedToken() {
        String token = jwtService.generateToken("test@email.com");
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";

        assertThatThrownBy(() -> jwtService.extractUsername(tampered))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void shouldRejectRandomString() {
        assertThatThrownBy(() -> jwtService.extractUsername("not-a-jwt-token"))
                .isInstanceOf(JwtException.class);
    }
}
