package br.ifsp.locacao.infrastructure;

import br.ifsp.locacao.domain.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import java.nio.file.Path;
import java.time.*;
import java.math.BigDecimal;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

@Tag("IntegrationTest")
class SqliteLocacaoRepositoryTest {
    @TempDir Path temp;
    private SqliteLocacaoRepository repo() {
        DriverManagerDataSource ds = new DriverManagerDataSource("jdbc:sqlite:" + temp.resolve("test.db"));
        return new SqliteLocacaoRepository(new JdbcTemplate(ds), new ObjectMapper().findAndRegisterModules());
    }
    private Locacao locacao() {
        return new Locacao("cliente", new PeriodoLocacao(LocalDate.of(2026,10,6), LocalDate.of(2026,10,9)), List.of(
                new ItemLocacao(new CodigoEquipamento("CAM01"), "Câmera", new Dinheiro(BigDecimal.TEN)),
                new ItemLocacao(new CodigoEquipamento("PROJ01"), "Projetor", new Dinheiro(BigDecimal.ONE))));
    }
    @Test void recuperaAgregadoCompletoAoReabrirBanco() {
        SqliteLocacaoRepository r = repo(); Locacao l = locacao();
        l.confirmarRetirada(l.getPeriodo().inicio());
        l.registrarDevolucao(List.of(new CodigoEquipamento("CAM01")), l.getPeriodo().inicio().plusDays(1));
        l.renovar(7, l.getPeriodo().inicio().plusDays(2)); r.salvar(l);
        Locacao recuperada = repo().buscarPorId(l.getId()).orElseThrow();
        assertThat(recuperada.getId()).isEqualTo(l.getId());
        assertThat(recuperada.getCriadaEm()).isEqualTo(l.getCriadaEm());
        assertThat(recuperada.getEstado()).isEqualTo(EstadoLocacao.PARCIALMENTE_DEVOLVIDA);
        assertThat(recuperada.getQuantidadeRenovacoes()).isEqualTo(1);
        assertThat(recuperada.estaDevolvido(new CodigoEquipamento("CAM01"))).isTrue();
        assertThat(recuperada.getValorTotal()).isEqualTo(l.getValorTotal());
        assertThat(repo().listar()).hasSize(1);
    }
    @Test void detectaSobreposicaoExcluiPropriaReservaELiberaCancelamento() {
        SqliteLocacaoRepository r = repo(); Locacao l = locacao(); r.salvar(l);
        CodigoEquipamento c = new CodigoEquipamento("CAM01");
        assertThat(r.estaReservado(c, new PeriodoLocacao(LocalDate.of(2026,10,8), LocalDate.of(2026,10,11)))).isTrue();
        assertThat(r.estaReservado(c, l.getPeriodo(), l.getId())).isFalse();
        assertThat(r.estaReservado(c, new PeriodoLocacao(LocalDate.of(2026,10,9), LocalDate.of(2026,10,12)))).isFalse();
        l.cancelar(); r.salvar(l); assertThat(r.estaReservado(c, l.getPeriodo())).isFalse();
    }
    @Test void liberaItemDevolvidoMantendoPendenteReservado() {
        SqliteLocacaoRepository r = repo(); Locacao l = locacao(); l.confirmarRetirada(l.getPeriodo().inicio());
        l.registrarDevolucao(List.of(new CodigoEquipamento("CAM01")), l.getPeriodo().inicio().plusDays(1)); r.salvar(l);
        assertThat(r.estaReservado(new CodigoEquipamento("CAM01"), l.getPeriodo())).isFalse();
        assertThat(r.estaReservado(new CodigoEquipamento("PROJ01"), l.getPeriodo())).isTrue();
    }
    @Test void recuperaReservaRenovadaAteQuarentaEQuatroDias() {
        Locacao l = new Locacao("cliente", new PeriodoLocacao(LocalDate.of(2026,10,6), LocalDate.of(2026,11,5)), locacao().getItens());
        l.confirmarRetirada(l.getPeriodo().inicio());
        l.renovar(7,l.getPeriodo().inicio().plusDays(1)); l.renovar(7,l.getPeriodo().inicio().plusDays(2));
        repo().salvar(l);
        Locacao recuperada=repo().buscarPorId(l.getId()).orElseThrow();
        assertThat(recuperada.getPeriodo()).isEqualTo(l.getPeriodo());
        assertThat(recuperada.getQuantidadeRenovacoes()).isEqualTo(2);
    }
}
