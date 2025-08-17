package myapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")) // Disable CSRF for H2 console if still present
                .ignoringRequestMatchers(AntPathRequestMatcher.antMatcher("/api/register")) // Disable CSRF for registration endpoint
            )
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll() // Allow access to H2 console if still present
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/register")).permitAll() // Allow access to registration endpoint
                .anyRequest().authenticated() // All other requests require authentication
            )
            .headers(headers -> headers.frameOptions().sameOrigin()); // For H2 console frames if still present
        return http.build();
    }
}
