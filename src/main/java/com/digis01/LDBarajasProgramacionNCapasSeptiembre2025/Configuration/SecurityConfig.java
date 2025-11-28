package com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.Configuration;

import com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.Service.UserDetailsJPAService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsJPAService userDetailsJPAService;

    public SecurityConfig(UserDetailsJPAService userDetailsJPAService1) {
        this.userDetailsJPAService = userDetailsJPAService1;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(configurer -> configurer
                .requestMatchers("/login", "/usuario/login").permitAll()
                .requestMatchers("/css/**", "/js/**", "/img/**").permitAll()
                .requestMatchers("/usuario/**")
                .hasAnyRole("Asistente", "Administrador", "Gerente", "Subgerente", "Colaborador")
                .anyRequest().authenticated()
                )
                
                .formLogin(form -> form
                .loginPage("/usuario/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/usuario", true)
                .permitAll()
                )
                
                .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/usuario/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
                )
                
                .userDetailsService(userDetailsJPAService);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
