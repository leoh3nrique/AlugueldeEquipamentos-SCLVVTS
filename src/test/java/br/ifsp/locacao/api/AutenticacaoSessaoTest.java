package br.ifsp.locacao.api;

import br.ifsp.locacao.application.AutenticarUsuarioService;
import br.ifsp.locacao.application.CadastrarUsuarioService;
import br.ifsp.locacao.domain.Usuario;
import br.ifsp.locacao.infrastructure.SecurityConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("UnitTest")
@WebMvcTest(AutenticacaoController.class)
@Import(SecurityConfiguration.class)
class AutenticacaoSessaoTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private CsrfTokenRepository csrfTokens;
    @MockitoBean
    private CadastrarUsuarioService cadastro;
    @MockitoBean
    private AutenticarUsuarioService autenticacao;

    @Test
    @DisplayName("Login retorna o usuário e salva a autenticação em uma sessão com novo ID")
    void loginRetornaUsuarioECriaSessao() throws Exception {
        Usuario usuario = new Usuario(UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "Ana Silva", "ana@example.com", "hash-que-nao-deve-sair");
        when(autenticacao.autenticar("ana@example.com", "SenhaDeTeste123!"))
                .thenReturn(Optional.of(usuario));
        MockHttpSession sessao = new MockHttpSession();
        String idAnterior = sessao.getId();
        var respostaCsrf = mvc.perform(get("/api/auth/csrf").session(sessao))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();
        var dadosCsrf = mapper.readTree(respostaCsrf.getResponse().getContentAsByteArray());
        assertThat(dadosCsrf.size()).isEqualTo(2);
        assertThat(dadosCsrf.path("token").isTextual()).isTrue();
        assertThat(dadosCsrf.path("token").textValue()).isNotBlank();
        assertThat(dadosCsrf.path("headerName").textValue()).isEqualTo("X-CSRF-TOKEN");
        var tokenAnterior = csrfTokens.loadToken(respostaCsrf.getRequest());
        assertThat(tokenAnterior).isNotNull();

        var resultado = mvc.perform(post("/api/auth/login")
                        .session(sessao)
                        .header(dadosCsrf.path("headerName").textValue(), dadosCsrf.path("token").textValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"ana@example.com","senha":"SenhaDeTeste123!"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        assertThat(mapper.readTree(resultado.getResponse().getContentAsByteArray())).isEqualTo(mapper.readTree("""
                {"id":"00000000-0000-0000-0000-000000000001","nome":"Ana Silva","email":"ana@example.com"}
                """));
        assertThat(sessao.getId()).isNotEqualTo(idAnterior);
        SecurityContext contexto = (SecurityContext) sessao.getAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertThat(contexto).isNotNull();
        assertThat(contexto.getAuthentication().isAuthenticated()).isTrue();
        assertThat(contexto.getAuthentication().getPrincipal()).isEqualTo(
                new AutenticacaoController.UsuarioSaida(usuario.id(), usuario.nome(), usuario.email()));
        assertThat(contexto.getAuthentication().getCredentials()).isNull();
        var tokenAtual = csrfTokens.loadToken(resultado.getRequest());
        assertThat(Optional.ofNullable(tokenAtual).map(CsrfToken::getToken))
                .isNotEqualTo(Optional.of(tokenAnterior.getToken()));
        verify(autenticacao).autenticar("ana@example.com", "SenhaDeTeste123!");
    }

    @ParameterizedTest
    @CsvSource({"ana@example.com, SenhaErrada123!", "inexistente@example.com, SenhaDeTeste123!"})
    @DisplayName("Login inválido retorna 401 com a mesma mensagem e não autentica a sessão")
    void loginRecusaCredenciaisInvalidas(String email, String senha) throws Exception {
        when(autenticacao.autenticar(email, senha)).thenReturn(Optional.empty());
        MockHttpSession sessao = new MockHttpSession();
        String idAnterior = sessao.getId();
        var respostaCsrf = mvc.perform(get("/api/auth/csrf").session(sessao))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();
        var dadosCsrf = mapper.readTree(respostaCsrf.getResponse().getContentAsByteArray());
        assertThat(dadosCsrf.size()).isEqualTo(2);
        assertThat(dadosCsrf.path("token").isTextual()).isTrue();
        assertThat(dadosCsrf.path("token").textValue()).isNotBlank();
        assertThat(dadosCsrf.path("headerName").textValue()).isEqualTo("X-CSRF-TOKEN");
        var tokenAnterior = csrfTokens.loadToken(respostaCsrf.getRequest());
        assertThat(tokenAnterior).isNotNull();

        var resultado = mvc.perform(post("/api/auth/login")
                        .session(sessao)
                        .header(dadosCsrf.path("headerName").textValue(), dadosCsrf.path("token").textValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(new AutenticacaoController.LoginEntrada(email, senha))))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        assertThat(mapper.readTree(resultado.getResponse().getContentAsByteArray())).isEqualTo(mapper.readTree("""
                {"mensagem":"E-mail ou senha inválidos"}
                """));
        assertThat(sessao.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY)).isNull();
        assertThat(sessao.getId()).isEqualTo(idAnterior);
        assertThat(csrfTokens.loadToken(resultado.getRequest()).getToken()).isEqualTo(tokenAnterior.getToken());
        verify(autenticacao).autenticar(email, senha);
    }
}
