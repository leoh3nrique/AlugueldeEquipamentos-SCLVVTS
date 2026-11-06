package br.ifsp.locacao.infrastructure;

import br.ifsp.locacao.domain.Usuario;
import br.ifsp.locacao.domain.UsuarioRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class SqliteUsuarioRepository implements UsuarioRepository {
    private final JdbcTemplate jdbc;

    public SqliteUsuarioRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS usuarios (
                    id TEXT PRIMARY KEY,
                    nome TEXT NOT NULL,
                    email TEXT NOT NULL UNIQUE,
                    senha_hash TEXT NOT NULL
                )
                """);
    }

    @Override
    public void salvar(Usuario usuario) {
        jdbc.update("INSERT INTO usuarios(id, nome, email, senha_hash) VALUES(?, ?, ?, ?)",
                usuario.id().toString(), usuario.nome(), usuario.email(), usuario.senhaHash());
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return jdbc.query("SELECT id, nome, email, senha_hash FROM usuarios WHERE email = ?",
                (rs, linha) -> new Usuario(UUID.fromString(rs.getString("id")),
                        rs.getString("nome"), rs.getString("email"), rs.getString("senha_hash")),
                email).stream().findFirst();
    }
}
