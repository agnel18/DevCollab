    package com.agnel.devcollab.config;

    import com.agnel.devcollab.service.UserDetailsServiceImpl;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
    import org.springframework.security.config.annotation.web.builders.HttpSecurity;
    import org.springframework.security.core.userdetails.UserDetailsService;
    import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.security.web.SecurityFilterChain;
    import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

    @Configuration
    @EnableMethodSecurity
    public class SecurityConfig {

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http, DaoAuthenticationProvider authProvider) throws Exception {
            http
                .authenticationProvider(authProvider)
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/", "/register", "/login", "/h2-console/**", "/css/**", "/js/**").permitAll()
                    .requestMatchers("/projects", "/projects/**", "/subtasks/**").permitAll()
                    .requestMatchers("/ws/**").permitAll()
                    .anyRequest().authenticated()
                )
                .formLogin(form -> form
                    .loginPage("/login")
                    .defaultSuccessUrl("/projects", true)
                    .permitAll()
                )
                .logout(logout -> logout
                    .logoutSuccessUrl("/")
                    .permitAll()
                )
                .csrf(csrf -> csrf
                    .ignoringRequestMatchers("/h2-console/**", "/projects/**", "/subtasks/**")
                )
                .headers(headers -> headers
                    .frameOptions(frame -> frame.sameOrigin())
                );

            return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        @Bean
        public UserDetailsService userDetailsService() {
            return new UserDetailsServiceImpl();
        }

        // MODERN WAY: Constructor injection
        @Bean
        public DaoAuthenticationProvider authenticationProvider(
                UserDetailsService userDetailsService,
                PasswordEncoder passwordEncoder) {
            DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder);
    return provider;
        }
    }