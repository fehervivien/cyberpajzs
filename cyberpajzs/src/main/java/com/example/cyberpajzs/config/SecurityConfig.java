package com.example.cyberpajzs.config;

import com.example.cyberpajzs.service.CartService;
import com.example.cyberpajzs.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            DaoAuthenticationProvider daoAuthenticationProvider,
            AuthenticationSuccessHandler loginSuccessHandler
    ) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // Engedélyezz minden statikus erőforrást
                        .requestMatchers(
                                "/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico",
                                "/**"
                        ).permitAll()
                        // Útvonalak, amelyekhez nem kell bejelentkezés
                        .requestMatchers(
                                "/",
                                "/product-list",
                                "/product-details/**",
                                "/cart/**",
                                "/login",
                                "/register",
                                "/checkout",
                                "/checkout/place-order",
                                "/order-confirmation",
                                "/request-quote/**",
                                "/newsletter/subscribe",
                                "/about",
                                "/news",
                                "/news/**"
                        ).permitAll()
                        // Útvonalak, amelyekhez bejelentkezés kell
                        .requestMatchers(
                                "/profile"
                        ).authenticated()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // Minden más útvonalhoz továbbra is kell bejelentkezés
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/perform_login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error")
                        .successHandler(loginSuccessHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                );

        http.authenticationProvider(daoAuthenticationProvider);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(UserService userService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // A CartService-t most paraméterként injektáljuk, nem a SecurityConfig konstruktorába
    @Bean
    public AuthenticationSuccessHandler loginSuccessHandler(UserService userService, CartService cartService) {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                String username = authentication.getName();
                userService.findUserByUsername(username).ifPresent(user -> {
                    // Itt hívjuk meg a CartService-t, hogy egyesítse a kosarakat
                    cartService.mergeSessionCartToUserCart(user, request.getSession());
                });
                // Átirányítás a főoldalra vagy az eredetileg kért URL-re
                response.sendRedirect("/");
            }
        };
    }
}
