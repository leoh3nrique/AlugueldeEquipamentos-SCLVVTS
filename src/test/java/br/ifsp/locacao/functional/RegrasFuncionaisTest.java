package br.ifsp.locacao.functional;

import br.ifsp.locacao.domain.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

@Tag("UnitTest")
@Tag("Functional")
class RegrasFuncionaisTest {
    private final LocalDate inicio = LocalDate.of(2026, 10, 6);
    private final CodigoEquipamento codigo = new CodigoEquipamento("CAM01");
    private ItemLocacao item(String diaria) {
        return new ItemLocacao(codigo, "Câmera", new Dinheiro(new BigDecimal(diaria)));
    }
    private Locacao locacao(int dias) {
        Locacao l = new Locacao("cliente", new PeriodoLocacao(inicio, inicio.plusDays(dias)), List.of(item("100.00")));
        l.confirmarRetirada(inicio);
        return l;
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 30})
    void aceitaLimitesDoPeriodoInicial(int dias) { assertThat(locacao(dias).getPeriodo().fim()).isEqualTo(inicio.plusDays(dias)); }
    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 31})
    void rejeitaPeriodosIniciaisInvalidos(int dias) {
        assertThatThrownBy(() -> locacao(dias)).isInstanceOf(IllegalArgumentException.class);
    }
    @ParameterizedTest
    @ValueSource(ints = {1, 7})
    void aceitaLimitesDaRenovacao(int dias) {
        Locacao l = locacao(3); l.renovar(dias, inicio.plusDays(1));
        assertThat(l.getPeriodo().fim()).isEqualTo(inicio.plusDays(3 + dias));
    }
    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 8})
    void rejeitaDiasInvalidosSemAlterarAgregado(int dias) {
        Locacao l = locacao(3); PeriodoLocacao original = l.getPeriodo();
        assertThatThrownBy(() -> l.renovar(dias, inicio.plusDays(1))).isInstanceOf(IllegalArgumentException.class);
        assertThat(l.getPeriodo()).isEqualTo(original); assertThat(l.getQuantidadeRenovacoes()).isZero();
    }
    @Test void permiteDuasRenovacoesDeSeteDiasAposReservaDeTrinta() {
        Locacao l = locacao(30); l.renovar(7, inicio.plusDays(1)); l.renovar(7, inicio.plusDays(2));
        assertThat(l.getPeriodo().fim()).isEqualTo(inicio.plusDays(44));
        assertThatThrownBy(() -> l.renovar(1, inicio.plusDays(3))).isInstanceOf(IllegalStateException.class);
    }
    @Test void rejeitaRenovacaoNaDataFinal() {
        Locacao l = locacao(3);
        assertThatThrownBy(() -> l.renovar(1, inicio.plusDays(3))).isInstanceOf(IllegalArgumentException.class);
        assertThat(l.getQuantidadeRenovacoes()).isZero();
    }
    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void rejeitaClienteECodigoEDescricaoEmBranco(String texto) {
        assertThatThrownBy(() -> new Locacao(texto, new PeriodoLocacao(inicio, inicio.plusDays(1)), List.of(item("1.00"))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CodigoEquipamento(texto)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ItemLocacao(codigo, texto, new Dinheiro(BigDecimal.ONE))).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void rejeitaNulosComErroDeValidacao() {
        assertThatThrownBy(() -> new PeriodoLocacao(null, inicio)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Dinheiro(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CodigoEquipamento(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ItemLocacao(codigo, "Câmera", null)).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void rejeitaDevolucaoVaziaDesconhecidaEDuplicada() {
        Locacao l = locacao(3);
        for (List<CodigoEquipamento> codigos : List.of(List.<CodigoEquipamento>of(), List.of(new CodigoEquipamento("OUTRO")), List.of(codigo, codigo))) {
            assertThatThrownBy(() -> l.registrarDevolucao(codigos, inicio.plusDays(1))).isInstanceOf(IllegalArgumentException.class);
            assertThat(l.estaDevolvido(codigo)).isFalse(); assertThat(l.getEstado()).isEqualTo(EstadoLocacao.EM_ANDAMENTO);
        }
    }
    @Test void rejeitaDevolucaoAnteriorARetirada() {
        Locacao l = locacao(3);
        assertThatThrownBy(() -> l.registrarDevolucao(List.of(codigo), inicio.minusDays(1))).isInstanceOf(IllegalArgumentException.class);
        assertThat(l.estaDevolvido(codigo)).isFalse();
    }
    @Test void validaLoteInteiroAntesDeModificar() {
        CodigoEquipamento outro = new CodigoEquipamento("PROJ01");
        Locacao l = new Locacao("cliente", new PeriodoLocacao(inicio, inicio.plusDays(3)), List.of(item("100.00"), new ItemLocacao(outro, "Projetor", new Dinheiro(BigDecimal.TEN))));
        l.confirmarRetirada(inicio); l.registrarDevolucao(List.of(codigo), inicio.plusDays(1));
        assertThatThrownBy(() -> l.registrarDevolucao(List.of(outro, codigo), inicio.plusDays(2))).isInstanceOf(IllegalArgumentException.class);
        assertThat(l.estaDevolvido(outro)).isFalse();
    }
    @Test void liberaEquipamentoDevolvido() {
        Locacao l = locacao(3); l.registrarDevolucao(List.of(codigo), inicio.plusDays(3));
        assertThat(l.bloqueiaReserva(codigo)).isFalse();
    }
    @Test void arredondaMultaDoItemParaCentavos() {
        Locacao l = new Locacao("cliente", new PeriodoLocacao(inicio, inicio.plusDays(1)), List.of(item("0.03")));
        l.confirmarRetirada(inicio); l.registrarDevolucao(List.of(codigo), inicio.plusDays(2));
        assertThat(l.getMulta().valor()).isEqualByComparingTo("0.01");
        assertThat(l.getValorTotal().valor()).isEqualByComparingTo("0.04");
    }
    @Test void preservaColecaoContraAlteracaoExterna() {
        Locacao l = locacao(3);
        assertThatThrownBy(() -> l.getItens().clear()).isInstanceOf(UnsupportedOperationException.class);
    }
}
