package br.ifsp.locacao.structural;

import br.ifsp.locacao.domain.CodigoEquipamento;
import br.ifsp.locacao.domain.Dinheiro;
import br.ifsp.locacao.domain.ItemLocacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    @DisplayName("Rejeitar descrição nula mesmo com código e diária válidos")
    void rejeitaDescricaoNulaComCodigoEDiariaValidos() {
        CodigoEquipamento codigo = new CodigoEquipamento("CAM01");
        Dinheiro diaria = new Dinheiro(new BigDecimal("100.00"));

        assertThatThrownBy(() -> new ItemLocacao(codigo, null, diaria))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Código e descrição são obrigatórios");
    }

    @Test
    @DisplayName("Considerar diferentes itens com códigos distintos mesmo com descrição e diária iguais")
    void naoConsideraIguaisItensComCodigosDiferentes() {
        Dinheiro diaria = new Dinheiro(new BigDecimal("100.00"));
        ItemLocacao primeiro = new ItemLocacao(new CodigoEquipamento("CAM01"), "Câmera", diaria);
        ItemLocacao segundo = new ItemLocacao(new CodigoEquipamento("CAM02"), "Câmera", diaria);

        assertThat(primeiro.equals(segundo)).isFalse();
    }

    @Test
    @DisplayName("Considerar um item de locação diferente de um objeto de outro tipo")
    void naoConsideraItemIgualAObjetoDeOutroTipo() {
        ItemLocacao item = new ItemLocacao(new CodigoEquipamento("CAM01"), "Câmera",
                new Dinheiro(new BigDecimal("100.00")));
        Object outro = new Object();

        assertThat(item.equals(outro)).isFalse();
    }
}
