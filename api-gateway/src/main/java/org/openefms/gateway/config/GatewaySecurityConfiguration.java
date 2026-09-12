package org.openefms.gateway.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableConfigurationProperties(GatewaySecurityProperties.class)
class GatewaySecurityConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "oefms.gateway.security", name = "enabled", havingValue = "true")
    SecurityFilterChain jwtGatewaySecurityFilterChain(
            HttpSecurity http,
            GatewaySecurityProperties properties
    ) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);
        http.logout(AbstractHttpConfigurer::disable);
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers(publicPaths()).permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
        );
        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(
                NimbusJwtDecoder.withJwkSetUri(properties.jwkSetUri().toString()).build()
        )));
        return http.build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "oefms.gateway.security", name = "enabled", havingValue = "false", matchIfMissing = true)
    SecurityFilterChain openGatewaySecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);
        http.logout(AbstractHttpConfigurer::disable);
        http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
        return http.build();
    }

    private static String[] publicPaths() {
        return new String[]{
                "/actuator/health",
                "/actuator/info",
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/v3/api-docs",
                "/v3/api-docs/**",
                "/gateway/routes",
                "/auth/**",
                "/oauth2/**",
                "/userinfo",
                "/login",
                "/login/**",
                "/.well-known/**"
        };
    }
}
