package br.ifsp.locacao.infrastructure;

import br.ifsp.locacao.api.ApiExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

import java.util.List;

@Configuration
@EnableMethodSecurity
@SecurityScheme(name = "sessao", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.COOKIE, paramName = "JSESSIONID")
public class SecurityConfiguration {
    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    CsrfTokenRepository csrfTokenRepository() {
        return new HttpSessionCsrfTokenRepository();
    }

    @Bean
    SessionAuthenticationStrategy sessionAuthenticationStrategy(CsrfTokenRepository csrfTokens) {
        return new CompositeSessionAuthenticationStrategy(List.of(
                new ChangeSessionIdAuthenticationStrategy(),
                new CsrfAuthenticationStrategy(csrfTokens)));
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityContextRepository contexts,
                                           CsrfTokenRepository csrfTokens, ObjectMapper mapper) throws Exception {
        return http
                .authorizeHttpRequests(regras -> regras
                        .requestMatchers("/api/locacoes", "/api/locacoes/**").authenticated()
                        .anyRequest().permitAll())
                .exceptionHandling(erros -> erros.authenticationEntryPoint((requisicao, resposta, erro) -> {
                    resposta.setStatus(401);
                    resposta.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    mapper.writeValue(resposta.getOutputStream(), new ApiExceptionHandler.Erro("Sessão não autenticada"));
                }).accessDeniedHandler((requisicao, resposta, erro) -> {
                    resposta.setStatus(403);
                    resposta.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    mapper.writeValue(resposta.getOutputStream(), new ApiExceptionHandler.Erro("Acesso não permitido"));
                }))
                .securityContext(contexto -> contexto.securityContextRepository(contexts))
                .csrf(csrf -> csrf.csrfTokenRepository(csrfTokens))
                .build();
    }
}
