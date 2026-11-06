package br.ifsp.locacao.mutation;

import br.ifsp.locacao.application.ConsultarLocacoesService;
import br.ifsp.locacao.domain.CodigoEquipamento;
import br.ifsp.locacao.domain.Dinheiro;
import br.ifsp.locacao.domain.EstadoLocacao;
import br.ifsp.locacao.domain.ItemLocacao;
import br.ifsp.locacao.domain.Locacao;
import br.ifsp.locacao.domain.LocacaoRepository;
import br.ifsp.locacao.domain.PeriodoLocacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("Mutation")
@Tag("UnitTest")
class ConsultarLocacoesServiceMutationTest {

    @Test
    @DisplayName("Buscar locação pelo ID retorna os dados da reserva")
    void retornaLocacaoEncontradaPeloId() {
        LocacaoRepository repository = mock(LocacaoRepository.class);
        PeriodoLocacao periodo = new PeriodoLocacao(
                LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 13));
        ItemLocacao camera = new ItemLocacao(new CodigoEquipamento("CAM01"),
                "Câmera", new Dinheiro(new BigDecimal("100.00")));
        Locacao reserva = new Locacao("cliente-1", periodo, List.of(camera));
        when(repository.buscarPorId(reserva.getId())).thenReturn(Optional.of(reserva));
        ConsultarLocacoesService service = new ConsultarLocacoesService(repository);

        Locacao resultado = service.buscarPorId(reserva.getId());

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(reserva.getId());
        assertThat(resultado.getClienteId()).isEqualTo("cliente-1");
        assertThat(resultado.getPeriodo()).isEqualTo(periodo);
        assertThat(resultado.getEstado()).isEqualTo(EstadoLocacao.ABERTA);
        assertThat(resultado.getItens()).containsExactly(camera);
        assertThat(resultado.getValorTotal().valor()).isEqualByComparingTo("300.00");
        verify(repository).buscarPorId(reserva.getId());
    }
}
