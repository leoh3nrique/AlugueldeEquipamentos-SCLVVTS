package br.ifsp.locacao.api;

import br.ifsp.locacao.application.*;
import br.ifsp.locacao.domain.*;
import br.ifsp.locacao.infrastructure.SecurityConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
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
    @MockitoBean(name = "consultarLocacoesService") private ConsultarLocacoesService consultar;
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

    @ParameterizedTest
    @CsvSource({"criar,true", "criar,false", "listar,true", "listar,false", "filtrar,true", "filtrar,false"})
    @DisplayName("Criação e listagem aceitam apenas o cliente da sessão")
    void restringeClienteNaCriacaoENaListagem(String operacao, boolean proprioCliente) throws Exception {
        String clienteId = proprioCliente ? "00000000-0000-0000-0000-000000000001"
                : "00000000-0000-0000-0000-000000000002";
        Locacao locacao = reservaDoCliente(clienteId);
        when(criar.criar(eq(clienteId), any(PeriodoLocacao.class), anyList())).thenReturn(locacao);
        when(consultar.consultarPorCliente(clienteId)).thenReturn(List.of(locacao));
        when(consultar.consultarPorCliente(clienteId, EstadoLocacao.ABERTA)).thenReturn(List.of(locacao));
        MockHttpSession sessao = sessaoDoUsuario();
        var csrf = obterCsrf(sessao);
        String caminho = operacao.equals("criar") ? "/api/locacoes" : "/api/locacoes?clienteId=" + clienteId;
        if (operacao.equals("filtrar")) caminho += "&estado=ABERTA";
        var resposta = mvc.perform(request(operacao.equals("criar") ? HttpMethod.POST : HttpMethod.GET, caminho)
                        .session(sessao)
                        .header(csrf.path("headerName").textValue(), csrf.path("token").textValue())
                        .contentType(MediaType.APPLICATION_JSON).content(corpoReserva(clienteId)))
                .andExpect(status().is(proprioCliente ? (operacao.equals("criar") ? 201 : 200) : 403))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andReturn().getResponse();

        if (proprioCliente) {
            JsonNode esperado = jsonDaReservaEsperado(locacao.getId(), clienteId);
            if (operacao.equals("criar")) {
                assertThat(resposta.getHeader("Location")).isEqualTo("/api/locacoes/" + locacao.getId());
                verify(criar).criar(eq(clienteId), any(PeriodoLocacao.class), anyList());
            } else {
                esperado = mapper.createArrayNode().add(esperado);
                if (operacao.equals("filtrar")) verify(consultar).consultarPorCliente(clienteId, EstadoLocacao.ABERTA);
                else verify(consultar).consultarPorCliente(clienteId);
            }
            assertThat(mapper.readTree(resposta.getContentAsByteArray())).isEqualTo(esperado);
        } else {
            assertThat(mapper.readTree(resposta.getContentAsByteArray())).isEqualTo(mapper.readTree("""
                    {"mensagem":"Acesso não permitido"}
                    """));
            verifyNoInteractions(criar, alterar, retirada, renovar, devolver, cancelar, consultar);
        }
    }

    @ParameterizedTest
    @CsvSource({"buscar,true", "buscar,false", "alterar,true", "alterar,false", "retirar,true", "retirar,false",
            "renovar,true", "renovar,false", "devolver,true", "devolver,false", "cancelar,true", "cancelar,false"})
    @DisplayName("Operações por ID permitem apenas o proprietário da locação")
    void restringeOperacoesAoProprietario(String operacao, boolean proprietario) throws Exception {
        String clienteId = proprietario ? "00000000-0000-0000-0000-000000000001"
                : "00000000-0000-0000-0000-000000000002";
        Locacao locacao = reservaDoCliente(clienteId);
        UUID id = locacao.getId();
        ObjectNode esperado = (ObjectNode) jsonDaReservaEsperado(id, clienteId);
        ObjectNode itemEsperado = (ObjectNode) esperado.path("itens").get(0);
        LocalDate dia10 = LocalDate.of(2026, 11, 10);
        LocalDate dia11 = LocalDate.of(2026, 11, 11);
        LocalDate dia12 = LocalDate.of(2026, 11, 12);
        PeriodoLocacao novoPeriodo = new PeriodoLocacao(dia10, LocalDate.of(2026, 11, 14));
        String caminho = "/api/locacoes/" + id;
        HttpMethod metodo = HttpMethod.POST;
        String corpo = "";
        switch (operacao) {
            case "buscar" -> metodo = HttpMethod.GET;
            case "alterar" -> {
                metodo = HttpMethod.PUT;
                corpo = corpoReserva(clienteId).replace("2026-11-13", "2026-11-14");
                locacao.alterar(novoPeriodo, List.of(new ItemLocacao(new CodigoEquipamento("CAM01"),
                        "Câmera", new Dinheiro(new BigDecimal("100.00")))));
                esperado.put("fim", "2026-11-14").put("valorNormal", 400.00).put("valorTotal", 400.00);
                itemEsperado.put("fimContratado", "2026-11-14");
            }
            case "retirar" -> {
                caminho += "/retirada";
                corpo = "{\"data\":\"2026-11-10\"}";
                locacao.confirmarRetirada(dia10);
                esperado.put("estado", "EM_ANDAMENTO");
            }
            case "renovar" -> {
                caminho += "/renovacoes";
                corpo = "{\"dias\":3,\"data\":\"2026-11-11\"}";
                locacao.confirmarRetirada(dia10);
                locacao.renovar(3, dia11);
                esperado.put("estado", "EM_ANDAMENTO").put("renovacoes", 1).put("fim", "2026-11-16")
                        .put("valorNormal", 600.00).put("valorTotal", 600.00);
                itemEsperado.put("fimContratado", "2026-11-16");
            }
            case "devolver" -> {
                caminho += "/devolucoes";
                corpo = "{\"codigos\":[\"CAM01\"],\"data\":\"2026-11-12\"}";
                locacao.confirmarRetirada(dia10);
                locacao.registrarDevolucao(List.of(new CodigoEquipamento("CAM01")), dia12);
                esperado.put("estado", "FINALIZADA");
                itemEsperado.put("devolvido", true).put("dataDevolucao", "2026-11-12");
            }
            case "cancelar" -> {
                caminho += "/cancelamento";
                locacao.cancelar();
                esperado.put("estado", "CANCELADA");
            }
        }
        when(consultar.buscarPorId(id)).thenReturn(locacao);
        when(alterar.alterar(eq(id), any(PeriodoLocacao.class), anyList())).thenReturn(locacao);
        when(retirada.confirmar(id, dia10)).thenReturn(locacao);
        when(renovar.renovar(id, 3, dia11)).thenReturn(locacao);
        when(devolver.registrar(id, List.of(new CodigoEquipamento("CAM01")), dia12)).thenReturn(locacao);
        when(cancelar.cancelar(id)).thenReturn(locacao);
        MockHttpSession sessao = sessaoDoUsuario();
        var csrf = obterCsrf(sessao);

        var resposta = mvc.perform(request(metodo, caminho).session(sessao)
                        .header(csrf.path("headerName").textValue(), csrf.path("token").textValue())
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().is(proprietario ? 200 : 403))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andReturn().getResponse();

        if (proprietario) {
            assertThat(mapper.readTree(resposta.getContentAsByteArray())).isEqualTo(esperado);
            switch (operacao) {
                case "buscar" -> verify(consultar, atLeastOnce()).buscarPorId(id);
                case "alterar" -> verify(alterar).alterar(eq(id), eq(novoPeriodo), anyList());
                case "retirar" -> verify(retirada).confirmar(id, dia10);
                case "renovar" -> verify(renovar).renovar(id, 3, dia11);
                case "devolver" -> verify(devolver).registrar(id, List.of(new CodigoEquipamento("CAM01")), dia12);
                case "cancelar" -> verify(cancelar).cancelar(id);
            }
        } else {
            assertThat(mapper.readTree(resposta.getContentAsByteArray())).isEqualTo(mapper.readTree("""
                    {"mensagem":"Acesso não permitido"}
                    """));
            verify(consultar).buscarPorId(id);
            verifyNoInteractions(criar, alterar, retirada, renovar, devolver, cancelar);
        }
    }

    private MockHttpSession sessaoDoUsuario() {
        var usuario = new AutenticacaoController.UsuarioSaida(
                UUID.fromString("00000000-0000-0000-0000-000000000001"), "Ana Silva", "ana@example.com");
        var contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(usuario, null, List.of()));
        MockHttpSession sessao = new MockHttpSession();
        sessao.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, contexto);
        return sessao;
    }

    private Locacao reservaDoCliente(String clienteId) {
        return new Locacao(clienteId, new PeriodoLocacao(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 13)),
                List.of(new ItemLocacao(new CodigoEquipamento("CAM01"), "Câmera", new Dinheiro(new BigDecimal("100.00")))),
                Clock.fixed(Instant.parse("2026-11-06T12:00:00Z"), ZoneOffset.UTC));
    }

    private String corpoReserva(String clienteId) {
        return """
                {"clienteId":"%s","inicio":"2026-11-10","fim":"2026-11-13",                "itens":[{"codigo":"CAM01","descricao":"Câmera","diaria":100.00}]}
                """.formatted(clienteId);
    }

    private JsonNode jsonDaReservaEsperado(UUID id, String clienteId) throws Exception {
        return mapper.readTree("""
                {"id":"%s","clienteId":"%s","criadaEm":"2026-11-06T12:00:00Z",                "inicio":"2026-11-10","fim":"2026-11-13","estado":"ABERTA","renovacoes":0,                "valorNormal":300.00,"multa":0.00,"valorTotal":300.00,                "itens":[{"codigo":"CAM01","descricao":"Câmera","diaria":100.00,"devolvido":false,                "dataDevolucao":null,"fimContratado":"2026-11-13"}]}
                """.formatted(id, clienteId));
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
