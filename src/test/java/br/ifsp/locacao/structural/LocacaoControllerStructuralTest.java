package br.ifsp.locacao.structural;

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
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("Structural")
@Tag("UnitTest")
class LocacaoControllerStructuralTest {
    private CriarLocacaoService criar;
    private ObjectMapper mapper;
    private MockMvc mvc;

    @BeforeEach
    void preparaApi() {
        criar = mock(CriarLocacaoService.class);
        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mvc = MockMvcBuilders.standaloneSetup(new LocacaoController(criar, mock(AlterarLocacaoService.class),
                        mock(ConfirmarRetiradaService.class), mock(RenovarLocacaoService.class),
                        mock(RegistrarDevolucaoService.class), mock(CancelarLocacaoService.class),
                        mock(ConsultarLocacoesService.class)))
                .setControllerAdvice(new ApiExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper)).build();
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", "[null]"})
    @DisplayName("Reserva sem lista de itens ou com item nulo retorna 400 sem chegar ao serviço")
    void reservaComItensAusentesRetornaErro(String itens) throws Exception {
        var resposta = mvc.perform(post("/api/locacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":"cliente-1","inicio":"2026-11-10","fim":"2026-11-13","itens":%s}
                                """.formatted(itens)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertThat(mapper.readTree(resposta.getContentAsByteArray())).isEqualTo(mapper.readTree("""
                {"mensagem":"Itens são obrigatórios"}
                """));
        verifyNoInteractions(criar);
    }
}
