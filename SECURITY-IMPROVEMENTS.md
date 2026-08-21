# Alterações de Segurança — Web Manager API

## Resumo

Todas as vulnerabilidades críticas e altas identificadas na análise (`SECURITY-ANALYSIS.md`) foram corrigidas. Os testes passam 53/53.

---

## Arquivos Alterados

| Arquivo | Alteração |
|---------|-----------|
| `pom.xml` | Versão JJWT padronizada para `0.12.6` via propriedade `${jjwt.version}` |
| `application.properties` | Adicionado `jwt.secret=${JWT_SECRET}` |
| `application-test.properties` | Adicionado `jwt.secret` para ambiente de teste |
| `security/JwtService.java` | Secret key via `@Value("${jwt.secret}")` em vez de hardcoded; adicionado `generateExpiredToken()` |
| `security/JwtAuthenticationFilter.java` | Try-catch em `JwtException`; authorities com `ROLE_` + enum Role |
| `security/SecurityConfig.java` | CORS configurado; headers de segurança HTTP; `@EnableMethodSecurity`; `AuthenticationEntryPoint` (401) e `AccessDeniedHandler` (403) customizados; `SessionCreationPolicy.STATELESS` |
| `dto/login/LoginRequestDTO.java` | Validações `@NotBlank`, `@Email` |
| `controller/AuthController.java` | Adicionado `@Valid` no `@RequestBody` |
| `exception/GlobalExceptionHandler.java` | Handlers para `AccessDeniedException` (403) e `AuthenticationException` (401) |
| `WebManagerApplicationTests.java` | Adicionado `@ActiveProfiles("test")` para rodar sem PostgreSQL |

---

## Arquivos Criados (Testes)

| Arquivo | Descrição | Testes |
|---------|-----------|--------|
| `security/SecurityIntegrationTest.java` | Integração: 401 sem token, 401 token inválido, 403 acesso negado, admin cria manager, token expirado, validação login, headers HTTP | 10 |
| `security/JwtAuthenticationFilterTest.java` | Filtro: rejeição sem header, prefixo inválido, token válido, token expirado, usuário inexistente, token adulterado, roles ADMIN e MANAGER | 9 |
| `security/JwtServiceTest.java` | Unit: geração de token, validação, email incorreto, token expirado, token adulterado, string aleatória | 6 |
| `service/AuthenticationServiceTest.java` | Unit: login sucesso, email não encontrado, senha incorreta | 3 |
| `dto/login/LoginRequestDTOTest.java` | Validação: dados válidos, email em branco, email nulo, email inválido, senha em branco, senha nula | 6 |

**Total: 34 novos testes de segurança**

---

## O que foi implementado

### 1. JWT Secret externalizado (CRÍTICO)

```java
// Antes
private static final String SECRET_KEY = "mysupersecretkeymysupersecretkeymysupersecretkey";

// Depois
public JwtService(@Value("${jwt.secret}") String secret) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes());
}
```

```properties
# application.properties
jwt.secret=${JWT_SECRET}

# application-test.properties
jwt.secret=dGVzdHNlY3JldGtleWZvcnRlc3RpbmdwdXJwb3Nlcw==
```

Para gerar uma chave segura: `openssl rand -base64 32`

### 2. Versão JJWT padronizada

```xml
<properties>
    <jjwt.version>0.12.6</jjwt.version>
</properties>

<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>${jjwt.version}</version>
</dependency>
<!-- jjwt-impl e jjwt-jackson também usam ${jjwt.version} -->
```

### 3. Validação no LoginRequestDTO

```java
public record LoginRequestDTO(
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    String email,

    @NotBlank(message = "Senha é obrigatória")
    String password
) {}
```

### 4. Try-catch no filtro JWT

```java
try {
    String email = jwtService.extractUsername(token);
    // ... lógica de autenticação
} catch (JwtException | IllegalArgumentException e) {
    // Token inválido → request segue sem autenticação → 401
}
```

### 5. Authorities com roles

```java
List<SimpleGrantedAuthority> authorities = List.of(
    new SimpleGrantedAuthority("ROLE_" + manager.getRole().name())
);

UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
    manager, null, authorities
);
```

### 6. CORS configurado

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:3000"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    config.setAllowCredentials(true);
    // ...
}
```

### 7. Autorização por roles

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
    .requestMatchers(HttpMethod.POST, "/managers").hasRole("ADMIN")
    .anyRequest().authenticated()
)
```

`POST /managers` requer `ROLE_ADMIN`. Outros endpoints autenticados funcionam para qualquer role.

### 8. Headers de segurança HTTP

```java
.headers(headers -> headers
    .contentTypeOptions(contentType -> {})       // X-Content-Type-Options: nosniff
    .frameOptions(frame -> frame.deny())         // X-Frame-Options: DENY
    .xssProtection(xss -> {})                    // X-XSS-Protection
    .httpStrictTransportSecurity(hsts -> hsts    // Strict-Transport-Security
        .includeSubDomains(true)
        .maxAgeInSeconds(31536000)
    )
)
```

### 9. Respostas 401/403 padronizadas

```java
.exceptionHandling(ex -> ex
    .authenticationEntryPoint((request, response, authException) -> {
        response.setStatus(401);
        response.setContentType("application/json");
        response.getWriter().write("{\"status\":401,\"message\":\"Não autenticado\"}");
    })
    .accessDeniedHandler((request, response, accessDeniedException) -> {
        response.setStatus(403);
        response.setContentType("application/json");
        response.getWriter().write("{\"status\":403,\"message\":\"Acesso negado\"}");
    })
)
```

### 10. Sessão stateless

```java
.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
```

Garante que o Spring Security não cria sessões HTTP.

---

## O que NÃO foi implementado (pendente)

| Item | Motivo |
|------|--------|
| Rate limiting no login | Requer dependência externa (ex: `bucket4j-spring-boot-starter`) |
| Refresh token flow | Requer nova tabela no banco + endpoints novos |
| Token blacklist/revogação | Requer infraestrutura adicional (Redis ou tabela JPA) |

Esses itens são recomendados para produção mas são de maior complexidade e podem ser implementados em uma iteração futura.

---

## Testes

```bash
./mvnw test
```

Resultado: **53 testes, 0 falhas**

| Suite | Testes |
|-------|--------|
| SecurityIntegrationTest | 10 |
| JwtAuthenticationFilterTest | 9 |
| JwtServiceTest | 6 |
| LoginRequestDTOTest | 6 |
| AuthenticationServiceTest | 3 |
| EmployeeControllerIntegrationTest | 5 |
| EmployeeServiceRoleTest | 4 |
| EmployeeServiceTest | 2 |
| WebManagerApplicationTests | 1 |
| Repository tests | 7 |
| **Total** | **53** |
