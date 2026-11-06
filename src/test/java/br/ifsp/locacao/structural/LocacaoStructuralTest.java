package br.ifsp.locacao.structural;

import br.ifsp.locacao.domain.CodigoEquipamento;
import br.ifsp.locacao.domain.Dinheiro;
import br.ifsp.locacao.domain.EstadoLocacao;
import br.ifsp.locacao.domain.ItemLocacao;
import br.ifsp.locacao.domain.Locacao;
import br.ifsp.locacao.domain.LocacaoDados;
import br.ifsp.locacao.domain.PeriodoLocacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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

    @Test
    @DisplayName("Rejeitar devolução com data nula preservando o equipamento pendente e a locação em andamento")
    void rejeitaDataDeDevolucaoNulaSemAlterarLocacao() {
        LocalDate inicio = LocalDate.of(2026, 11, 5);
        PeriodoLocacao periodo = new PeriodoLocacao(inicio, inicio.plusDays(3));
        ItemLocacao camera = new ItemLocacao(new CodigoEquipamento("CAM01"), "Câmera",
                new Dinheiro(new BigDecimal("100.00")));
        Locacao locacao = new Locacao("cliente-1", periodo, List.of(camera));
        locacao.confirmarRetirada(inicio);

        assertThatThrownBy(() -> locacao.registrarDevolucao(List.of(camera.codigo()), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Devolução não pode anteceder a retirada");
        assertThat(locacao.estaDevolvido(camera.codigo())).isFalse();
        assertThat(locacao.getEstado()).isEqualTo(EstadoLocacao.EM_ANDAMENTO);
        assertThat(locacao.getPeriodo()).isEqualTo(periodo);
    }

    @Test
    @DisplayName("Rejeitar devolução com lista de códigos nula preservando o equipamento pendente e o estado")
    void rejeitaListaDeCodigosNulaNaDevolucaoSemAlterarLocacao() {
        LocalDate inicio = LocalDate.of(2026, 11, 5);
        PeriodoLocacao periodo = new PeriodoLocacao(inicio, inicio.plusDays(3));
        ItemLocacao camera = new ItemLocacao(new CodigoEquipamento("CAM01"), "Câmera",
                new Dinheiro(new BigDecimal("100.00")));
        Locacao locacao = new Locacao("cliente-1", periodo, List.of(camera));
        locacao.confirmarRetirada(inicio);

        assertThatThrownBy(() -> locacao.registrarDevolucao(null, inicio.plusDays(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Informe equipamentos distintos para devolver");
        assertThat(locacao.estaDevolvido(camera.codigo())).isFalse();
        assertThat(locacao.getEstado()).isEqualTo(EstadoLocacao.EM_ANDAMENTO);
        assertThat(locacao.getPeriodo()).isEqualTo(periodo);
    }

    @Test
    @DisplayName("Rejeitar código nulo após um código válido na devolução sem alterar a locação")
    void rejeitaCodigoNuloNaDevolucaoSemAlterarLocacao() {
        LocalDate inicio = LocalDate.of(2026, 11, 5);
        PeriodoLocacao periodo = new PeriodoLocacao(inicio, inicio.plusDays(3));
        ItemLocacao camera = new ItemLocacao(new CodigoEquipamento("CAM01"), "Câmera",
                new Dinheiro(new BigDecimal("100.00")));
        Locacao locacao = new Locacao("cliente-1", periodo, List.of(camera));
        locacao.confirmarRetirada(inicio);
        List<CodigoEquipamento> codigos = Arrays.asList(camera.codigo(), null);

        assertThatThrownBy(() -> locacao.registrarDevolucao(codigos, inicio.plusDays(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Informe equipamentos distintos para devolver");
        assertThat(locacao.estaDevolvido(camera.codigo())).isFalse();
        assertThat(locacao.getEstado()).isEqualTo(EstadoLocacao.EM_ANDAMENTO);
        assertThat(locacao.getPeriodo()).isEqualTo(periodo);
    }

    @Test
    @DisplayName("Rejeitar alteração com período nulo preservando período, itens e estado da reserva")
    void rejeitaPeriodoNuloNaAlteracaoSemModificarReserva() {
        LocalDate inicio = LocalDate.of(2026, 11, 5);
        PeriodoLocacao periodo = new PeriodoLocacao(inicio, inicio.plusDays(3));
        ItemLocacao camera = new ItemLocacao(new CodigoEquipamento("CAM01"), "Câmera",
                new Dinheiro(new BigDecimal("100.00")));
        Locacao locacao = new Locacao("cliente-1", periodo, List.of(camera));
        ItemLocacao projetor = new ItemLocacao(new CodigoEquipamento("PROJ01"), "Projetor",
                new Dinheiro(new BigDecimal("50.00")));

        assertThatThrownBy(() -> locacao.alterar(null, List.of(projetor)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Período é obrigatório");
        assertThat(locacao.getPeriodo()).isEqualTo(periodo);
        assertThat(locacao.getItens()).containsExactly(camera);
        assertThat(locacao.getEstado()).isEqualTo(EstadoLocacao.ABERTA);
    }

    @Test
    @DisplayName("Liberar equipamento devolvido mantendo o pendente bloqueado na devolução parcial")
    void naoBloqueiaReservaDeItemDevolvidoEmLocacaoParcial() {
        LocalDate inicio = LocalDate.of(2026, 11, 5);
        PeriodoLocacao periodo = new PeriodoLocacao(inicio, inicio.plusDays(3));
        ItemLocacao camera = new ItemLocacao(new CodigoEquipamento("CAM01"), "Câmera",
                new Dinheiro(new BigDecimal("100.00")));
        ItemLocacao projetor = new ItemLocacao(new CodigoEquipamento("PROJ01"), "Projetor",
                new Dinheiro(new BigDecimal("50.00")));
        Locacao locacao = new Locacao("cliente-1", periodo, List.of(camera, projetor));
        locacao.confirmarRetirada(inicio);
        locacao.registrarDevolucao(List.of(camera.codigo()), periodo.fim());

        assertThat(locacao.getEstado()).isEqualTo(EstadoLocacao.PARCIALMENTE_DEVOLVIDA);
        assertThat(locacao.estaDevolvido(camera.codigo())).isTrue();
        assertThat(locacao.bloqueiaReserva(camera.codigo())).isFalse();
        assertThat(locacao.bloqueiaReserva(projetor.codigo())).isTrue();
    }

    @Test
    @DisplayName("Não bloquear reservas de equipamento que não pertence à locação")
    void naoBloqueiaReservaDeEquipamentoForaDaLocacao() {
        LocalDate inicio = LocalDate.of(2026, 11, 5);
        PeriodoLocacao periodo = new PeriodoLocacao(inicio, inicio.plusDays(3));
        ItemLocacao camera = new ItemLocacao(new CodigoEquipamento("CAM01"), "Câmera",
                new Dinheiro(new BigDecimal("100.00")));
        Locacao locacao = new Locacao("cliente-1", periodo, List.of(camera));
        CodigoEquipamento outro = new CodigoEquipamento("PROJ01");

        assertThat(locacao.bloqueiaReserva(outro)).isFalse();
        assertThat(locacao.bloqueiaReserva(camera.codigo())).isTrue();
        assertThat(locacao.getEstado()).isEqualTo(EstadoLocacao.ABERTA);
        assertThat(locacao.getItens()).containsExactly(camera);
    }

    @Test
    @DisplayName("Reconstituir locação parcialmente devolvida preservando dados, prazos e valores")
    void reconstituiLocacaoParcialmenteDevolvidaComItemPendente() {
        LocalDate inicio = LocalDate.of(2026, 11, 5);
        LocalDate fimOriginal = inicio.plusDays(3);
        LocalDate fimRenovado = inicio.plusDays(10);
        CodigoEquipamento camera = new CodigoEquipamento("CAM01");
        CodigoEquipamento projetor = new CodigoEquipamento("PROJ01");
        LocacaoDados dados = new LocacaoDados(
                UUID.fromString("a9b4f95d-16a0-4a48-84e7-fb01e03a9a2c"), "cliente-1",
                Instant.parse("2026-11-04T10:00:00Z"), inicio, fimRenovado,
                EstadoLocacao.PARCIALMENTE_DEVOLVIDA, 1, inicio,
                List.of(
                        new LocacaoDados.ItemDados(camera.valor(), "Câmera", new BigDecimal("100.00"),
                                inicio.plusDays(1), fimOriginal),
                        new LocacaoDados.ItemDados(projetor.valor(), "Projetor", new BigDecimal("50.00"),
                                null, fimRenovado)));

        Locacao recuperada = Locacao.reconstituir(dados);

        assertThat(recuperada.dados()).isEqualTo(dados);
        assertThat(recuperada.estaDevolvido(camera)).isTrue();
        assertThat(recuperada.estaDevolvido(projetor)).isFalse();
        assertThat(recuperada.getFimContratado(camera)).isEqualTo(fimOriginal);
        assertThat(recuperada.getFimContratado(projetor)).isEqualTo(fimRenovado);
        assertThat(recuperada.getValorNormal().valor()).isEqualByComparingTo("800.00");
        assertThat(recuperada.getMulta().valor()).isEqualByComparingTo("0.00");
        assertThat(recuperada.getValorTotal().valor()).isEqualByComparingTo("800.00");
    }
}
