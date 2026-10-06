package br.ifsp.locacao.application;

import br.ifsp.locacao.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@Tag("UnitTest")
@Tag("TDD")
class CriarLocacaoServiceTest {

    @Test
    @DisplayName("#5: criar locação com período de exatamente 30 dias")
    void deveCriarLocacaoComPeriodoDeTrintaDias() {
        LocacaoRepository repository = mock(LocacaoRepository.class);
        CriarLocacaoService service = new CriarLocacaoService(repository);
        LocalDate inicio = LocalDate.of(2026, 10, 6);
        PeriodoLocacao periodo = new PeriodoLocacao(inicio, inicio.plusDays(30));
        List<ItemLocacao> itens = List.of(
                new ItemLocacao(new CodigoEquipamento("CAM01"), "Câmera",
                        new Dinheiro(new BigDecimal("100.00"))));

        Locacao locacao = service.criar("cliente-1", periodo, itens);

        assertThat(locacao.getId()).isNotNull();
        assertThat(locacao.getClienteId()).isEqualTo("cliente-1");
        assertThat(locacao.getPeriodo()).isEqualTo(periodo);
        assertThat(locacao.getItens()).containsExactlyElementsOf(itens);
        assertThat(locacao.getEstado()).isEqualTo(EstadoLocacao.ABERTA);
        verify(repository).salvar(locacao);
    }

    @Test
    @DisplayName("#4: rejeitar criação quando a data final é anterior à inicial")
    void deveRejeitarLocacaoComDataFinalAnteriorAInicial() {
        LocacaoRepository repository = mock(LocacaoRepository.class);
        CriarLocacaoService service = new CriarLocacaoService(repository);
        LocalDate inicio = LocalDate.of(2026, 10, 6);
        LocalDate fim = LocalDate.of(2026, 10, 5);
        List<ItemLocacao> itens = List.of(
                new ItemLocacao(new CodigoEquipamento("CAM01"), "Câmera",
                        new Dinheiro(new BigDecimal("100.00"))));

        assertThatThrownBy(() -> service.criar("cliente-1",
                new PeriodoLocacao(inicio, fim), itens))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data final deve ser posterior à inicial");
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("#3: rejeitar criação quando as datas inicial e final são iguais")
    void deveRejeitarLocacaoComDatasIguais() {
        LocacaoRepository repository = mock(LocacaoRepository.class);
        CriarLocacaoService service = new CriarLocacaoService(repository);
        LocalDate data = LocalDate.of(2026, 10, 6);
        List<ItemLocacao> itens = List.of(
                new ItemLocacao(new CodigoEquipamento("CAM01"), "Câmera",
                        new Dinheiro(new BigDecimal("100.00"))));

        assertThatThrownBy(() -> service.criar("cliente-1",
                new PeriodoLocacao(data, data), itens))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Datas inicial e final devem ser diferentes");
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("#2: criar locação com cliente, período válido e dois equipamentos disponíveis")
    void deveCriarLocacaoAbertaComIdentificadorUnico() {
        LocacaoRepository repository = mock(LocacaoRepository.class);
        CriarLocacaoService service = new CriarLocacaoService(repository);
        PeriodoLocacao periodo = new PeriodoLocacao(
                LocalDate.of(2026, 10, 6), LocalDate.of(2026, 10, 9));
        List<ItemLocacao> itens = List.of(
                new ItemLocacao(new CodigoEquipamento("CAM01"), "Câmera",
                        new Dinheiro(new BigDecimal("100.00"))),
                new ItemLocacao(new CodigoEquipamento("PROJ01"), "Projetor",
                        new Dinheiro(new BigDecimal("50.00"))));

        Locacao locacao = service.criar("cliente-1", periodo, itens);
        Locacao outra = service.criar("cliente-2", periodo, itens);

        assertThat(locacao.getId()).isNotNull().isNotEqualTo(outra.getId());
        assertThat(locacao.getClienteId()).isEqualTo("cliente-1");
        assertThat(locacao.getPeriodo()).isEqualTo(periodo);
        assertThat(locacao.getItens()).containsExactlyElementsOf(itens);
        assertThat(locacao.getEstado()).isEqualTo(EstadoLocacao.ABERTA);
        verify(repository).salvar(locacao);
        verify(repository).salvar(outra);
    }
}
