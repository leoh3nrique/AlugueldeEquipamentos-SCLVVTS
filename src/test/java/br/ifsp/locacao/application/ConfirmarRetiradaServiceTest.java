package br.ifsp.locacao.application;

import br.ifsp.locacao.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@Tag("UnitTest")
@Tag("TDD")
class ConfirmarRetiradaServiceTest {

    @Test
    @DisplayName("#24: rejeitar retirada de locação finalizada")
    void deveRejeitarRetiradaDeLocacaoFinalizada() {
        LocacaoRepository repository = mock(LocacaoRepository.class);
        LocalDate inicio = LocalDate.of(2026, 10, 6);
        PeriodoLocacao periodo = new PeriodoLocacao(inicio, inicio.plusDays(3));
        CodigoEquipamento codigo = new CodigoEquipamento("CAM01");
        ItemLocacao camera = new ItemLocacao(codigo,
                "Câmera", new Dinheiro(new BigDecimal("100.00")));
        List<ItemLocacao> itens = List.of(camera);
        Locacao locacao = new Locacao("cliente-1", periodo, itens);
        UUID idOriginal = locacao.getId();
        locacao.confirmarRetirada(inicio);
        locacao.registrarDevolucao(List.of(codigo), periodo.fim());
        assertThat(locacao.getEstado()).isEqualTo(EstadoLocacao.FINALIZADA);
        when(repository.buscarPorId(idOriginal)).thenReturn(Optional.of(locacao));
        ConfirmarRetiradaService service = new ConfirmarRetiradaService(repository);

        assertThatThrownBy(() -> service.confirmar(idOriginal, periodo.fim()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Somente locações abertas podem ter a retirada confirmada");
        assertThat(locacao.getEstado()).isEqualTo(EstadoLocacao.FINALIZADA);
        assertThat(locacao.getId()).isEqualTo(idOriginal);
        assertThat(locacao.getPeriodo()).isEqualTo(periodo);
        assertThat(locacao.getItens()).containsExactlyElementsOf(itens);
        verify(repository).buscarPorId(idOriginal);
        verify(repository, never()).salvar(any(Locacao.class));
    }

    @Test
    @DisplayName("#23: rejeitar nova confirmação de retirada de locação em andamento")
    void deveRejeitarSegundaConfirmacaoDeRetirada() {
        LocacaoRepository repository = mock(LocacaoRepository.class);
        LocalDate inicio = LocalDate.of(2026, 10, 6);
        PeriodoLocacao periodo = new PeriodoLocacao(inicio, inicio.plusDays(3));
        ItemLocacao camera = new ItemLocacao(new CodigoEquipamento("CAM01"),
                "Câmera", new Dinheiro(new BigDecimal("100.00")));
        List<ItemLocacao> itens = List.of(camera);
        Locacao locacao = new Locacao("cliente-1", periodo, itens);
        UUID idOriginal = locacao.getId();
        locacao.confirmarRetirada(inicio);
        assertThat(locacao.getEstado()).isEqualTo(EstadoLocacao.EM_ANDAMENTO);
        when(repository.buscarPorId(idOriginal)).thenReturn(Optional.of(locacao));
        ConfirmarRetiradaService service = new ConfirmarRetiradaService(repository);

        assertThatThrownBy(() -> service.confirmar(idOriginal, inicio.plusDays(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Somente locações abertas podem ter a retirada confirmada");
        assertThat(locacao.getEstado()).isEqualTo(EstadoLocacao.EM_ANDAMENTO);
        assertThat(locacao.getId()).isEqualTo(idOriginal);
        assertThat(locacao.getPeriodo()).isEqualTo(periodo);
        assertThat(locacao.getItens()).containsExactlyElementsOf(itens);
        verify(repository).buscarPorId(idOriginal);
        verify(repository, never()).salvar(any(Locacao.class));
    }

    @Test
    @DisplayName("#22: rejeitar retirada de locação cancelada")
    void deveRejeitarRetiradaDeLocacaoCancelada() {
        LocacaoRepository repository = mock(LocacaoRepository.class);
        LocalDate inicio = LocalDate.of(2026, 10, 6);
        PeriodoLocacao periodo = new PeriodoLocacao(inicio, inicio.plusDays(3));
        ItemLocacao camera = new ItemLocacao(new CodigoEquipamento("CAM01"),
                "Câmera", new Dinheiro(new BigDecimal("100.00")));
        List<ItemLocacao> itens = List.of(camera);
        Locacao locacao = new Locacao("cliente-1", periodo, itens);
        UUID idOriginal = locacao.getId();
        locacao.cancelar();
        assertThat(locacao.getEstado()).isEqualTo(EstadoLocacao.CANCELADA);
        when(repository.buscarPorId(idOriginal)).thenReturn(Optional.of(locacao));
        ConfirmarRetiradaService service = new ConfirmarRetiradaService(repository);

        assertThatThrownBy(() -> service.confirmar(idOriginal, inicio))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Somente locações abertas podem ter a retirada confirmada");
        assertThat(locacao.getEstado()).isEqualTo(EstadoLocacao.CANCELADA);
        assertThat(locacao.getId()).isEqualTo(idOriginal);
        assertThat(locacao.getPeriodo()).isEqualTo(periodo);
        assertThat(locacao.getItens()).containsExactlyElementsOf(itens);
        verify(repository).buscarPorId(idOriginal);
        verify(repository, never()).salvar(any(Locacao.class));
    }

    @Test
    @DisplayName("#21: rejeitar retirada antes da data inicial da locação")
    void deveRejeitarRetiradaAntesDaDataInicial() {
        LocacaoRepository repository = mock(LocacaoRepository.class);
        LocalDate inicio = LocalDate.of(2026, 10, 6);
        PeriodoLocacao periodo = new PeriodoLocacao(inicio, inicio.plusDays(3));
        ItemLocacao camera = new ItemLocacao(new CodigoEquipamento("CAM01"),
                "Câmera", new Dinheiro(new BigDecimal("100.00")));
        List<ItemLocacao> itens = List.of(camera);
        Locacao locacao = new Locacao("cliente-1", periodo, itens);
        UUID idOriginal = locacao.getId();
        when(repository.buscarPorId(idOriginal)).thenReturn(Optional.of(locacao));
        ConfirmarRetiradaService service = new ConfirmarRetiradaService(repository);

        assertThatThrownBy(() -> service.confirmar(idOriginal, inicio.minusDays(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Retirada não pode ocorrer antes da data inicial");
        assertThat(locacao.getEstado()).isEqualTo(EstadoLocacao.ABERTA);
        assertThat(locacao.getId()).isEqualTo(idOriginal);
        assertThat(locacao.getPeriodo()).isEqualTo(periodo);
        assertThat(locacao.getItens()).containsExactlyElementsOf(itens);
        verify(repository).buscarPorId(idOriginal);
        verify(repository, never()).salvar(any(Locacao.class));
    }

    @Test
    @DisplayName("#20: confirmar retirada de locação aberta na data inicial")
    void deveConfirmarRetiradaNaDataInicial() {
        LocacaoRepository repository = mock(LocacaoRepository.class);
        LocalDate inicio = LocalDate.of(2026, 10, 6);
        PeriodoLocacao periodo = new PeriodoLocacao(inicio, inicio.plusDays(3));
        ItemLocacao camera = new ItemLocacao(new CodigoEquipamento("CAM01"),
                "Câmera", new Dinheiro(new BigDecimal("100.00")));
        List<ItemLocacao> itens = List.of(camera);
        Locacao locacao = new Locacao("cliente-1", periodo, itens);
        UUID idOriginal = locacao.getId();
        assertThat(locacao.getEstado()).isEqualTo(EstadoLocacao.ABERTA);
        when(repository.buscarPorId(idOriginal)).thenReturn(Optional.of(locacao));
        ConfirmarRetiradaService service = new ConfirmarRetiradaService(repository);

        Locacao iniciada = service.confirmar(idOriginal, inicio);

        assertThat(iniciada).isSameAs(locacao);
        assertThat(iniciada.getId()).isEqualTo(idOriginal);
        assertThat(iniciada.getClienteId()).isEqualTo("cliente-1");
        assertThat(iniciada.getPeriodo()).isEqualTo(periodo);
        assertThat(iniciada.getItens()).containsExactlyElementsOf(itens);
        assertThat(iniciada.getEstado()).isEqualTo(EstadoLocacao.EM_ANDAMENTO);
        verify(repository).buscarPorId(idOriginal);
        verify(repository).salvar(iniciada);
    }
}
