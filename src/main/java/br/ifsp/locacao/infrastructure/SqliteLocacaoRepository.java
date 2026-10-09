package br.ifsp.locacao.infrastructure;

import br.ifsp.locacao.domain.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public class SqliteLocacaoRepository implements LocacaoRepository {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    public SqliteLocacaoRepository(JdbcTemplate jdbc, ObjectMapper mapper) {
        this.jdbc = jdbc; this.mapper = mapper;
        jdbc.execute("CREATE TABLE IF NOT EXISTS locacoes (id TEXT PRIMARY KEY, dados TEXT NOT NULL)");
    }
    @Override public void salvar(Locacao locacao) {
        try {
            jdbc.update("INSERT INTO locacoes(id,dados) VALUES(?,?) ON CONFLICT(id) DO UPDATE SET dados=excluded.dados",
                    locacao.getId().toString(), mapper.writeValueAsString(locacao.dados()));
        } catch (JsonProcessingException e) { throw new IllegalStateException("Não foi possível serializar locação", e); }
    }
    private Locacao ler(String dados) {
        try { return Locacao.reconstituir(mapper.readValue(dados, LocacaoDados.class)); }
        catch (JsonProcessingException e) { throw new IllegalStateException("Dados persistidos inválidos", e); }
    }
    @Override public Optional<Locacao> buscarPorId(UUID id) {
        return jdbc.query("SELECT dados FROM locacoes WHERE id=?", (rs, n) -> ler(rs.getString(1)), id.toString()).stream().findFirst();
    }
    @Override public List<Locacao> listar() {
        return jdbc.query("SELECT dados FROM locacoes ORDER BY rowid", (rs, n) -> ler(rs.getString(1)));
    }
    @Override public boolean estaReservado(CodigoEquipamento codigo, PeriodoLocacao periodo) {
        return estaReservado(codigo, periodo, null);
    }
    @Override public boolean estaReservado(CodigoEquipamento codigo, PeriodoLocacao periodo, UUID ignorada) {
        return listar().stream().anyMatch(l -> !l.getId().equals(ignorada) && l.bloqueiaReserva(codigo) && l.getPeriodo().sobrepoe(periodo));
    }
}
