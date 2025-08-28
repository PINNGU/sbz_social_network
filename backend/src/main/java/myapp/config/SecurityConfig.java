package myapp.config;

import myapp.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final UserService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(UserService userDetailsService, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }



    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);

        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("[SecurityConfig] Building filter chain for /api/posts and roles...");
    http
        .csrf(csrf -> csrf
            .ignoringRequestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**"))
            .ignoringRequestMatchers(AntPathRequestMatcher.antMatcher("/api/auth/**"))
            .ignoringRequestMatchers(AntPathRequestMatcher.antMatcher("/api/posts/**"))
            .ignoringRequestMatchers(AntPathRequestMatcher.antMatcher("/api/friendRequest/**"))
            .ignoringRequestMatchers(AntPathRequestMatcher.antMatcher("/api/users/**"))
            .ignoringRequestMatchers(AntPathRequestMatcher.antMatcher("/api/friends/**"))
            .ignoringRequestMatchers(AntPathRequestMatcher.antMatcher("/api/users/**"))
            .ignoringRequestMatchers(AntPathRequestMatcher.antMatcher("/api/block/**"))
            .ignoringRequestMatchers(AntPathRequestMatcher.antMatcher("/api/places/**"))
            .ignoringRequestMatchers(AntPathRequestMatcher.antMatcher("/api/placeRating/**"))
        )
                // No JWT or custom entry point needed
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
            .requestMatchers(AntPathRequestMatcher.antMatcher("/api/auth/register")).permitAll()
            .requestMatchers(AntPathRequestMatcher.antMatcher("/api/auth/login")).permitAll()
            .anyRequest().permitAll()
        );
        
                // .headers(headers -> headers.frameOptions().sameOrigin());

        http.authenticationProvider(authenticationProvider());


        return http.build();
    }
}
