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
class AlterarLocacaoServiceTest {

    @Test
    @DisplayName("#18: rejeitar remoção do único equipamento de locação aberta")
    void deveRejeitarRemocaoDoUnicoEquipamento() {
        LocacaoRepository repository = mock(LocacaoRepository.class);
        PeriodoLocacao periodo = new PeriodoLocacao(
                LocalDate.of(2026, 10, 6), LocalDate.of(2026, 10, 9));
        ItemLocacao camera = new ItemLocacao(new CodigoEquipamento("CAM01"),
                "Câmera", new Dinheiro(new BigDecimal("100.00")));
        List<ItemLocacao> itensOriginais = List.of(camera);
        Locacao locacao = new Locacao("cliente-1", periodo, itensOriginais);
        UUID idOriginal = locacao.getId();
        when(repository.buscarPorId(idOriginal)).thenReturn(Optional.of(locacao));
        AlterarLocacaoService service = new AlterarLocacaoService(repository);

        assertThatThrownBy(() -> service.alterar(idOriginal, periodo, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Locação deve possuir pelo menos um equipamento");
        assertThat(locacao.getId()).isEqualTo(idOriginal);
        assertThat(locacao.getPeriodo()).isEqualTo(periodo);
        assertThat(locacao.getItens()).containsExactlyElementsOf(itensOriginais);
        assertThat(locacao.getEstado()).isEqualTo(EstadoLocacao.ABERTA);
        verify(repository, never()).salvar(any(Locacao.class));
    }

    @Test
    @DisplayName("#17: rejeitar adição de sexto equipamento à locação aberta")
    void deveRejeitarAdicaoDeSextoEquipamento() {
        LocacaoRepository repository = mock(LocacaoRepository.class);
        PeriodoLocacao periodo = new PeriodoLocacao(
                LocalDate.of(2026, 10, 6), LocalDate.of(2026, 10, 9));
        List<ItemLocacao> seisItens = java.util.stream.IntStream.rangeClosed(1, 6)
                .mapToObj(numero -> new ItemLocacao(
                        new CodigoEquipamento("EQ" + numero), "Equipamento " + numero,
                        new Dinheiro(new BigDecimal("100.00"))))
                .toList();
        List<ItemLocacao> itensOriginais = seisItens.subList(0, 5);
        Locacao locacao = new Locacao("cliente-1", periodo, itensOriginais);
        UUID idOriginal = locacao.getId();
        when(repository.buscarPorId(idOriginal)).thenReturn(Optional.of(locacao));
        AlterarLocacaoService service = new AlterarLocacaoService(repository);

        assertThatThrownBy(() -> service.alterar(idOriginal, periodo, seisItens))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Locação não pode possuir mais de cinco equipamentos");
        assertThat(locacao.getId()).isEqualTo(idOriginal);
        assertThat(locacao.getPeriodo()).isEqualTo(periodo);
        assertThat(locacao.getItens()).containsExactlyElementsOf(itensOriginais);
        assertThat(locacao.getEstado()).isEqualTo(EstadoLocacao.ABERTA);
        verify(repository, never()).salvar(any(Locacao.class));
    }

    @Test
    @DisplayName("#16: rejeitar alteração para período superior a 30 dias")
    void deveRejeitarAlteracaoParaPeriodoSuperiorATrintaDias() {
        LocacaoRepository repository = mock(LocacaoRepository.class);
        PeriodoLocacao periodoOriginal = new PeriodoLocacao(
                LocalDate.of(2026, 10, 6), LocalDate.of(2026, 10, 9));
        ItemLocacao camera = new ItemLocacao(new CodigoEquipamento("CAM01"),
                "Câmera", new Dinheiro(new BigDecimal("100.00")));
        List<ItemLocacao> itensOriginais = List.of(camera);
        Locacao locacao = new Locacao("cliente-1", periodoOriginal, itensOriginais);
        AlterarLocacaoService service = new AlterarLocacaoService(repository);
        LocalDate novoInicio = LocalDate.of(2026, 10, 7);

        assertThatThrownBy(() -> service.alterar(locacao.getId(),
                new PeriodoLocacao(novoInicio, novoInicio.plusDays(31)), itensOriginais))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Período não pode ultrapassar 30 dias");
        assertThat(locacao.getPeriodo()).isEqualTo(periodoOriginal);
        assertThat(locacao.getItens()).containsExactlyElementsOf(itensOriginais);
        assertThat(locacao.getEstado()).isEqualTo(EstadoLocacao.ABERTA);
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("#15: rejeitar alteração quando equipamento está reservado no novo período")
    void deveRejeitarAlteracaoComEquipamentoReservadoNoNovoPeriodo() {
        LocacaoRepository repository = mock(LocacaoRepository.class);
        PeriodoLocacao periodoOriginal = new PeriodoLocacao(
                LocalDate.of(2026, 10, 6), LocalDate.of(2026, 10, 9));
        ItemLocacao camera = new ItemLocacao(new CodigoEquipamento("CAM01"),
                "Câmera", new Dinheiro(new BigDecimal("100.00")));
        ItemLocacao projetor = new ItemLocacao(new CodigoEquipamento("PROJ01"),
                "Projetor", new Dinheiro(new BigDecimal("50.00")));
        List<ItemLocacao> itensOriginais = List.of(camera, projetor);
        Locacao locacao = new Locacao("cliente-1", periodoOriginal, itensOriginais);
        UUID idOriginal = locacao.getId();
        PeriodoLocacao novoPeriodo = new PeriodoLocacao(
                LocalDate.of(2026, 10, 8), LocalDate.of(2026, 10, 12));
        when(repository.buscarPorId(idOriginal)).thenReturn(Optional.of(locacao));
        when(repository.estaReservado(projetor.codigo(), novoPeriodo, idOriginal))
                .thenReturn(true);
        AlterarLocacaoService service = new AlterarLocacaoService(repository);

        assertThatThrownBy(() -> service.alterar(idOriginal, novoPeriodo, itensOriginais))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Equipamento indisponível no período");
        assertThat(locacao.getId()).isEqualTo(idOriginal);
        assertThat(locacao.getPeriodo()).isEqualTo(periodoOriginal);
        assertThat(locacao.getItens()).containsExactlyElementsOf(itensOriginais);
        assertThat(locacao.getEstado()).isEqualTo(EstadoLocacao.ABERTA);
        verify(repository).estaReservado(projetor.codigo(), novoPeriodo, idOriginal);
        verify(repository, never()).salvar(any(Locacao.class));
    }

    @Test
    @DisplayName("#14: rejeitar alteração de período e equipamentos de locação em andamento")
    void deveRejeitarAlteracaoDeLocacaoEmAndamento() {
        LocacaoRepository repository = mock(LocacaoRepository.class);
        PeriodoLocacao periodoOriginal = new PeriodoLocacao(
                LocalDate.of(2026, 10, 6), LocalDate.of(2026, 10, 9));
        ItemLocacao camera = new ItemLocacao(new CodigoEquipamento("CAM01"),
                "Câmera", new Dinheiro(new BigDecimal("100.00")));
        List<ItemLocacao> itensOriginais = List.of(camera);
        Locacao locacao = new Locacao("cliente-1", periodoOriginal, itensOriginais);
        locacao.confirmarRetirada(periodoOriginal.inicio());
        assertThat(locacao.getEstado()).isEqualTo(EstadoLocacao.EM_ANDAMENTO);
        when(repository.buscarPorId(locacao.getId())).thenReturn(Optional.of(locacao));
        AlterarLocacaoService service = new AlterarLocacaoService(repository);
        PeriodoLocacao novoPeriodo = new PeriodoLocacao(
                LocalDate.of(2026, 10, 7), LocalDate.of(2026, 10, 11));
        ItemLocacao projetor = new ItemLocacao(new CodigoEquipamento("PROJ01"),
                "Projetor", new Dinheiro(new BigDecimal("50.00")));

        assertThatThrownBy(() -> service.alterar(locacao.getId(), novoPeriodo, List.of(projetor)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Somente locações abertas podem ser alteradas");
        assertThat(locacao.getPeriodo()).isEqualTo(periodoOriginal);
        assertThat(locacao.getItens()).containsExactlyElementsOf(itensOriginais);
        assertThat(locacao.getEstado()).isEqualTo(EstadoLocacao.EM_ANDAMENTO);
        verify(repository, never()).salvar(any(Locacao.class));
    }

    @Test
    @DisplayName("#13: alterar período e equipamentos de uma locação aberta")
    void deveAlterarPeriodoEEquipamentosDeLocacaoAberta() {
        LocacaoRepository repository = mock(LocacaoRepository.class);
        PeriodoLocacao periodoOriginal = new PeriodoLocacao(
                LocalDate.of(2026, 10, 6), LocalDate.of(2026, 10, 9));
        ItemLocacao camera = new ItemLocacao(new CodigoEquipamento("CAM01"),
                "Câmera", new Dinheiro(new BigDecimal("100.00")));
        Locacao locacao = new Locacao("cliente-1", periodoOriginal, List.of(camera));
        UUID idOriginal = locacao.getId();
        when(repository.buscarPorId(idOriginal)).thenReturn(Optional.of(locacao));
        AlterarLocacaoService service = new AlterarLocacaoService(repository);
        PeriodoLocacao novoPeriodo = new PeriodoLocacao(
                LocalDate.of(2026, 10, 7), LocalDate.of(2026, 10, 11));
        ItemLocacao projetor = new ItemLocacao(new CodigoEquipamento("PROJ01"),
                "Projetor", new Dinheiro(new BigDecimal("50.00")));
        List<ItemLocacao> novosItens = List.of(camera, projetor);

        Locacao atualizada = service.alterar(idOriginal, novoPeriodo, novosItens);

        assertThat(atualizada).isSameAs(locacao);
        assertThat(atualizada.getId()).isEqualTo(idOriginal);
        assertThat(atualizada.getClienteId()).isEqualTo("cliente-1");
        assertThat(atualizada.getPeriodo()).isEqualTo(novoPeriodo);
        assertThat(atualizada.getItens()).containsExactlyElementsOf(novosItens);
        assertThat(atualizada.getEstado()).isEqualTo(EstadoLocacao.ABERTA);
        verify(repository).buscarPorId(idOriginal);
        for (ItemLocacao item : novosItens) {
            verify(repository).estaReservado(item.codigo(), novoPeriodo, idOriginal);
        }
        verify(repository).salvar(atualizada);
    }
}
