package br.ifsp.locacao.mutation;

import br.ifsp.locacao.api.ApiExceptionHandler;
import br.ifsp.locacao.api.LocacaoController;
import br.ifsp.locacao.application.AlterarLocacaoService;
import br.ifsp.locacao.application.CancelarLocacaoService;
import br.ifsp.locacao.application.ConfirmarRetiradaService;
import br.ifsp.locacao.application.ConsultarLocacoesService;
import br.ifsp.locacao.application.CriarLocacaoService;
import br.ifsp.locacao.application.RegistrarDevolucaoService;
import br.ifsp.locacao.application.RenovarLocacaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("Mutation")
@Tag("UnitTest")
class ApiExceptionHandlerMutationTest {

    private ConsultarLocacoesService consultar;
    private CancelarLocacaoService cancelar;
    private RegistrarDevolucaoService devolver;
    private ObjectMapper mapper;
    private MockMvc mvc;

    @BeforeEach
    void preparaApi() {
        consultar = mock(ConsultarLocacoesService.class);
        cancelar = mock(CancelarLocacaoService.class);
        devolver = mock(RegistrarDevolucaoService.class);
        LocacaoController controller = new LocacaoController(
                mock(CriarLocacaoService.class), mock(AlterarLocacaoService.class),
                mock(ConfirmarRetiradaService.class), mock(RenovarLocacaoService.class),
                devolver, cancelar, consultar);
        mapper = new ObjectMapper().registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper)).build();
    }

    @Test
    @DisplayName("GET de locação inexistente retorna status 404 e mensagem em JSON")
    void buscaLocacaoInexistenteRetornaMensagemDeErro() throws Exception {
        UUID id = UUID.randomUUID();
        when(consultar.buscarPorId(id)).thenThrow(new NoSuchElementException("Locação não encontrada"));

        String resposta = mvc.perform(get("/api/locacoes/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(mapper.readTree(resposta)).isEqualTo(mapper.readTree("""
                {"mensagem": "Locação não encontrada"}
                """));
        verify(consultar).buscarPorId(id);
    }

    @Test
    @DisplayName("POST de cancelamento em estado incompatível retorna status 409 e mensagem em JSON")
    void cancelamentoEmEstadoIncompativelRetornaMensagemDeErro() throws Exception {
        UUID id = UUID.randomUUID();
        when(cancelar.cancelar(id)).thenThrow(
                new IllegalStateException("Somente locações abertas podem ser canceladas"));

        String resposta = mvc.perform(post("/api/locacoes/{id}/cancelamento", id))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(mapper.readTree(resposta)).isEqualTo(mapper.readTree("""
                {"mensagem": "Somente locações abertas podem ser canceladas"}
                """));
        verify(cancelar).cancelar(id);
    }

    @Test
    @DisplayName("POST de devolução sem códigos retorna status 400 e mensagem em JSON")
    void devolucaoSemCodigosRetornaMensagemDeErro() throws Exception {
        UUID id = UUID.randomUUID();

        String resposta = mvc.perform(post("/api/locacoes/{id}/devolucoes", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"data": "2026-11-15"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(mapper.readTree(resposta)).isEqualTo(mapper.readTree("""
                {"mensagem": "Códigos são obrigatórios"}
                """));
        verifyNoInteractions(devolver);
    }

    @Test
    @DisplayName("GET com ID inválido retorna status 400 e mensagem em JSON")
    void buscaComIdInvalidoRetornaMensagemDeErro() throws Exception {
        String resposta = mvc.perform(get("/api/locacoes/id-invalido"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(mapper.readTree(resposta)).isEqualTo(mapper.readTree("""
                {"mensagem": "Requisição inválida: confira datas, identificadores e campos obrigatórios"}
                """));
        verifyNoInteractions(consultar);
    }

    @Test
    @DisplayName("GET com falha no banco retorna status 503 e mensagem em JSON")
    void bancoIndisponivelRetornaMensagemDeErro() throws Exception {
        UUID id = UUID.randomUUID();
        when(consultar.buscarPorId(id)).thenThrow(
                new DataAccessResourceFailureException("Falha ao abrir conexão SQLite"));

        String resposta = mvc.perform(get("/api/locacoes/{id}", id))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(mapper.readTree(resposta)).isEqualTo(mapper.readTree("""
                {"mensagem": "Banco indisponível; tente novamente"}
                """));
        verify(consultar).buscarPorId(id);
    }
}
