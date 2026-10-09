package br.ifsp.locacao.application;

import br.ifsp.locacao.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Tag("UnitTest")
@Tag("TDD")
class ConsultarLocacoesServiceTest {

    @Test
    @DisplayName("#52: ordenar locações do cliente da criação mais recente para a mais antiga")
    void deveOrdenarLocacoesDaMaisRecenteParaAMaisAntiga() {
        LocacaoRepository repository = mock(LocacaoRepository.class);
        PeriodoLocacao periodo = new PeriodoLocacao(
                LocalDate.of(2026, 10, 6), LocalDate.of(2026, 10, 9));
        ItemLocacao camera = new ItemLocacao(new CodigoEquipamento("CAM01"),
                "Câmera", new Dinheiro(new BigDecimal("100.00")));
        Instant dataAntiga = Instant.parse("2026-10-01T10:00:00Z");
        Instant dataIntermediaria = Instant.parse("2026-10-02T10:00:00Z");
        Instant dataRecente = Instant.parse("2026-10-03T10:00:00Z");
        Locacao antiga = new Locacao("cliente-1", periodo, List.of(camera),
                Clock.fixed(dataAntiga, ZoneOffset.UTC));
        Locacao intermediaria = new Locacao("cliente-1", periodo, List.of(camera),
                Clock.fixed(dataIntermediaria, ZoneOffset.UTC));
        Locacao recente = new Locacao("cliente-1", periodo, List.of(camera),
                Clock.fixed(dataRecente, ZoneOffset.UTC));
        Locacao outroCliente = new Locacao("cliente-2", periodo, List.of(camera),
                Clock.fixed(dataRecente.plusSeconds(60), ZoneOffset.UTC));
        when(repository.listar()).thenReturn(List.of(intermediaria, outroCliente, antiga, recente));
        ConsultarLocacoesService service = new ConsultarLocacoesService(repository);

        List<Locacao> resultado = service.consultarPorCliente("cliente-1");

        assertThat(resultado).containsExactly(recente, intermediaria, antiga);
        assertThat(resultado).extracting(Locacao::getCriadaEm)
                .containsExactly(dataRecente, dataIntermediaria, dataAntiga);
        verify(repository).listar();
        verify(repository, never()).salvar(any(Locacao.class));
    }

    @Test
    @DisplayName("#51: filtrar locações do cliente pelo estado EM_ANDAMENTO")
    void deveConsultarSomenteLocacoesEmAndamentoDoCliente() {
        LocacaoRepository repository = mock(LocacaoRepository.class);
        LocalDate inicio = LocalDate.of(2026, 10, 6);
        PeriodoLocacao periodo = new PeriodoLocacao(inicio, inicio.plusDays(3));
        ItemLocacao camera = new ItemLocacao(new CodigoEquipamento("CAM01"),
                "Câmera", new Dinheiro(new BigDecimal("100.00")));
        Locacao aberta = new Locacao("cliente-1", periodo, List.of(camera));
        Locacao emAndamento = new Locacao("cliente-1", periodo, List.of(camera));
        emAndamento.confirmarRetirada(inicio);
        Locacao cancelada = new Locacao("cliente-1", periodo, List.of(camera));
        cancelada.cancelar();
        Locacao outroCliente = new Locacao("cliente-2", periodo, List.of(camera));
        outroCliente.confirmarRetirada(inicio);
        when(repository.listar()).thenReturn(List.of(aberta, outroCliente, emAndamento, cancelada));
        ConsultarLocacoesService service = new ConsultarLocacoesService(repository);

        List<Locacao> resultado = service.consultarPorCliente("cliente-1", EstadoLocacao.EM_ANDAMENTO);

        assertThat(resultado).containsExactly(emAndamento);
        assertThat(resultado).allSatisfy(locacao -> {
            assertThat(locacao.getClienteId()).isEqualTo("cliente-1");
            assertThat(locacao.getEstado()).isEqualTo(EstadoLocacao.EM_ANDAMENTO);
        });
        assertThat(aberta.getEstado()).isEqualTo(EstadoLocacao.ABERTA);
        assertThat(cancelada.getEstado()).isEqualTo(EstadoLocacao.CANCELADA);
        assertThat(outroCliente.getEstado()).isEqualTo(EstadoLocacao.EM_ANDAMENTO);
        verify(repository).listar();
        verify(repository, never()).salvar(any(Locacao.class));
    }

    @Test
    @DisplayName("#50: retornar lista vazia para cliente sem locações")
    void deveRetornarListaVaziaParaClienteSemLocacoes() {
        LocacaoRepository repository = mock(LocacaoRepository.class);
        PeriodoLocacao periodo = new PeriodoLocacao(
                LocalDate.of(2026, 10, 6), LocalDate.of(2026, 10, 9));
        ItemLocacao camera = new ItemLocacao(new CodigoEquipamento("CAM01"),
                "Câmera", new Dinheiro(new BigDecimal("100.00")));
        Locacao outroCliente = new Locacao("cliente-2", periodo, List.of(camera));
        when(repository.listar()).thenReturn(List.of(outroCliente));
        ConsultarLocacoesService service = new ConsultarLocacoesService(repository);

        List<Locacao> resultado = service.consultarPorCliente("cliente-1");

        assertThat(resultado).isNotNull().isEmpty();
        assertThat(outroCliente.getClienteId()).isEqualTo("cliente-2");
        assertThat(outroCliente.getEstado()).isEqualTo(EstadoLocacao.ABERTA);
        verify(repository).listar();
        verify(repository, never()).salvar(any(Locacao.class));
    }

    @Test
    @DisplayName("#49: consultar somente as locações do cliente com períodos, estados e valores")
    void deveConsultarSomenteLocacoesDoClienteInformado() {
        LocacaoRepository repository = mock(LocacaoRepository.class);
        LocalDate inicio = LocalDate.of(2026, 10, 6);
        PeriodoLocacao periodoCamera = new PeriodoLocacao(inicio, inicio.plusDays(3));
        PeriodoLocacao periodoProjetor = new PeriodoLocacao(inicio, inicio.plusDays(2));
        ItemLocacao camera = new ItemLocacao(new CodigoEquipamento("CAM01"),
                "Câmera", new Dinheiro(new BigDecimal("100.00")));
        ItemLocacao projetor = new ItemLocacao(new CodigoEquipamento("PROJ01"),
                "Projetor", new Dinheiro(new BigDecimal("50.00")));
        Locacao aberta = new Locacao("cliente-1", periodoCamera, List.of(camera));
        Locacao emAndamento = new Locacao("cliente-1", periodoProjetor, List.of(projetor));
        emAndamento.confirmarRetirada(inicio);
        Locacao outroCliente = new Locacao("cliente-2", periodoCamera, List.of(camera));
        when(repository.listar()).thenReturn(List.of(aberta, outroCliente, emAndamento));
        ConsultarLocacoesService service = new ConsultarLocacoesService(repository);

        List<Locacao> resultado = service.consultarPorCliente("cliente-1");

        assertThat(resultado).containsExactlyInAnyOrder(aberta, emAndamento);
        assertThat(resultado).doesNotContain(outroCliente);
        assertThat(resultado).allSatisfy(locacao ->
                assertThat(locacao.getClienteId()).isEqualTo("cliente-1"));
        Locacao cameraConsultada = resultado.stream()
                .filter(locacao -> locacao.getId().equals(aberta.getId())).findFirst().orElseThrow();
        assertThat(cameraConsultada.getPeriodo()).isEqualTo(periodoCamera);
        assertThat(cameraConsultada.getEstado()).isEqualTo(EstadoLocacao.ABERTA);
        assertThat(cameraConsultada.getValorTotal().valor()).isEqualByComparingTo("300.00");
        Locacao projetorConsultado = resultado.stream()
                .filter(locacao -> locacao.getId().equals(emAndamento.getId())).findFirst().orElseThrow();
        assertThat(projetorConsultado.getPeriodo()).isEqualTo(periodoProjetor);
        assertThat(projetorConsultado.getEstado()).isEqualTo(EstadoLocacao.EM_ANDAMENTO);
        assertThat(projetorConsultado.getValorTotal().valor()).isEqualByComparingTo("100.00");
        verify(repository).listar();
        verify(repository, never()).salvar(any(Locacao.class));
    }
}
