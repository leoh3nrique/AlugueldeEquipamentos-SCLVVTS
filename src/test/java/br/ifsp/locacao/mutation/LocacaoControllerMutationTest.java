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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("Mutation")
@Tag("UnitTest")
class LocacaoControllerMutationTest {

    @Test
    @DisplayName("GET por ID retorna a reserva completa com status 200")
    void buscaPorIdRetornaJsonDaReserva() throws Exception {
        ConsultarLocacoesService consultar = mock(ConsultarLocacoesService.class);
        LocacaoController controller = new LocacaoController(
                mock(CriarLocacaoService.class), mock(AlterarLocacaoService.class),
                mock(ConfirmarRetiradaService.class), mock(RenovarLocacaoService.class),
                mock(RegistrarDevolucaoService.class), mock(CancelarLocacaoService.class), consultar);
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper)).build();
        ItemLocacao camera = new ItemLocacao(new CodigoEquipamento("CAM01"),
                "Câmera", new Dinheiro(new BigDecimal("100.00")));
        Locacao reserva = new Locacao("cliente-1", new PeriodoLocacao(
                LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 13)), List.of(camera),
                Clock.fixed(Instant.parse("2026-11-06T12:00:00Z"), ZoneOffset.UTC));
        when(consultar.buscarPorId(reserva.getId())).thenReturn(reserva);

        String resposta = mvc.perform(get("/api/locacoes/{id}", reserva.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);

        assertThat(mapper.readTree(resposta)).isEqualTo(mapper.readTree("""
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
                """.formatted(reserva.getId())));
        verify(consultar).buscarPorId(reserva.getId());
    }
}
