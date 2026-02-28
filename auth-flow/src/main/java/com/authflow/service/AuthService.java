package com.authflow.service;

import com.authflow.dto.request.*;
import com.authflow.dto.response.AuthResponse;
import com.authflow.dto.response.MessageResponse;
import com.authflow.dto.response.UserResponse;
import com.authflow.exception.EmailNotVerifiedException;
import com.authflow.exception.InvalidCredentialsException;
import com.authflow.exception.TokenNotFoundException;
import com.authflow.exception.UserAlreadyExistsException;
import com.authflow.model.*;
import com.authflow.model.enums.AuthProvider;
import com.authflow.model.enums.Role;
import com.authflow.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserService userService;
	private final JwtService jwtService;
	private final RefreshTokenService refreshTokenService;
	private final PasswordResetTokenService passwordResetTokenService;
	private final EmailVerificationTokenService emailVerificationTokenService;
	private final EmailService emailService;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;

	@Transactional
	public AuthResponse refreshToken(RefreshTokenRequest request) {
		RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken());
		refreshTokenService.verifyExpiration(refreshToken);

		User user = refreshToken.getUser();

		String accessToken = jwtService.generateAccessToken(user);

		refreshTokenService.deleteByToken(request.getRefreshToken());
		RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

		UserResponse userResponse = UserResponse.builder()
				.id(user.getId())
				.email(user.getEmail())
				.name(user.getName())
				.roles(user.getRoles())
				.emailVerified(user.getEmailVerified())
				.build();

		return AuthResponse.builder()
				.accessToken(accessToken)
				.refreshToken(newRefreshToken.getToken())
				.tokenType("Bearer")
				.expiresIn(jwtService.getAccessTokenExpiration() / 1000)
				.user(userResponse)
				.build();
	}

	public UserResponse getCurrentUser(String email) {
		User user = userService.findByEmail(email);

		return UserResponse.builder()
				.id(user.getId())
				.email(user.getEmail())
				.name(user.getName())
				.roles(user.getRoles())
				.emailVerified(user.getEmailVerified())
				.build();
	}

	@Transactional
	public MessageResponse register(RegisterRequest request) {
		// Verificar se email já existe
		if (userService.existsByEmail(request.getEmail())) {
			throw new UserAlreadyExistsException("E-mail já cadastrado");
		}

		// Criar usuário
		User user = User.builder()
				.email(request.getEmail())
				.password(passwordEncoder.encode(request.getPassword()))
				.name(request.getName())
				.provider(AuthProvider.LOCAL)
				.roles(Set.of(Role.USER))
				.emailVerified(false)
				.enabled(true)
				.build();

		user = userService.createUser(user);

		// Criar token de verificação e enviar email
		EmailVerificationToken verificationToken =
				emailVerificationTokenService.createEmailVerificationToken(user);
		emailService.sendEmailVerificationEmail(
				user.getEmail(),
				user.getName(),
				verificationToken.getToken()
		);

		return new MessageResponse("Cadastro realizado com sucesso. Por favor, verifique seu e-mail para ativar sua conta.");
	}

	@Transactional
	public AuthResponse login(LoginRequest request) {
		// Autenticar credenciais
		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(
							request.getEmail(),
							request.getPassword()
					)
			);
		} catch (BadCredentialsException e) {
			throw new InvalidCredentialsException("E-mail ou senha inválidos");
		}

		// Buscar usuário
		User user = userService.findByEmail(request.getEmail());

		// Verificar se email foi verificado (apenas para LOCAL)
		if (user.getProvider() == AuthProvider.LOCAL && !user.getEmailVerified()) {
			throw new EmailNotVerifiedException(
					"Por favor, verifique seu e-mail antes de fazer login."
			);
		}

		// Verificar se conta está habilitada
		if (!user.getEnabled()) {
			throw new InvalidCredentialsException("Conta desabilitada");
		}

		// Gerar tokens
		String accessToken = jwtService.generateAccessToken(user);
		RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

		// Construir resposta
		UserResponse userResponse = UserResponse.builder()
				.id(user.getId())
				.email(user.getEmail())
				.name(user.getName())
				.roles(user.getRoles())
				.emailVerified(user.getEmailVerified())
				.build();

		return AuthResponse.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken.getToken())
				.tokenType("Bearer")
				.expiresIn(jwtService.getAccessTokenExpiration() / 1000)
				.user(userResponse)
				.build();
	}

	@Transactional
	public MessageResponse verifyEmail(VerifyEmailRequest request) {
		// Buscar token
		EmailVerificationToken verificationToken =
				emailVerificationTokenService.findByToken(request.getToken());

		// Verificar validade
		emailVerificationTokenService.verifyToken(verificationToken);

		// Atualizar usuário
		User user = verificationToken.getUser();
		user.setEmailVerified(true);
		userService.updateUser(user);

		// Marcar token como usado
		emailVerificationTokenService.markTokenAsUsed(verificationToken);

		// Enviar email de boas-vindas
		emailService.sendWelcomeEmail(user.getEmail(), user.getName());

		return new MessageResponse("E-mail verificado com sucesso. Você já pode fazer login.");
	}

	@Transactional
	public MessageResponse forgotPassword(ForgotPasswordRequest request) {
		// Buscar usuário
		User user = userService.findByEmailOptional(request.getEmail()).orElse(null);

		if (user != null) {
			// Criar token e enviar email
			PasswordResetToken resetToken =
					passwordResetTokenService.createPasswordResetToken(user);
			emailService.sendPasswordResetEmail(
					user.getEmail(),
					user.getName(),
					resetToken.getToken()
			);
		}

		// Retornar mensagem genérica (segurança)
		return new MessageResponse("Se este e-mail estiver cadastrado, um link de redefinição de senha foi enviado.");
	}

	@Transactional
	public MessageResponse resetPassword(ResetPasswordRequest request) {
		// Buscar token
		PasswordResetToken resetToken =
				passwordResetTokenService.findByToken(request.getToken());

		// Verificar validade
		passwordResetTokenService.verifyToken(resetToken);

		User user = resetToken.getUser();

		// Atualizar senha
		user.setPassword(passwordEncoder.encode(request.getNewPassword()));
		userService.updateUser(user);

		// Marcar token como usado
		passwordResetTokenService.markTokenAsUsed(resetToken);

		// Invalidar todos os refresh tokens (logout forçado)
		refreshTokenService.deleteByUser(user);

		// Enviar email de confirmação
		emailService.sendPasswordResetConfirmationEmail(user.getEmail(), user.getName());

		return new MessageResponse("Senha redefinida com sucesso. Você já pode fazer login com sua nova senha.");
	}

	@Transactional
	public MessageResponse resendVerificationEmail(ResendVerificationRequest request) {
		// Buscar usuário
		User user = userService.findByEmailOptional(request.getEmail()).orElse(null);

		if (user != null) {
			// Verificar se o email já foi verificado
			if (user.getEmailVerified()) {
				return new MessageResponse("E-mail já foi verificado. Você já pode fazer login.");
			}

			// Invalidar tokens antigos e criar novo
			EmailVerificationToken verificationToken =
					emailVerificationTokenService.createEmailVerificationToken(user);

			// Enviar email
			emailService.sendEmailVerificationEmail(
					user.getEmail(),
					user.getName(),
					verificationToken.getToken()
			);
		}

		// Retornar mensagem genérica (segurança - não revelar se email existe)
		return new MessageResponse("Se este e-mail estiver cadastrado e não verificado, um novo link de verificação foi enviado.");
	}

	@Transactional
	public MessageResponse logout(LogoutRequest request) {
		try {
			refreshTokenService.revokeToken(request.getRefreshToken());
			return new MessageResponse("Logged out successfully");
		} catch (TokenNotFoundException e) {
			// Se o token não existe, considera logout bem-sucedido
			return new MessageResponse("Logged out successfully");
		}
	}
}
