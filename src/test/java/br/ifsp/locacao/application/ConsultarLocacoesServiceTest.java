package br.ifsp.locacao.application;

import br.ifsp.locacao.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Tag("UnitTest")
@Tag("TDD")
class ConsultarLocacoesServiceTest {

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
