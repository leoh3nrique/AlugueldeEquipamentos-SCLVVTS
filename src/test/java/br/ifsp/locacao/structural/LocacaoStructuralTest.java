package br.ifsp.locacao.structural;

import br.ifsp.locacao.domain.CodigoEquipamento;
import br.ifsp.locacao.domain.Dinheiro;
import br.ifsp.locacao.domain.EstadoLocacao;
import br.ifsp.locacao.domain.ItemLocacao;
import br.ifsp.locacao.domain.Locacao;
import br.ifsp.locacao.domain.PeriodoLocacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    @DisplayName("Rejeitar lista com item nulo mesmo após um equipamento válido")
    void rejeitaListaComItemNuloAposEquipamentoValido() {
        LocalDate inicio = LocalDate.of(2026, 11, 5);
        PeriodoLocacao periodo = new PeriodoLocacao(inicio, inicio.plusDays(3));
        ItemLocacao camera = new ItemLocacao(new CodigoEquipamento("CAM01"), "Câmera",
                new Dinheiro(new BigDecimal("100.00")));
        List<ItemLocacao> itens = Arrays.asList(camera, null);

        assertThatThrownBy(() -> new Locacao("cliente-1", periodo, itens))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Itens são obrigatórios");
    }

    @Test
    @DisplayName("Rejeitar retirada com data nula preservando a locação aberta e sem retirada")
    void rejeitaDataDeRetiradaNulaSemAlterarLocacao() {
        LocalDate inicio = LocalDate.of(2026, 11, 5);
        PeriodoLocacao periodo = new PeriodoLocacao(inicio, inicio.plusDays(3));
        ItemLocacao camera = new ItemLocacao(new CodigoEquipamento("CAM01"), "Câmera",
                new Dinheiro(new BigDecimal("100.00")));
        Locacao locacao = new Locacao("cliente-1", periodo, List.of(camera));

        assertThatThrownBy(() -> locacao.confirmarRetirada(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data de retirada é obrigatória");
        assertThat(locacao.getEstado()).isEqualTo(EstadoLocacao.ABERTA);
        assertThat(locacao.dados().retirada()).isNull();
    }

    @Test
    @DisplayName("Rejeitar renovação com data de solicitação nula sem alterar prazo, contador ou estado")
    void rejeitaDataDeSolicitacaoNulaNaRenovacaoSemAlterarLocacao() {
        LocalDate inicio = LocalDate.of(2026, 11, 5);
        PeriodoLocacao periodo = new PeriodoLocacao(inicio, inicio.plusDays(3));
        ItemLocacao camera = new ItemLocacao(new CodigoEquipamento("CAM01"), "Câmera",
                new Dinheiro(new BigDecimal("100.00")));
        Locacao locacao = new Locacao("cliente-1", periodo, List.of(camera));
        locacao.confirmarRetirada(inicio);

        assertThatThrownBy(() -> locacao.renovar(1, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data da solicitação é obrigatória");
        assertThat(locacao.getPeriodo()).isEqualTo(periodo);
        assertThat(locacao.getQuantidadeRenovacoes()).isZero();
        assertThat(locacao.getEstado()).isEqualTo(EstadoLocacao.EM_ANDAMENTO);
    }

    @Test
    @DisplayName("Rejeitar renovação anterior à retirada sem alterar prazo, contador ou estado")
    void rejeitaRenovacaoAnteriorARetiradaSemAlterarLocacao() {
        LocalDate inicio = LocalDate.of(2026, 11, 5);
        PeriodoLocacao periodo = new PeriodoLocacao(inicio, inicio.plusDays(3));
        ItemLocacao camera = new ItemLocacao(new CodigoEquipamento("CAM01"), "Câmera",
                new Dinheiro(new BigDecimal("100.00")));
        Locacao locacao = new Locacao("cliente-1", periodo, List.of(camera));
        LocalDate retirada = inicio.plusDays(1);
        locacao.confirmarRetirada(retirada);

        assertThatThrownBy(() -> locacao.renovar(1, inicio))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Solicitação não pode anteceder a retirada");
        assertThat(locacao.getPeriodo()).isEqualTo(periodo);
        assertThat(locacao.getQuantidadeRenovacoes()).isZero();
        assertThat(locacao.getEstado()).isEqualTo(EstadoLocacao.EM_ANDAMENTO);
        assertThat(locacao.dados().retirada()).isEqualTo(retirada);
    }
}
