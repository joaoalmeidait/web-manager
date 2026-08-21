# Análise de Segurança — Web Manager API

## Sumário Executivo

A API utiliza autenticação JWT com Spring Security, BCrypt para senhas e um filtro customizado (`OncePerRequestFilter`) para validação de tokens. A arquitetura é funcional, mas apresenta **vulnerabilidades críticas** e diversas melhorias que devem ser endereçadas antes de qualquer produção.

---

## 1. Como a Autenticação Funciona

### Fluxo Completo

```
Cliente envia requisição
        │
        ▼
┌─────────────────────────────────┐
│   JwtAuthenticationFilter      │
│   (executa ANTES do filter      │
│    padrão do Spring Security)   │
└─────────────────────────────────┘
        │
        ├── Sem header "Authorization" ou prefixo ≠ "Bearer "
        │       → Passa sem autenticação (request fica "anonymous")
        │
        └── Com "Bearer <token>" →
                │
                ▼
        ┌──────────────────────────┐
        │ JwtService.extractUsername│ ← decodifica o JWT e retorna o email
        └──────────────────────────┘
                │
                ▼
        ┌──────────────────────────┐
        │ ManagerRepository        │ ← busca o Manager no banco por email
        │   .findByEmail(email)    │
        └──────────────────────────┘
                │
                ▼
        ┌──────────────────────────┐
        │ JwtService.isTokenValid()│ ← confirma subject == email
        └──────────────────────────┘
                │
                ▼
        ┌──────────────────────────┐
        │ SecurityContextHolder    │ ← registra a autenticação
        │ .setAuthentication(...)  │
        └──────────────────────────┘
                │
                ▼
┌─────────────────────────────────┐
│   SecurityFilterChain           │
│   (regras de autorização)       │
└─────────────────────────────────┘
        │
        ├── /auth/**        → permitAll (sem login)
        ├── /swagger-ui/**  → permitAll
        ├── /v3/api-docs/** → permitAll
        └── Qualquer outro  → precisa estar autenticado (senão → 403)
```

### Fluxo de Login

```
POST /auth/login  { "email": "...", "password": "..." }
        │
        ▼
AuthenticationService.login()
        │
        ├── findByEmail() → Manager não existe? → InvalidCredentialsException (400)
        │
        ├── BCrypt.matches(senha, hash) → senha inválida? → InvalidCredentialsException (400)
        │
        └── JwtService.generateToken(email) → retorna token JWT (validade: 24h)
                │
                ▼
        Response: { "token": "eyJhbGci..." }
```

### Segurança nas Rotas

| Rota | Acesso |
|------|--------|
| `POST /auth/login` | Público (permitAll) |
| `GET /swagger-ui/**` | Público (permitAll) |
| `GET /v3/api-docs/**` | Público (permitAll) |
| `POST /employees` | Autenticado (Bearer JWT) |
| `PUT /employees/{id}` | Autenticado (Bearer JWT) |
| `GET /employees` | Autenticado (Bearer JWT) |
| `GET /employees/{id}` | Autenticado (Bearer JWT) |
| `GET /employees/email/{email}` | Autenticado (Bearer JWT) |
| `POST /managers` | Autenticado (Bearer JWT) |
| `GET /managers` | Autenticado (Bearer JWT) |

---

## 2. Vulnerabilidades Encontradas

### CRÍTICA

#### 2.1 Chave JWT hardcoded no código fonte

**Arquivo:** `src/main/java/com/webmanager/security/JwtService.java:13`

```java
private static final String SECRET_KEY = "mysupersecretkeymysupersecretkeymysupersecretkey";
```

**Risco:** Qualquer pessoa com acesso ao repositório pode forjar tokens JWT válidos. Em um ataque, o invasor poderia acessar qualquer endpoint da API como se fosse um manager autenticado.

**Correção:** Externalizar a chave para variável de ambiente ou `application.properties`:

```java
@Value("${jwt.secret}")
private String secretKey;
```

```properties
# application.properties
jwt.secret=${JWT_SECRET}
```

E adicionar `JWT_SECRET` nas variáveis de ambiente (mínimo 256 bits para HS256).

---

#### 2.2 Falta de configuração CORS

Não existe nenhuma configuração CORS no projeto. Não há `CorsConfigurationSource`, nem `@CrossOrigin`, nem propriedades CORS.

**Risco:** Se um cliente browser em origem diferente precisar acessar a API, será bloqueado. Se for API pura (sem browser), a ausência de CORS é segura, mas não há restrição explícita de origens.

---

### ALTA

#### 2.3 Versões incompatíveis das dependências JJWT

**Arquivo:** `pom.xml`

| Dependência | Versão |
|-------------|--------|
| `jjwt-api` | 0.12.5 |
| `jjwt-impl` | 0.12.6 |
| `jjwt-jackson` | 0.12.5 |

**Risco:** Incompatibilidade de versões pode causar erros de runtime difíceis de diagnosticar (compile OK, runtime Exception).

**Correção:** Padronizar todas para a mesma versão (ex: `0.12.6`):

```xml
<jjwt.version>0.12.6</jjwt.version>
```

---

#### 2.4 Sem autorização baseada em papéis (roles)

O campo `Manager.role` (enum `Role`) existe na entidade, mas **nunca é verificado**. Não há nenhuma anotação `@PreAuthorize`, `@Secured` ou `@RolesAllowed` nos controllers.

**Risco:** Um manager comum e um admin têm exatamente os mesmos privilégios. Não é possível restringir endpoints específicos (ex: deletar outro manager) a papéis específicos.

---

#### 2.5 Nulo nas authorities do SecurityContext

```java
UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
    manager, null, null  // ← authorities = null
);
```

As authorities são sempre vazias, impossibilitando qualquer verificação de papel via Spring Security.

---

#### 2.6 Sem tratamento de erros 401/403 do Spring Security

O `GlobalExceptionHandler` não captura `AccessDeniedException` nem `AuthenticationException`. Quando um request não autenticado acessa um endpoint protegido, o Spring retorna respostas 401/403 raw, fora do padrão `ErrorResponseDTO` da aplicação.

**Correção:** Adicionar handlers para essas exceções e customizar os entry points:

```java
@ExceptionHandler(AccessDeniedException.class)
public ResponseEntity<ErrorResponseDTO> handleAccessDenied(AccessDeniedException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(ErrorResponseDTO.of(403, "Acesso negado"));
}

@ExceptionHandler(AuthenticationException.class)
public ResponseEntity<ErrorResponseDTO> handleAuthentication(AuthenticationException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ErrorResponseDTO.of(401, "Não autenticado"));
}
```

---

### MÉDIA

#### 2.7 Validação incompleta do token

`JwtService.isTokenValid()` apenas confirma que o subject == email. Embora o `parseSignedClaims()` lance exceção em token expirado (tratado implicitamente), não há:

- Verificação explícita de expiração
- Verificação de `notBefore`
- Identificador único (`jti`) para suporte a revogação

---

#### 2.8 Sem mecanismo de revogação ou refresh de token

Um token emitido é válido por 24 horas sem possibilidade de invalidação antecipada. Não existe:

- Token blacklist
- Refresh token flow
- Endpoint de logout que invalida o token

**Impacto:** Se um token é roubado, o invasor tem acesso por 24h sem possibilidade de revogação.

---

#### 2.9 Falha silenciosa no filtro JWT

Se o token for inválido (expirado, adulterado), `jwtService.extractUsername()` lança `JwtException` que propaga sem tratamento, resultando em erro **500** em vez de um **401** limpo.

**Correção:** Envolva em try-catch no filtro:

```java
try {
    String email = jwtService.extractUsername(token);
    // ... resto da lógica
} catch (JwtException e) {
    // Token inválido → não autentica, deixa o request seguir sem auth
}
```

---

#### 2.10 LoginRequestDTO sem validação

O DTO de login não possui anotações de validação:

```java
public record LoginRequestDTO(String email, String password) {}
```

Comparado com outros DTOs do projeto que usam `@NotBlank`, `@Email`, etc., o login aceita campos nulos ou vazios.

---

## 3. Boas Práticas e Recomendações

### Prioridade 1 — Crítico (fazer antes de qualquer deploy)

| # | Ação | Arquivo |
|---|------|---------|
| 1 | Externalizar `SECRET_KEY` para variável de ambiente | `JwtService.java` |
| 2 | Padronizar versão JJWT em todas as dependências | `pom.xml` |
| 3 | Adicionar validação ao `LoginRequestDTO` | `LoginRequestDTO.java` |
| 4 | Tratar exceções JWT no filtro (try-catch) | `JwtAuthenticationFilter.java` |

### Prioridade 2 — Alto

| # | Ação | Arquivo |
|---|------|---------|
| 5 | Configurar CORS com origens permitidas | `SecurityConfig.java` |
| 6 | Implementar autorização baseada em roles (`@PreAuthorize`) | Controllers + `SecurityConfig.java` |
| 7 | Adicionar authorities ao `UsernamePasswordAuthenticationToken` | `JwtAuthenticationFilter.java` |
| 8 | Handler para 401/403 no `GlobalExceptionHandler` | `GlobalExceptionHandler.java` |

### Prioridade 3 — Médio

| # | Ação | Arquivo |
|---|------|---------|
| 9 | Implementar refresh token flow | `JwtService.java`, `AuthController.java` |
| 10 | Adicionar `jti` ao JWT para suporte a revogação | `JwtService.java` |
| 11 | Configurar blacklist de tokens (Redis ou tabela JPA) | Novo componente |
| 12 | Customizar `AuthenticationEntryPoint` para respostas padronizadas | `SecurityConfig.java` |

### Prioridade 4 — Baixo

| # | Ação | Arquivo |
|---|------|---------|
| 13 | Adicionar rate limiting no endpoint de login | `AuthController.java` |
| 14 | Configurar headers de segurança HTTP (HSTS, X-Content-Type, etc.) | `SecurityConfig.java` |
| 15 | Auditar e documentar todas as rotas com suas necessidades de papel | Documentação |

---

## 4. Arquivos Envolvidos

| Arquivo | Responsabilidade |
|---------|-----------------|
| `security/SecurityConfig.java` | Configuração do Spring Security, filter chain, beans |
| `security/JwtService.java` | Geração, extração e validação de tokens JWT |
| `security/JwtAuthenticationFilter.java` | Interceptor de requests que valida o token |
| `service/AuthenticationService.java` | Lógica de login (BCrypt + geração de token) |
| `controller/AuthController.java` | Endpoint público `POST /auth/login` |
| `controller/EmployeeController.java` | Endpoints protegidos de funcionários |
| `controller/ManagerController.java` | Endpoints protegidos de managers |
| `dto/login/LoginRequestDTO.java` | DTO de request de login (sem validação) |
| `dto/login/LoginResponseDTO.java` | DTO de response de login (token) |
| `exception/GlobalExceptionHandler.java` | Handler global de exceções |
| `config/OpenApiConfig.java` | Configuração Swagger com esquema de segurança |
| `pom.xml` | Dependências de segurança (Spring Security, JJWT) |

---

## 5. Resumo

A API tem uma **base sólida** de autenticação JWT com Spring Security, BCrypt para senhas e uma arquitetura bem organizada. Os principais riscos são:

1. **Chave JWT hardcoded** — vulnerabilidade que permite forjar tokens
2. **Sem autorização por roles** — todos os autenticados têm o mesmo acesso
3. **Sem CORS configurado** — restrição de origens inexistente
4. **Erros de segurança não padronizados** — respostas 401/403 não seguem o padrão da API

Endereçar os itens de **Prioridade 1** é essencial antes de qualquer ambiente de produção.
