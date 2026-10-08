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
class CancelarLocacaoServiceTest {

    @Test
    @DisplayName("#44: cancelar locação aberta e liberar seus equipamentos")
    void deveCancelarLocacaoAbertaELiberarEquipamentos() {
        LocacaoRepository repository = mock(LocacaoRepository.class);
        PeriodoLocacao periodo = new PeriodoLocacao(
                LocalDate.of(2026, 10, 6), LocalDate.of(2026, 10, 9));
        CodigoEquipamento codigo = new CodigoEquipamento("CAM01");
        ItemLocacao camera = new ItemLocacao(codigo,
                "Câmera", new Dinheiro(new BigDecimal("100.00")));
        Locacao locacao = new Locacao("cliente-1", periodo, List.of(camera));
        UUID idOriginal = locacao.getId();
        assertThat(locacao.getEstado()).isEqualTo(EstadoLocacao.ABERTA);
        assertThat(locacao.bloqueiaReserva(codigo)).isTrue();
        when(repository.buscarPorId(idOriginal)).thenReturn(Optional.of(locacao));
        CancelarLocacaoService service = new CancelarLocacaoService(repository);

        Locacao cancelada = service.cancelar(idOriginal);

        assertThat(cancelada).isSameAs(locacao);
        assertThat(cancelada.getId()).isEqualTo(idOriginal);
        assertThat(cancelada.getClienteId()).isEqualTo("cliente-1");
        assertThat(cancelada.getPeriodo()).isEqualTo(periodo);
        assertThat(cancelada.getItens()).containsExactly(camera);
        assertThat(cancelada.getEstado()).isEqualTo(EstadoLocacao.CANCELADA);
        assertThat(cancelada.bloqueiaReserva(codigo)).isFalse();
        verify(repository).buscarPorId(idOriginal);
        verify(repository).salvar(cancelada);
    }
}
