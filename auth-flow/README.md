# Auth Flow

Core de autenticação extraído do projeto InstrutorBrasil, contendo apenas as funcionalidades essenciais de login e registro sem KYC.

## Funcionalidades

- Registro de usuários com email e senha
- Verificação de email
- Login com email e senha
- Recuperação de senha
- Refresh tokens
- Rate limiting
- JWT authentication

## Tecnologias

- Java 17
- Spring Boot 3.5.9
- PostgreSQL
- JWT (jjwt 0.12.6)
- Spring Security
- Thymeleaf (templates de email)
- Swagger/OpenAPI

## Configuração

### 1. Configurar banco de dados

Inicie o PostgreSQL usando Docker Compose:

```bash
docker-compose up -d
```

### 2. Configurar aplicação

Copie o arquivo de exemplo e configure com seus valores:

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Edite o arquivo `application.properties` e configure:

- Credenciais do banco de dados (se diferentes do padrão)
- Configurações de email (SMTP)
- Chave secreta JWT (gere uma chave base64 de pelo menos 256 bits)
- URL do frontend

### 3. Executar aplicação

```bash
./mvnw spring-boot:run
```

A aplicação estará disponível em `http://localhost:8080`

## API Documentation

Acesse a documentação Swagger em: `http://localhost:8080/swagger-ui.html`

## Endpoints Principais

### Autenticação
- `POST /auth/register` - Registrar novo usuário
- `POST /auth/login` - Login com email/senha
- `POST /auth/verify-email` - Verificar email
- `POST /auth/resend-verification` - Reenviar email de verificação
- `POST /auth/recover/request` - Solicitar recuperação de senha
- `POST /auth/recover/confirm` - Confirmar recuperação de senha
- `POST /auth/refresh-token` - Renovar access token
- `POST /auth/logout` - Fazer logout
- `GET /auth/me` - Obter dados do usuário autenticado

### Usuário
- `PUT /user/profile` - Atualizar perfil do usuário

## Segurança

- Senhas são criptografadas com BCrypt
- Tokens JWT com expiração configurável
- Rate limiting para prevenir ataques
- CORS configurado

## Rate Limiting

Por padrão, o rate limiting usa armazenamento em memória. Para ambientes com múltiplas instâncias, configure Redis no `application.properties`:

```properties
app.rate-limit.storage-type=REDIS
spring.redis.host=localhost
spring.redis.port=6379
```

## Email Mock

Para desenvolvimento, você pode ativar o modo mock de email que apenas loga os emails no console:

```properties
spring.mail.mock.enabled=true
```

## Estrutura do Projeto

```
auth-flow/
├── src/main/java/com/authflow/
│   ├── annotation/          # Anotações customizadas (Rate Limit)
│   ├── config/              # Configurações (Security, OpenAPI, etc)
│   ├── controller/          # Controllers REST
│   ├── dto/                 # DTOs de request e response
│   ├── exception/           # Exceções customizadas e handlers
│   ├── filter/              # Filtros HTTP
│   ├── interceptor/         # Interceptors
│   ├── model/               # Entidades JPA
│   ├── repository/          # Repositories JPA
│   ├── security/            # Configurações de segurança (JWT)
│   └── service/             # Lógica de negócio
└── src/main/resources/
    ├── templates/           # Templates de email (Thymeleaf)
    └── application.properties.example
```

## Licença

Este projeto foi extraído do InstrutorBrasil para servir como template de autenticação.
