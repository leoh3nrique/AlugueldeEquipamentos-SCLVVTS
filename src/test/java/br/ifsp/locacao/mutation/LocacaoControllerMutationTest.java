package br.ifsp.locacao.mutation;

import br.ifsp.locacao.api.LocacaoController;
import br.ifsp.locacao.application.AlterarLocacaoService;
import br.ifsp.locacao.application.CancelarLocacaoService;
import br.ifsp.locacao.application.ConfirmarRetiradaService;
import br.ifsp.locacao.application.ConsultarLocacoesService;
import br.ifsp.locacao.application.CriarLocacaoService;
import br.ifsp.locacao.application.RegistrarDevolucaoService;
import br.ifsp.locacao.application.RenovarLocacaoService;
import br.ifsp.locacao.domain.CodigoEquipamento;
import br.ifsp.locacao.domain.Dinheiro;
import br.ifsp.locacao.domain.ItemLocacao;
import br.ifsp.locacao.domain.Locacao;
import br.ifsp.locacao.domain.PeriodoLocacao;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("Mutation")
@Tag("UnitTest")
class LocacaoControllerMutationTest {

    private CriarLocacaoService criar;
    private ConsultarLocacoesService consultar;
    private CancelarLocacaoService cancelar;
    private ConfirmarRetiradaService retirada;
    private RenovarLocacaoService renovar;
    private ObjectMapper mapper;
    private MockMvc mvc;
    private Locacao reserva;

    @BeforeEach
    void preparaApi() {
        criar = mock(CriarLocacaoService.class);
        consultar = mock(ConsultarLocacoesService.class);
        cancelar = mock(CancelarLocacaoService.class);
        retirada = mock(ConfirmarRetiradaService.class);
        renovar = mock(RenovarLocacaoService.class);
        LocacaoController controller = new LocacaoController(
                criar, mock(AlterarLocacaoService.class),
                retirada, renovar,
                mock(RegistrarDevolucaoService.class), cancelar, consultar);
        mapper = new ObjectMapper().registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper)).build();
        ItemLocacao camera = new ItemLocacao(new CodigoEquipamento("CAM01"),
                "Câmera", new Dinheiro(new BigDecimal("100.00")));
        reserva = new Locacao("cliente-1", new PeriodoLocacao(
                LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 13)), List.of(camera),
                Clock.fixed(Instant.parse("2026-11-06T12:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    @DisplayName("GET por ID retorna a reserva completa com status 200")
    void buscaPorIdRetornaJsonDaReserva() throws Exception {
        when(consultar.buscarPorId(reserva.getId())).thenReturn(reserva);

        String resposta = mvc.perform(get("/api/locacoes/{id}", reserva.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(mapper.readTree(resposta)).isEqualTo(jsonDaReservaEsperado());
        verify(consultar).buscarPorId(reserva.getId());
    }

    @Test
    @DisplayName("POST cria reserva e retorna status 201, Location e dados completos")
    void criaReservaRetornaJsonELocalizacao() throws Exception {
        when(criar.criar(eq("cliente-1"), eq(reserva.getPeriodo()), anyList())).thenReturn(reserva);

        String resposta = mvc.perform(post("/api/locacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clienteId": "cliente-1",
                                  "inicio": "2026-11-10",
                                  "fim": "2026-11-13",
                                  "itens": [{
                                    "codigo": "CAM01",
                                    "descricao": "Câmera",
                                    "diaria": 100.00
                                  }]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/locacoes/" + reserva.getId()))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(mapper.readTree(resposta)).isEqualTo(jsonDaReservaEsperado());
        ArgumentCaptor<List<ItemLocacao>> itens = ArgumentCaptor.captor();
        verify(criar).criar(eq("cliente-1"), eq(reserva.getPeriodo()), itens.capture());
        assertThat(itens.getValue()).singleElement().satisfies(item -> {
            assertThat(item.codigo().valor()).isEqualTo("CAM01");
            assertThat(item.descricao()).isEqualTo("Câmera");
            assertThat(item.diaria().valor()).isEqualByComparingTo("100.00");
        });
    }

    @Test
    @DisplayName("GET por cliente retorna a lista completa sem filtro de estado")
    void listaLocacoesDoClienteSemFiltroDeEstado() throws Exception {
        when(consultar.consultarPorCliente("cliente-1")).thenReturn(List.of(reserva));

        String resposta = mvc.perform(get("/api/locacoes").param("clienteId", "cliente-1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        JsonNode esperado = mapper.createArrayNode().add(jsonDaReservaEsperado());
        assertThat(mapper.readTree(resposta)).isEqualTo(esperado);
        verify(consultar).consultarPorCliente("cliente-1");
    }

    @Test
    @DisplayName("POST de cancelamento retorna os dados da reserva cancelada com status 200")
    void cancelaReservaRetornaJsonComEstadoCancelada() throws Exception {
        reserva.cancelar();
        when(cancelar.cancelar(reserva.getId())).thenReturn(reserva);

        String resposta = mvc.perform(post("/api/locacoes/{id}/cancelamento", reserva.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        ObjectNode esperado = (ObjectNode) jsonDaReservaEsperado();
        esperado.put("estado", "CANCELADA");
        assertThat(mapper.readTree(resposta)).isEqualTo(esperado);
        verify(cancelar).cancelar(reserva.getId());
    }

    @Test
    @DisplayName("POST de retirada retorna os dados da locação em andamento com status 200")
    void confirmaRetiradaRetornaJsonComEstadoEmAndamento() throws Exception {
        LocalDate dataRetirada = LocalDate.of(2026, 11, 11);
        reserva.confirmarRetirada(dataRetirada);
        when(retirada.confirmar(reserva.getId(), dataRetirada)).thenReturn(reserva);

        String resposta = mvc.perform(post("/api/locacoes/{id}/retirada", reserva.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"data": "2026-11-11"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        ObjectNode esperado = (ObjectNode) jsonDaReservaEsperado();
        esperado.put("estado", "EM_ANDAMENTO");
        assertThat(mapper.readTree(resposta)).isEqualTo(esperado);
        verify(retirada).confirmar(reserva.getId(), dataRetirada);
    }

    @Test
    @DisplayName("POST de renovação retorna prazo e valores atualizados com status 200")
    void renovaLocacaoRetornaJsonComPrazoEValoresAtualizados() throws Exception {
        LocalDate dataSolicitacao = LocalDate.of(2026, 11, 12);
        reserva.confirmarRetirada(LocalDate.of(2026, 11, 10));
        reserva.renovar(3, dataSolicitacao);
        when(renovar.renovar(reserva.getId(), 3, dataSolicitacao)).thenReturn(reserva);

        String resposta = mvc.perform(post("/api/locacoes/{id}/renovacoes", reserva.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"dias": 3, "data": "2026-11-12"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        ObjectNode esperado = (ObjectNode) jsonDaReservaEsperado();
        esperado.put("estado", "EM_ANDAMENTO");
        esperado.put("fim", "2026-11-16");
        esperado.put("renovacoes", 1);
        esperado.put("valorNormal", 600.00);
        esperado.put("valorTotal", 600.00);
        ((ObjectNode) esperado.withArray("itens").get(0)).put("fimContratado", "2026-11-16");
        assertThat(mapper.readTree(resposta)).isEqualTo(esperado);
        verify(renovar).renovar(reserva.getId(), 3, dataSolicitacao);
    }

    private JsonNode jsonDaReservaEsperado() throws Exception {
        return mapper.readTree("""
                {
                  "id": "%s",
                  "clienteId": "cliente-1",
                  "criadaEm": "2026-11-06T12:00:00Z",
                  "inicio": "2026-11-10",
                  "fim": "2026-11-13",
                  "estado": "ABERTA",
                  "renovacoes": 0,
                  "valorNormal": 300.00,
                  "multa": 0.00,
                  "valorTotal": 300.00,
                  "itens": [{
                    "codigo": "CAM01",
                    "descricao": "Câmera",
                    "diaria": 100.00,
                    "devolvido": false,
                    "dataDevolucao": null,
                    "fimContratado": "2026-11-13"
                  }]
                }
                """.formatted(reserva.getId()));
    }
}
