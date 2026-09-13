package org.openefms.gateway.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableConfigurationProperties({GatewayCorsProperties.class, GatewaySecurityProperties.class})
public class GatewaySecurityConfiguration {

    private final GatewayCorsProperties corsProperties;

    public GatewaySecurityConfiguration(GatewayCorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Bean
    @ConditionalOnProperty(prefix = "oefms.gateway.security", name = "enabled", havingValue = "true")
    public SecurityFilterChain jwtGatewaySecurityFilterChain(
            HttpSecurity http,
            GatewaySecurityProperties properties
    ) throws Exception {
        http.cors(Customizer.withDefaults());
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
    public SecurityFilterChain openGatewaySecurityFilterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults());
        http.csrf(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);
        http.logout(AbstractHttpConfigurer::disable);
        http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    public FilterRegistrationBean<CorsFilter> gatewayCorsFilter(
            @Qualifier("gatewayCorsConfigurationSource") CorsConfigurationSource corsConfigurationSource
    ) {
        FilterRegistrationBean<CorsFilter> registration = new FilterRegistrationBean<>(
                new CorsFilter(corsConfigurationSource)
        );
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    @Bean
    public CorsConfigurationSource gatewayCorsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProperties.allowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("X-Correlation-Id"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
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
