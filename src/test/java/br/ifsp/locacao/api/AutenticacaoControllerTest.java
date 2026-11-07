package br.ifsp.locacao.api;

import br.ifsp.locacao.application.AutenticarUsuarioService;
import br.ifsp.locacao.application.CadastrarUsuarioService;
import br.ifsp.locacao.domain.Usuario;
import br.ifsp.locacao.domain.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("UnitTest")
class AutenticacaoControllerTest {
    @Test
    @DisplayName("Cadastro retorna 201 com ID, nome e e-mail, sem expor senha ou hash")
    void cadastroRetornaUsuarioSemExporSenha() throws Exception {
        CadastrarUsuarioService cadastro = mock(CadastrarUsuarioService.class);
        Usuario usuario = new Usuario(UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "Ana Silva", "ana@example.com", "hash-que-nao-deve-sair");
        when(cadastro.cadastrar("Ana Silva", "ana@example.com", "SenhaDeTeste123!"))
                .thenReturn(usuario);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new AutenticacaoController(cadastro,
                        mock(AutenticarUsuarioService.class), mock(SessionAuthenticationStrategy.class),
                        mock(SecurityContextRepository.class)))
                .setControllerAdvice(new ApiExceptionHandler()).build();
        ObjectMapper mapper = new ObjectMapper();

        var resposta = mvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Ana Silva","email":"ana@example.com","senha":"SenhaDeTeste123!"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertThat(mapper.readTree(resposta.getContentAsByteArray())).isEqualTo(mapper.readTree("""
                {"id":"00000000-0000-0000-0000-000000000001","nome":"Ana Silva","email":"ana@example.com"}
                """));
        verify(cadastro).cadastrar("Ana Silva", "ana@example.com", "SenhaDeTeste123!");
    }

    @ParameterizedTest(name = "[{index}] {2}")
    @CsvSource(delimiter = '|', textBlock = """
            {"email":"ana@example.com","senha":"SenhaDeTeste123!"} | 400 | Nome é obrigatório
            {"nome":"Ana Silva","email":"ana.example.com","senha":"SenhaDeTeste123!"} | 400 | E-mail inválido
            {"nome":"Ana Silva","email":"ana@example.com","senha":""} | 400 | Senha é obrigatória
            {"nome":"Ana Silva","email":" ANA@EXAMPLE.COM ","senha":"SenhaDeTeste123!"} | 409 | E-mail já cadastrado
            """)
    @DisplayName("Cadastro informa campos inválidos e e-mail duplicado sem salvar usuário")
    void cadastroRetornaErrosDeValidacao(String corpo, int statusEsperado, String mensagem) throws Exception {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        Usuario existente = new Usuario(UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "Ana Silva", "ana@example.com", "hash-original");
        when(repository.buscarPorEmail("ana@example.com")).thenReturn(Optional.of(existente));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new AutenticacaoController(
                        new CadastrarUsuarioService(repository), mock(AutenticarUsuarioService.class),
                        mock(SessionAuthenticationStrategy.class), mock(SecurityContextRepository.class)))
                .setControllerAdvice(new ApiExceptionHandler()).build();
        ObjectMapper mapper = new ObjectMapper();

        var resposta = mvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().is(statusEsperado))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertThat(mapper.readTree(resposta.getContentAsByteArray()))
                .isEqualTo(mapper.createObjectNode().put("mensagem", mensagem));
        verify(repository, never()).salvar(any(Usuario.class));
    }

    @Test
    @DisplayName("Login disponibiliza o usuário autenticado na própria requisição")
    void loginAutenticaARequisicaoAtual() throws Exception {
        AutenticarUsuarioService autenticacao = mock(AutenticarUsuarioService.class);
        Usuario usuario = new Usuario(UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "Ana Silva", "ana@example.com", "hash-que-nao-deve-sair");
        when(autenticacao.autenticar("ana@example.com", "SenhaDeTeste123!"))
                .thenReturn(Optional.of(usuario));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new AutenticacaoController(
                        mock(CadastrarUsuarioService.class), autenticacao,
                        mock(SessionAuthenticationStrategy.class), mock(SecurityContextRepository.class)))
                .addFilter((requisicao, resposta, cadeia) -> {
                    cadeia.doFilter(requisicao, resposta);
                    var requisicaoAutenticada = new SecurityContextHolderAwareRequestWrapper(
                            (HttpServletRequest) requisicao, "ROLE_");
                    assertThat(requisicaoAutenticada.getUserPrincipal()).isInstanceOf(Authentication.class);
                    Authentication principal = (Authentication) requisicaoAutenticada.getUserPrincipal();
                    assertThat(principal.isAuthenticated()).isTrue();
                    assertThat(principal.getPrincipal()).isEqualTo(new AutenticacaoController.UsuarioSaida(
                            usuario.id(), "Ana Silva", "ana@example.com"));
                    assertThat(principal.getCredentials()).isNull();
                }).build();
        ObjectMapper mapper = new ObjectMapper();
        SecurityContextHolder.clearContext();
        try {
            var resposta = mvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email":"ana@example.com","senha":"SenhaDeTeste123!"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andReturn().getResponse();

            assertThat(mapper.readTree(resposta.getContentAsByteArray())).isEqualTo(mapper.readTree("""
                    {"id":"00000000-0000-0000-0000-000000000001","nome":"Ana Silva","email":"ana@example.com"}
                    """));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
