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
import static org.mockito.Mockito.*;

@Tag("UnitTest")
@Tag("TDD")
class AlterarLocacaoServiceTest {

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
