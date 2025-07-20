package com.svastik.workoutextract;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final UserRepository userRepository;

    public OAuth2LoginSuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oAuth2User) {
            Map<String, Object> attributes = oAuth2User.getAttributes();
            String googleUserId = (String) attributes.get("sub");
            String email = (String) attributes.get("email");
            String name = (String) attributes.get("name");
            if (googleUserId != null && email != null) {
                Optional<User> userOpt = userRepository.findByGoogleUserId(googleUserId);
                if (userOpt.isEmpty()) {
                    User user = User.builder()
                            .googleUserId(googleUserId)
                            .email(email)
                            .name(name)
                            .build();
                    userRepository.save(user);
                }
            }
        }
        super.onAuthenticationSuccess(request, response, authentication);
    }
} 