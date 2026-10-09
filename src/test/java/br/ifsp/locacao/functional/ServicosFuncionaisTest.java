package br.ifsp.locacao.functional;

import br.ifsp.locacao.application.*;
import br.ifsp.locacao.domain.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.time.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.IntStream;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("UnitTest")
@Tag("Functional")
class ServicosFuncionaisTest {
    private final LocalDate inicio = LocalDate.of(2026,10,6);
    private final PeriodoLocacao periodo = new PeriodoLocacao(inicio, inicio.plusDays(3));
    private List<ItemLocacao> itens(int quantidade) {
        return IntStream.range(0,quantidade).mapToObj(n -> new ItemLocacao(new CodigoEquipamento("EQ"+n), "Equipamento", new Dinheiro(BigDecimal.TEN))).toList();
    }
    @ParameterizedTest @ValueSource(ints={1,5})
    void criaQuantidadeNosLimites(int quantidade) {
        LocacaoRepository r=mock(LocacaoRepository.class);
        Locacao l=new CriarLocacaoService(r).criar("cliente",periodo,itens(quantidade));
        assertThat(l.getItens()).hasSize(quantidade); verify(r).salvar(l);
    }
    @ParameterizedTest @ValueSource(ints={0,6})
    void rejeitaQuantidadeForaDosLimitesSemPersistir(int quantidade) {
        LocacaoRepository r=mock(LocacaoRepository.class);
        assertThatThrownBy(() -> new CriarLocacaoService(r).criar("cliente",periodo,itens(quantidade))).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(r);
    }
    @ParameterizedTest @ValueSource(ints={-1,0,8})
    void servicoRejeitaDiasInvalidosAntesDeConsultarDisponibilidade(int dias) {
        LocacaoRepository r=mock(LocacaoRepository.class); Locacao l=new Locacao("cliente",periodo,itens(1)); l.confirmarRetirada(inicio);
        when(r.buscarPorId(l.getId())).thenReturn(Optional.of(l));
        assertThatThrownBy(() -> new RenovarLocacaoService(r).renovar(l.getId(),dias,inicio.plusDays(1))).isInstanceOf(IllegalArgumentException.class);
        verify(r,never()).estaReservado(any(),any(),any()); verify(r,never()).salvar(any());
        assertThat(l.getPeriodo()).isEqualTo(periodo); assertThat(l.getQuantidadeRenovacoes()).isZero();
    }
    @Test void preservaPrazoEValorDeItemDevolvidoAposRenovacao() {
        LocacaoRepository r=mock(LocacaoRepository.class); List<ItemLocacao> itens=itens(2);
        Locacao l=new Locacao("cliente",periodo,itens); l.confirmarRetirada(inicio);
        l.registrarDevolucao(List.of(itens.get(0).codigo()),inicio.plusDays(1));
        when(r.buscarPorId(l.getId())).thenReturn(Optional.of(l));
        new RenovarLocacaoService(r).renovar(l.getId(),7,inicio.plusDays(2));
        new RegistrarDevolucaoService(r).registrar(l.getId(),List.of(itens.get(1).codigo()),inicio.plusDays(11));
        assertThat(l.getValorNormal().valor()).isEqualByComparingTo("130.00");
        assertThat(l.getMulta().valor()).isEqualByComparingTo("2.00");
        assertThat(l.getValorTotal().valor()).isEqualByComparingTo("132.00");
    }
    @Test void operacoesDeIdInexistenteNaoSalvam() {
        LocacaoRepository r=mock(LocacaoRepository.class); UUID id=UUID.randomUUID(); when(r.buscarPorId(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> new ConsultarLocacoesService(r).buscarPorId(id)).isInstanceOf(NoSuchElementException.class);
        assertThatThrownBy(() -> new AlterarLocacaoService(r).alterar(id,periodo,itens(1))).isInstanceOf(NoSuchElementException.class);
        assertThatThrownBy(() -> new CancelarLocacaoService(r).cancelar(id)).isInstanceOf(NoSuchElementException.class);
        assertThatThrownBy(() -> new ConfirmarRetiradaService(r).confirmar(id,inicio)).isInstanceOf(NoSuchElementException.class);
        assertThatThrownBy(() -> new RegistrarDevolucaoService(r).registrar(id,List.of(new CodigoEquipamento("EQ0")),inicio)).isInstanceOf(NoSuchElementException.class);
        assertThatThrownBy(() -> new RenovarLocacaoService(r).renovar(id,1,inicio)).isInstanceOf(NoSuchElementException.class);
        verify(r,never()).salvar(any());
    }
}
