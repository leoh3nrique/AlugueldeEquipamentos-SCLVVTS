package br.ifsp.locacao.mutation;

import br.ifsp.locacao.domain.CodigoEquipamento;
import br.ifsp.locacao.domain.Dinheiro;
import br.ifsp.locacao.domain.ItemLocacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("Mutation")
@Tag("UnitTest")
class ItemLocacaoMutationTest {

    @Test
    @DisplayName("Hash do item considera o código e preserva a igualdade com outros dados")
    void consideraCodigoNoHashSemDependerDeDescricaoEDiaria() {
        ItemLocacao item = new ItemLocacao(new CodigoEquipamento("CAM01"), "Câmera",
                new Dinheiro(new BigDecimal("100.00")));
        ItemLocacao mesmoCodigo = new ItemLocacao(new CodigoEquipamento("CAM01"), "Câmera profissional",
                new Dinheiro(new BigDecimal("200.00")));
        ItemLocacao outroCodigo = new ItemLocacao(new CodigoEquipamento("CAM02"), "Câmera",
                new Dinheiro(new BigDecimal("100.00")));

        assertThat(item).isEqualTo(mesmoCodigo).isNotEqualTo(outroCodigo);
        assertThat(item.hashCode()).isEqualTo(mesmoCodigo.hashCode())
                .isNotEqualTo(outroCodigo.hashCode());
    }
}
