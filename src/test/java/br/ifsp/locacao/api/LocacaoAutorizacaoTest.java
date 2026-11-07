package br.ifsp.locacao.api;

import br.ifsp.locacao.application.*;
import br.ifsp.locacao.infrastructure.SecurityConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("UnitTest")
@WebMvcTest({LocacaoController.class, AutenticacaoController.class})
@Import(SecurityConfiguration.class)
class LocacaoAutorizacaoTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @MockitoBean private CriarLocacaoService criar;
    @MockitoBean private AlterarLocacaoService alterar;
    @MockitoBean private ConfirmarRetiradaService retirada;
    @MockitoBean private RenovarLocacaoService renovar;
    @MockitoBean private RegistrarDevolucaoService devolver;
    @MockitoBean private CancelarLocacaoService cancelar;
    @MockitoBean private ConsultarLocacoesService consultar;
    @MockitoBean private CadastrarUsuarioService cadastro;
    @MockitoBean private AutenticarUsuarioService autenticacao;

    @ParameterizedTest
    @MethodSource("operacoesDeLocacao")
    @DisplayName("Todas as operações de locação exigem login mesmo com CSRF válido")
    void recusaLocacoesSemLogin(String metodo, String caminho, String corpo) throws Exception {
        MockHttpSession sessao = new MockHttpSession();
        var csrf = obterCsrf(sessao);

        var resposta = mvc.perform(request(HttpMethod.valueOf(metodo), caminho)
                        .session(sessao)
                        .header(csrf.path("headerName").textValue(), csrf.path("token").textValue())
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertThat(mapper.readTree(resposta.getContentAsByteArray())).isEqualTo(mapper.readTree("""
                {"mensagem":"Sessão não autenticada"}
                """));
        verifyNoInteractions(criar, alterar, retirada, renovar, devolver, cancelar, consultar);
    }

    private JsonNode obterCsrf(MockHttpSession sessao) throws Exception {
        var resultado = mvc.perform(get("/api/auth/csrf").session(sessao))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andReturn();
        var dados = mapper.readTree(resultado.getResponse().getContentAsByteArray());
        assertThat(dados.size()).isEqualTo(2);
        assertThat(dados.path("token").isTextual()).isTrue();
        assertThat(dados.path("token").textValue()).isNotBlank();
        assertThat(dados.path("headerName").textValue()).isEqualTo("X-CSRF-TOKEN");
        return dados;
    }

    private static Stream<Arguments> operacoesDeLocacao() {
        String base = "/api/locacoes/00000000-0000-0000-0000-000000000010";
        String reserva = """
                {"clienteId":"00000000-0000-0000-0000-000000000001","inicio":"2026-11-10","fim":"2026-11-13",\
                "itens":[{"codigo":"CAM01","descricao":"Câmera","diaria":100.00}]}
                """;
        return Stream.of(
                Arguments.of("GET", "/api/locacoes?clienteId=00000000-0000-0000-0000-000000000001", ""),
                Arguments.of("GET", base, ""),
                Arguments.of("POST", "/api/locacoes", reserva),
                Arguments.of("PUT", base, reserva),
                Arguments.of("POST", base + "/retirada", "{\"data\":\"2026-11-10\"}"),
                Arguments.of("POST", base + "/renovacoes", "{\"dias\":3,\"data\":\"2026-11-11\"}"),
                Arguments.of("POST", base + "/devolucoes", "{\"codigos\":[\"CAM01\"],\"data\":\"2026-11-12\"}"),
                Arguments.of("POST", base + "/cancelamento", ""));
    }
}
