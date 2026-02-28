package com.authflow.security.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.authflow.dto.response.AuthResponse;
import com.authflow.dto.response.UserResponse;
import com.authflow.model.RefreshToken;
import com.authflow.model.User;
import com.authflow.model.enums.AuthProvider;
import com.authflow.security.jwt.JwtService;
import com.authflow.service.RefreshTokenService;
import com.authflow.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final JwtService jwtService;
	private final UserService userService;
	private final RefreshTokenService refreshTokenService;
	private final ObjectMapper objectMapper;

	@Override
	public void onAuthenticationSuccess(
			HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication
	) throws IOException, ServletException {
		OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

		User user = userService.processOAuth2User(oAuth2User, AuthProvider.GOOGLE);

		String accessToken = jwtService.generateAccessToken(user);
		RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

		UserResponse userResponse = UserResponse.builder()
				.id(user.getId())
				.email(user.getEmail())
				.name(user.getName())
				.roles(user.getRoles())
				.emailVerified(user.getEmailVerified())
				.build();

		AuthResponse authResponse = AuthResponse.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken.getToken())
				.tokenType("Bearer")
				.expiresIn(jwtService.getAccessTokenExpiration() / 1000)
				.user(userResponse)
				.build();

		String jsonResponse = objectMapper.writeValueAsString(authResponse);

		// Return an HTML page that works for both web (postMessage) and native (WebView)
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");

		String htmlStart = """
			<!DOCTYPE html>
			<html>
			<head>
				<meta charset="UTF-8">
				<title>Login Successful</title>
				<style>
					body {
						font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
						display: flex;
						justify-content: center;
						align-items: center;
						height: 100vh;
						margin: 0;
						background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
					}
					.message {
						background: white;
						padding: 2rem;
						border-radius: 8px;
						box-shadow: 0 4px 6px rgba(0,0,0,0.1);
						text-align: center;
					}
					.success {
						color: #10b981;
						font-size: 48px;
						margin-bottom: 1rem;
					}
					h1 {
						color: #111827;
						margin: 0 0 0.5rem 0;
					}
					p {
						color: #6b7280;
						margin: 0;
					}
				</style>
			</head>
			<body>
				<div class="message">
					<div class="success">✓</div>
					<h1>Login Successful!</h1>
					<p>Redirecting...</p>
				</div>
				<script>
					// Auth data from backend
					const authData = """;

		String htmlEnd = """
			;

					// For web: Send message to parent window (popup opener)
					if (window.opener) {
						window.opener.postMessage({ type: 'GOOGLE_LOGIN_SUCCESS', data: authData }, '*');
						// Close popup after a short delay
						setTimeout(() => window.close(), 500);
					}

					// For native WebView: Message will be detected by injected JavaScript
					// The JSON is also in the page body for fallback detection
					if (window.ReactNativeWebView) {
						window.ReactNativeWebView.postMessage(JSON.stringify(authData));
					}
				</script>
			</body>
			</html>
			""";

		String html = htmlStart + jsonResponse + htmlEnd;
		response.getWriter().write(html);
	}
}
