package br.ifsp.locacao.structural;

import br.ifsp.locacao.domain.Dinheiro;
import br.ifsp.locacao.domain.ItemLocacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("Structural")
@Tag("UnitTest")
class ItemLocacaoStructuralTest {

    @Test
    @DisplayName("Rejeitar código nulo mesmo com descrição e diária válidas")
    void rejeitaCodigoNuloComDescricaoEDiariaValidas() {
        Dinheiro diaria = new Dinheiro(new BigDecimal("100.00"));

        assertThatThrownBy(() -> new ItemLocacao(null, "Câmera", diaria))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Código e descrição são obrigatórios");
    }
}
