package br.ifsp.locacao.structural;

import br.ifsp.locacao.domain.PeriodoLocacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

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
}
