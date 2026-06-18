package com.autowash.config;

<<<<<<< HEAD
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
=======
import com.autowash.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;

import java.util.Collections;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    // Gọi trực tiếp JwtTokenProvider đã có sẵn của bạn để giải mã token
    private final JwtTokenProvider tokenProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Tắt CSRF chống lỗi 403 bậy bạ trên Swagger
                .csrf(csrf -> csrf.disable())

                // 2. Phân quyền API công khai và bảo mật
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/packages/**", "/slots/**", "/hello", "/auth/**").permitAll()
                        .anyRequest().authenticated()
                )

                // 3. Trả về mã lỗi 401 tiếng Việt trực quan khi chưa login
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint())
                );

        // 4. BỘ LỌC TỰ ĐỘNG (Viết trực tiếp không cần file mới)
        http.addFilterBefore((request, response, chain) -> {
            try {
                jakarta.servlet.http.HttpServletRequest httpRequest = (jakarta.servlet.http.HttpServletRequest) request;
                String bearerToken = httpRequest.getHeader("Authorization");
                String jwt = null;

                if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
                    jwt = bearerToken.substring(7);
                }

                // Nếu có Token gửi lên từ Swagger và Token đó hợp lệ
                if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                    String email = tokenProvider.getEmailFromToken(jwt);

                    UserDetails userDetails = new User(email, "", Collections.emptyList());
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );

                    // Xác nhận với Spring người dùng này đã đăng nhập thành công
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                // Bỏ qua nếu có lỗi giải mã
            }
            chain.doFilter(request, response);
        }, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(401);
            response.setCharacterEncoding("UTF-8");

            String jsonResponse = "{\"error\": \"Bạn cần đăng nhập để thực hiện chức năng này!\"}";
            response.getWriter().write(jsonResponse);
        };
    }

>>>>>>> main
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> main
