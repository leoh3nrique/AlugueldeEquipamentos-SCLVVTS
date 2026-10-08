package br.ifsp.locacao.application;

import br.ifsp.locacao.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Tag("UnitTest")
@Tag("TDD")
class RegistrarDevolucaoServiceTest {

    @Test
    @DisplayName("#35: devolver apenas um de três equipamentos e manter devolução parcial")
    void deveRegistrarDevolucaoParcialDeUmEntreTresEquipamentos() {
        LocacaoRepository repository = mock(LocacaoRepository.class);
        LocalDate inicio = LocalDate.of(2026, 10, 6);
        PeriodoLocacao periodo = new PeriodoLocacao(inicio, inicio.plusDays(3));
        CodigoEquipamento codigoCamera = new CodigoEquipamento("CAM01");
        CodigoEquipamento codigoProjetor = new CodigoEquipamento("PROJ01");
        CodigoEquipamento codigoAudio = new CodigoEquipamento("AUD01");
        List<ItemLocacao> itens = List.of(
                new ItemLocacao(codigoCamera, "Câmera", new Dinheiro(new BigDecimal("100.00"))),
                new ItemLocacao(codigoProjetor, "Projetor", new Dinheiro(new BigDecimal("50.00"))),
                new ItemLocacao(codigoAudio, "Áudio", new Dinheiro(new BigDecimal("25.00"))));
        Locacao locacao = new Locacao("cliente-1", periodo, itens);
        locacao.confirmarRetirada(inicio);
        UUID idOriginal = locacao.getId();
        assertThat(locacao.estaDevolvido(codigoCamera)).isFalse();
        assertThat(locacao.estaDevolvido(codigoProjetor)).isFalse();
        assertThat(locacao.estaDevolvido(codigoAudio)).isFalse();
        when(repository.buscarPorId(idOriginal)).thenReturn(Optional.of(locacao));
        RegistrarDevolucaoService service = new RegistrarDevolucaoService(repository);

        Locacao parcialmenteDevolvida = service.registrar(idOriginal,
                List.of(codigoCamera), inicio.plusDays(1));

        assertThat(parcialmenteDevolvida).isSameAs(locacao);
        assertThat(parcialmenteDevolvida.getId()).isEqualTo(idOriginal);
        assertThat(parcialmenteDevolvida.getClienteId()).isEqualTo("cliente-1");
        assertThat(parcialmenteDevolvida.getPeriodo()).isEqualTo(periodo);
        assertThat(parcialmenteDevolvida.getItens()).containsExactlyElementsOf(itens);
        assertThat(parcialmenteDevolvida.getEstado()).isEqualTo(EstadoLocacao.PARCIALMENTE_DEVOLVIDA);
        assertThat(parcialmenteDevolvida.estaDevolvido(codigoCamera)).isTrue();
        assertThat(parcialmenteDevolvida.estaDevolvido(codigoProjetor)).isFalse();
        assertThat(parcialmenteDevolvida.estaDevolvido(codigoAudio)).isFalse();
        verify(repository).buscarPorId(idOriginal);
        verify(repository).salvar(parcialmenteDevolvida);
    }

    @Test
    @DisplayName("#34: devolver todos os equipamentos no prazo e finalizar sem multa")
    void deveFinalizarDevolucaoCompletaNoPrazoSemMulta() {
        LocacaoRepository repository = mock(LocacaoRepository.class);
        LocalDate inicio = LocalDate.of(2026, 10, 6);
        PeriodoLocacao periodo = new PeriodoLocacao(inicio, inicio.plusDays(3));
        CodigoEquipamento codigoCamera = new CodigoEquipamento("CAM01");
        CodigoEquipamento codigoProjetor = new CodigoEquipamento("PROJ01");
        ItemLocacao camera = new ItemLocacao(codigoCamera,
                "Câmera", new Dinheiro(new BigDecimal("100.00")));
        ItemLocacao projetor = new ItemLocacao(codigoProjetor,
                "Projetor", new Dinheiro(new BigDecimal("50.00")));
        List<ItemLocacao> itens = List.of(camera, projetor);
        Locacao locacao = new Locacao("cliente-1", periodo, itens);
        locacao.confirmarRetirada(inicio);
        UUID idOriginal = locacao.getId();
        when(repository.buscarPorId(idOriginal)).thenReturn(Optional.of(locacao));
        RegistrarDevolucaoService service = new RegistrarDevolucaoService(repository);

        Locacao finalizada = service.registrar(idOriginal,
                List.of(codigoCamera, codigoProjetor), periodo.fim());

        assertThat(finalizada).isSameAs(locacao);
        assertThat(finalizada.getId()).isEqualTo(idOriginal);
        assertThat(finalizada.getClienteId()).isEqualTo("cliente-1");
        assertThat(finalizada.getPeriodo()).isEqualTo(periodo);
        assertThat(finalizada.getItens()).containsExactlyElementsOf(itens);
        assertThat(finalizada.getEstado()).isEqualTo(EstadoLocacao.FINALIZADA);
        assertThat(finalizada.estaDevolvido(codigoCamera)).isTrue();
        assertThat(finalizada.estaDevolvido(codigoProjetor)).isTrue();
        assertThat(finalizada.getValorNormal().valor()).isEqualByComparingTo("450.00");
        assertThat(finalizada.getMulta().valor()).isEqualByComparingTo("0.00");
        assertThat(finalizada.getValorTotal().valor()).isEqualByComparingTo("450.00");
        verify(repository).buscarPorId(idOriginal);
        verify(repository).salvar(finalizada);
    }
}
