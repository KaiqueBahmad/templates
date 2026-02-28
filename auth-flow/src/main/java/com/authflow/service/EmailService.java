package com.authflow.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

	private final JavaMailSender mailSender;
	private final TemplateEngine templateEngine;

	@Value("${app.email.from}")
	private String emailFrom;

	@Value("${app.email.from-name}")
	private String emailFromName;

	@Value("${app.frontend.url}")
	private String frontendUrl;

	@Async
	public void sendWelcomeEmail(String to, String name) {
		try {
			Context context = new Context();
			context.setVariable("name", name);

			String htmlContent = templateEngine.process("email/welcome", context);

			sendHtmlEmail(to, "Bem-vindo ao AuthFlow!", htmlContent);
			log.info("Welcome email sent to: {}", to);
		} catch (Exception e) {
			log.error("Failed to send welcome email to: {}", to, e);
		}
	}

	@Async
	public void sendPasswordResetEmail(String to, String name, String resetToken) {
		try {
			String resetLink = frontendUrl + "/redefinir-senha?token=" + resetToken;

			Context context = new Context();
			context.setVariable("name", name);
			context.setVariable("resetLink", resetLink);
			context.setVariable("expirationTime", "1 hora");

			String htmlContent = templateEngine.process("email/password-reset", context);

			sendHtmlEmail(to, "Solicitação de Redefinição de Senha", htmlContent);
			log.info("Password reset email sent to: {}", to);
		} catch (Exception e) {
			log.error("Failed to send password reset email to: {}", to, e);
		}
	}

	@Async
	public void sendPasswordResetConfirmationEmail(String to, String name) {
		try {
			Context context = new Context();
			context.setVariable("name", name);

			String htmlContent = templateEngine.process("email/password-reset-confirmation", context);

			sendHtmlEmail(to, "Senha Alterada com Sucesso", htmlContent);
			log.info("Password reset confirmation email sent to: {}", to);
		} catch (Exception e) {
			log.error("Failed to send password reset confirmation email to: {}", to, e);
		}
	}

	@Async
	public void sendEmailVerificationEmail(String to, String name, String verificationToken) {
		try {
			String verificationLink = frontendUrl + "/verificar-email?token=" + verificationToken;

			Context context = new Context();
			context.setVariable("name", name);
			context.setVariable("verificationLink", verificationLink);
			context.setVariable("expirationTime", "24 horas");

			String htmlContent = templateEngine.process("email/email-verification", context);

			sendHtmlEmail(to, "Verifique seu Endereço de E-mail", htmlContent);
			log.info("Email verification email sent to: {}", to);
		} catch (Exception e) {
			log.error("Failed to send email verification email to: {}", to, e);
		}
	}

	private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException, UnsupportedEncodingException {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

		helper.setFrom(emailFrom, emailFromName);
		helper.setTo(to);
		helper.setSubject(subject);
		helper.setText(htmlContent, true);

		mailSender.send(message);
	}
}
