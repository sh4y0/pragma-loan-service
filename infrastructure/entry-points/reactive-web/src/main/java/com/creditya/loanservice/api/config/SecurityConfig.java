package com.creditya.loanservice.api.config;

import com.creditya.loanservice.api.exception.CustomAccessDeniedHandler;
import com.creditya.loanservice.api.exception.CustomAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity httpSecurity,
                                                         CustomAccessDeniedHandler accessDeniedHandler,
                                                         CustomAuthenticationEntryPoint authenticationEntryPoint) {
        return httpSecurity
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.POST,"/api/v1/loan").hasAnyAuthority("ROLE_CUSTOMER", "ROLE_ADVISER")
                        .pathMatchers(HttpMethod.GET,"/api/v1/loan").hasAnyAuthority("ROLE_ADMIN", "ROLE_ADVISER")
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwtSpec -> jwtSpec.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                .addFilterAfter(new SecurityHeadersConfig(), SecurityWebFiltersOrder.AUTHENTICATION)
                .exceptionHandling(spec -> spec
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .build();
    }


    @Bean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        converter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }
}
