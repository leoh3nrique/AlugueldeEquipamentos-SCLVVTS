package br.ifsp.locacao.structural;

import br.ifsp.locacao.domain.PeriodoLocacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("Structural")
@Tag("UnitTest")
class PeriodoLocacaoStructuralTest {

    @Test
    @DisplayName("Rejeitar data final nula mesmo quando a data inicial é válida")
    void rejeitaDataFinalNulaComInicioValido() {
        LocalDate inicio = LocalDate.of(2026, 11, 5);

        assertThatThrownBy(() -> new PeriodoLocacao(inicio, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Datas são obrigatórias");
    }

    @Test
    @DisplayName("Não considerar sobrepostos períodos quando o próximo começa no fim do anterior")
    void naoConsideraSobrepostosPeriodosAdjacentes() {
        LocalDate inicio = LocalDate.of(2026, 11, 5);
        LocalDate limite = inicio.plusDays(3);
        PeriodoLocacao anterior = new PeriodoLocacao(inicio, limite);
        PeriodoLocacao proximo = new PeriodoLocacao(limite, limite.plusDays(3));

        assertThat(anterior.sobrepoe(proximo)).isFalse();
    }

    @Test
    @DisplayName("Considerar sobrepostos períodos que compartilham um dia")
    void consideraSobrepostosPeriodosComUmDiaEmComum() {
        LocalDate inicio = LocalDate.of(2026, 11, 5);
        PeriodoLocacao primeiro = new PeriodoLocacao(inicio, inicio.plusDays(3));
        PeriodoLocacao segundo = new PeriodoLocacao(inicio.plusDays(2), inicio.plusDays(5));

        assertThat(primeiro.sobrepoe(segundo)).isTrue();
    }
}
