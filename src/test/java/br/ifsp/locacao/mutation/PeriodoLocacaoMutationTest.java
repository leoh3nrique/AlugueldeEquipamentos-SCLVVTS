package br.ifsp.locacao.mutation;

import br.ifsp.locacao.domain.PeriodoLocacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("Mutation")
@Tag("UnitTest")
class PeriodoLocacaoMutationTest {

    @Test
    @DisplayName("Hash do período mantém a igualdade e considera as duas datas")
    void consideraAsDuasDatasNoHashDoPeriodo() {
        LocalDate inicio = LocalDate.of(2026, 11, 10);
        LocalDate fim = LocalDate.of(2026, 11, 13);
        PeriodoLocacao periodo = new PeriodoLocacao(inicio, fim);
        PeriodoLocacao mesmasDatas = new PeriodoLocacao(inicio, fim);
        PeriodoLocacao outroInicio = new PeriodoLocacao(inicio.plusDays(1), fim);
        PeriodoLocacao outroFim = new PeriodoLocacao(inicio, fim.plusDays(1));

        assertThat(periodo).isEqualTo(mesmasDatas);
        assertThat(periodo.hashCode()).isEqualTo(mesmasDatas.hashCode())
                .isNotEqualTo(outroInicio.hashCode())
                .isNotEqualTo(outroFim.hashCode());
    }
}
