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
class RenovarLocacaoServiceTest {

    @Test
    @DisplayName("#31: rejeitar renovação com acréscimo de oito dias")
    void deveRejeitarRenovacaoComOitoDiasAdicionais() {
        LocacaoRepository repository = mock(LocacaoRepository.class);
        LocalDate inicio = LocalDate.of(2026, 10, 6);
        PeriodoLocacao periodoOriginal = new PeriodoLocacao(inicio, inicio.plusDays(3));
        ItemLocacao camera = new ItemLocacao(new CodigoEquipamento("CAM01"),
                "Câmera", new Dinheiro(new BigDecimal("100.00")));
        List<ItemLocacao> itens = List.of(camera);
        Locacao locacao = new Locacao("cliente-1", periodoOriginal, itens);
        locacao.confirmarRetirada(inicio);
        UUID idOriginal = locacao.getId();
        when(repository.buscarPorId(idOriginal)).thenReturn(Optional.of(locacao));
        RenovarLocacaoService service = new RenovarLocacaoService(repository);

        assertThatThrownBy(() -> service.renovar(idOriginal, 8, inicio.plusDays(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Renovação não pode acrescentar mais de sete dias");
        assertThat(locacao.getId()).isEqualTo(idOriginal);
        assertThat(locacao.getPeriodo()).isEqualTo(periodoOriginal);
        assertThat(locacao.getQuantidadeRenovacoes()).isZero();
        assertThat(locacao.getEstado()).isEqualTo(EstadoLocacao.EM_ANDAMENTO);
        assertThat(locacao.getItens()).containsExactlyElementsOf(itens);
        verify(repository, never()).salvar(any(Locacao.class));
    }

    @Test
    @DisplayName("#30: rejeitar terceira renovação de uma locação")
    void deveRejeitarTerceiraRenovacao() {
        LocacaoRepository repository = mock(LocacaoRepository.class);
        LocalDate inicio = LocalDate.of(2026, 10, 6);
        PeriodoLocacao periodoOriginal = new PeriodoLocacao(inicio, inicio.plusDays(3));
        ItemLocacao camera = new ItemLocacao(new CodigoEquipamento("CAM01"),
                "Câmera", new Dinheiro(new BigDecimal("100.00")));
        List<ItemLocacao> itens = List.of(camera);
        Locacao locacao = new Locacao("cliente-1", periodoOriginal, itens);
        locacao.confirmarRetirada(inicio);
        locacao.renovar(1, inicio.plusDays(1));
        locacao.renovar(1, inicio.plusDays(2));
        assertThat(locacao.getQuantidadeRenovacoes()).isEqualTo(2);
        PeriodoLocacao periodoAposDuasRenovacoes = locacao.getPeriodo();
        assertThat(periodoAposDuasRenovacoes.fim()).isEqualTo(periodoOriginal.fim().plusDays(2));
        UUID idOriginal = locacao.getId();
        when(repository.buscarPorId(idOriginal)).thenReturn(Optional.of(locacao));
        RenovarLocacaoService service = new RenovarLocacaoService(repository);

        assertThatThrownBy(() -> service.renovar(idOriginal, 1, inicio.plusDays(3)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Locação não pode ser renovada mais de duas vezes");
        assertThat(locacao.getId()).isEqualTo(idOriginal);
        assertThat(locacao.getPeriodo()).isEqualTo(periodoAposDuasRenovacoes);
        assertThat(locacao.getQuantidadeRenovacoes()).isEqualTo(2);
        assertThat(locacao.getEstado()).isEqualTo(EstadoLocacao.EM_ANDAMENTO);
        assertThat(locacao.getItens()).containsExactlyElementsOf(itens);
        verify(repository, never()).salvar(any(Locacao.class));
    }

    @Test
    @DisplayName("#29: rejeitar renovação de locação ainda aberta")
    void deveRejeitarRenovacaoDeLocacaoAberta() {
        LocacaoRepository repository = mock(LocacaoRepository.class);
        LocalDate inicio = LocalDate.of(2026, 10, 6);
        PeriodoLocacao periodoOriginal = new PeriodoLocacao(inicio, inicio.plusDays(3));
        ItemLocacao camera = new ItemLocacao(new CodigoEquipamento("CAM01"),
                "Câmera", new Dinheiro(new BigDecimal("100.00")));
        List<ItemLocacao> itens = List.of(camera);
        Locacao locacao = new Locacao("cliente-1", periodoOriginal, itens);
        UUID idOriginal = locacao.getId();
        assertThat(locacao.getEstado()).isEqualTo(EstadoLocacao.ABERTA);
        when(repository.buscarPorId(idOriginal)).thenReturn(Optional.of(locacao));
        RenovarLocacaoService service = new RenovarLocacaoService(repository);

        assertThatThrownBy(() -> service.renovar(idOriginal, 3, inicio.plusDays(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Somente locações em andamento ou parcialmente devolvidas podem ser renovadas");
        assertThat(locacao.getId()).isEqualTo(idOriginal);
        assertThat(locacao.getPeriodo()).isEqualTo(periodoOriginal);
        assertThat(locacao.getQuantidadeRenovacoes()).isZero();
        assertThat(locacao.getEstado()).isEqualTo(EstadoLocacao.ABERTA);
        assertThat(locacao.getItens()).containsExactlyElementsOf(itens);
        verify(repository, never()).salvar(any(Locacao.class));
    }

    @Test
    @DisplayName("#28: rejeitar renovação solicitada após o término do período")
    void deveRejeitarRenovacaoAposTerminoDoPeriodo() {
        LocacaoRepository repository = mock(LocacaoRepository.class);
        LocalDate inicio = LocalDate.of(2026, 10, 6);
        PeriodoLocacao periodoOriginal = new PeriodoLocacao(inicio, inicio.plusDays(3));
        ItemLocacao camera = new ItemLocacao(new CodigoEquipamento("CAM01"),
                "Câmera", new Dinheiro(new BigDecimal("100.00")));
        List<ItemLocacao> itens = List.of(camera);
        Locacao locacao = new Locacao("cliente-1", periodoOriginal, itens);
        locacao.confirmarRetirada(inicio);
        UUID idOriginal = locacao.getId();
        when(repository.buscarPorId(idOriginal)).thenReturn(Optional.of(locacao));
        RenovarLocacaoService service = new RenovarLocacaoService(repository);

        assertThatThrownBy(() -> service.renovar(idOriginal, 3,
                periodoOriginal.fim().plusDays(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Renovação deve ser solicitada antes da data final");
        assertThat(locacao.getId()).isEqualTo(idOriginal);
        assertThat(locacao.getPeriodo()).isEqualTo(periodoOriginal);
        assertThat(locacao.getQuantidadeRenovacoes()).isZero();
        assertThat(locacao.getEstado()).isEqualTo(EstadoLocacao.EM_ANDAMENTO);
        assertThat(locacao.getItens()).containsExactlyElementsOf(itens);
        verify(repository, never()).salvar(any(Locacao.class));
    }

    @Test
    @DisplayName("#27: renovar somente equipamentos pendentes após devolução parcial")
    void deveRenovarSomenteEquipamentosAindaNaoDevolvidos() {
        LocacaoRepository repository = mock(LocacaoRepository.class);
        LocalDate inicio = LocalDate.of(2026, 10, 6);
        PeriodoLocacao periodoOriginal = new PeriodoLocacao(inicio, inicio.plusDays(3));
        CodigoEquipamento codigoCamera = new CodigoEquipamento("CAM01");
        CodigoEquipamento codigoProjetor = new CodigoEquipamento("PROJ01");
        ItemLocacao camera = new ItemLocacao(codigoCamera,
                "Câmera", new Dinheiro(new BigDecimal("100.00")));
        ItemLocacao projetor = new ItemLocacao(codigoProjetor,
                "Projetor", new Dinheiro(new BigDecimal("50.00")));
        Locacao locacao = new Locacao("cliente-1", periodoOriginal, List.of(camera, projetor));
        locacao.confirmarRetirada(inicio);
        locacao.registrarDevolucao(List.of(codigoCamera), inicio.plusDays(1));
        UUID idOriginal = locacao.getId();
        assertThat(locacao.getEstado()).isEqualTo(EstadoLocacao.PARCIALMENTE_DEVOLVIDA);
        assertThat(locacao.estaDevolvido(codigoCamera)).isTrue();
        assertThat(locacao.estaDevolvido(codigoProjetor)).isFalse();
        when(repository.buscarPorId(idOriginal)).thenReturn(Optional.of(locacao));
        RenovarLocacaoService service = new RenovarLocacaoService(repository);
        PeriodoLocacao periodoAdicional = new PeriodoLocacao(
                periodoOriginal.fim(), periodoOriginal.fim().plusDays(3));

        Locacao renovada = service.renovar(idOriginal, 3, inicio.plusDays(2));

        assertThat(renovada).isSameAs(locacao);
        assertThat(renovada.getId()).isEqualTo(idOriginal);
        assertThat(renovada.getClienteId()).isEqualTo("cliente-1");
        assertThat(renovada.getEstado()).isEqualTo(EstadoLocacao.PARCIALMENTE_DEVOLVIDA);
        assertThat(renovada.getPeriodo().fim()).isEqualTo(periodoOriginal.fim().plusDays(3));
        assertThat(renovada.getQuantidadeRenovacoes()).isEqualTo(1);
        assertThat(renovada.getFimContratado(codigoCamera)).isEqualTo(periodoOriginal.fim());
        assertThat(renovada.getFimContratado(codigoProjetor))
                .isEqualTo(periodoOriginal.fim().plusDays(3));
        assertThat(renovada.estaDevolvido(codigoCamera)).isTrue();
        assertThat(renovada.estaDevolvido(codigoProjetor)).isFalse();
        assertThat(renovada.getItens()).containsExactly(camera, projetor);
        verify(repository).estaReservado(codigoProjetor, periodoAdicional, idOriginal);
        verify(repository, never()).estaReservado(eq(codigoCamera),
                any(PeriodoLocacao.class), eq(idOriginal));
        verify(repository).salvar(renovada);
    }

    @Test
    @DisplayName("#26: renovar locação em andamento por três dias antes do término")
    void deveRenovarLocacaoEmAndamentoPorTresDias() {
        LocacaoRepository repository = mock(LocacaoRepository.class);
        LocalDate inicio = LocalDate.of(2026, 10, 6);
        PeriodoLocacao periodoOriginal = new PeriodoLocacao(inicio, inicio.plusDays(3));
        ItemLocacao camera = new ItemLocacao(new CodigoEquipamento("CAM01"),
                "Câmera", new Dinheiro(new BigDecimal("100.00")));
        List<ItemLocacao> itens = List.of(camera);
        Locacao locacao = new Locacao("cliente-1", periodoOriginal, itens);
        locacao.confirmarRetirada(inicio);
        UUID idOriginal = locacao.getId();
        assertThat(locacao.getEstado()).isEqualTo(EstadoLocacao.EM_ANDAMENTO);
        assertThat(locacao.getQuantidadeRenovacoes()).isZero();
        when(repository.buscarPorId(idOriginal)).thenReturn(Optional.of(locacao));
        RenovarLocacaoService service = new RenovarLocacaoService(repository);
        PeriodoLocacao periodoAdicional = new PeriodoLocacao(
                periodoOriginal.fim(), periodoOriginal.fim().plusDays(3));

        Locacao renovada = service.renovar(idOriginal, 3, inicio.plusDays(1));

        assertThat(renovada).isSameAs(locacao);
        assertThat(renovada.getId()).isEqualTo(idOriginal);
        assertThat(renovada.getClienteId()).isEqualTo("cliente-1");
        assertThat(renovada.getPeriodo().inicio()).isEqualTo(inicio);
        assertThat(renovada.getPeriodo().fim()).isEqualTo(periodoOriginal.fim().plusDays(3));
        assertThat(renovada.getQuantidadeRenovacoes()).isEqualTo(1);
        assertThat(renovada.getEstado()).isEqualTo(EstadoLocacao.EM_ANDAMENTO);
        assertThat(renovada.getItens()).containsExactlyElementsOf(itens);
        verify(repository).buscarPorId(idOriginal);
        verify(repository).estaReservado(camera.codigo(), periodoAdicional, idOriginal);
        verify(repository).salvar(renovada);
    }
}
