package br.ifsp.locacao.structural;

import br.ifsp.locacao.domain.CodigoEquipamento;
import br.ifsp.locacao.domain.Dinheiro;
import br.ifsp.locacao.domain.ItemLocacao;
import br.ifsp.locacao.domain.Locacao;
import br.ifsp.locacao.domain.PeriodoLocacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("Structural")
@Tag("UnitTest")
class LocacaoStructuralTest {

    @Test
    @DisplayName("Rejeitar período nulo na criação mesmo com cliente e equipamento válidos")
    void rejeitaPeriodoNuloNaCriacaoComClienteEEquipamentoValidos() {
        ItemLocacao camera = new ItemLocacao(new CodigoEquipamento("CAM01"), "Câmera",
                new Dinheiro(new BigDecimal("100.00")));

        assertThatThrownBy(() -> new Locacao("cliente-1", null, List.of(camera)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Período é obrigatório");
    }

    @Test
    @DisplayName("Rejeitar cliente nulo na criação mesmo com período e equipamento válidos")
    void rejeitaClienteNuloNaCriacaoComPeriodoEEquipamentoValidos() {
        LocalDate inicio = LocalDate.of(2026, 11, 5);
        PeriodoLocacao periodo = new PeriodoLocacao(inicio, inicio.plusDays(3));
        ItemLocacao camera = new ItemLocacao(new CodigoEquipamento("CAM01"), "Câmera",
                new Dinheiro(new BigDecimal("100.00")));

        assertThatThrownBy(() -> new Locacao(null, periodo, List.of(camera)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cliente, período e relógio são obrigatórios");
    }

    @Test
    @DisplayName("Rejeitar relógio nulo na criação mesmo com cliente, período e equipamento válidos")
    void rejeitaRelogioNuloNaCriacaoComDemaisDadosValidos() {
        LocalDate inicio = LocalDate.of(2026, 11, 5);
        PeriodoLocacao periodo = new PeriodoLocacao(inicio, inicio.plusDays(3));
        ItemLocacao camera = new ItemLocacao(new CodigoEquipamento("CAM01"), "Câmera",
                new Dinheiro(new BigDecimal("100.00")));

        assertThatThrownBy(() -> new Locacao("cliente-1", periodo, List.of(camera), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cliente, período e relógio são obrigatórios");
    }

    @Test
    @DisplayName("Rejeitar lista de itens nula na criação mesmo com cliente e período válidos")
    void rejeitaListaDeItensNulaNaCriacaoComClienteEPeriodoValidos() {
        LocalDate inicio = LocalDate.of(2026, 11, 5);
        PeriodoLocacao periodo = new PeriodoLocacao(inicio, inicio.plusDays(3));

        assertThatThrownBy(() -> new Locacao("cliente-1", periodo, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Itens são obrigatórios");
    }
}
