package br.ifsp.locacao.infrastructure;

import br.ifsp.locacao.domain.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("IntegrationTest")
class SqliteUsuarioRepositoryTest {
    @TempDir Path temp;

    private SqliteUsuarioRepository repo() {
        DriverManagerDataSource ds = new DriverManagerDataSource("jdbc:sqlite:" + temp.resolve("usuarios.db"));
        return new SqliteUsuarioRepository(new JdbcTemplate(ds));
    }

    @Test
    @DisplayName("Busca por e-mail recupera o usuário correto após reabrir o banco")
    void recuperaUsuarioPeloEmailAoReabrirBanco() {
        Usuario ana = new Usuario(UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "Ana Silva", "ana@example.com", "hash-ana");
        Usuario bruno = new Usuario(UUID.fromString("00000000-0000-0000-0000-000000000002"),
                "Bruno Souza", "bruno@example.com", "hash-bruno");
        SqliteUsuarioRepository repository = repo();
        repository.salvar(ana);
        repository.salvar(bruno);

        SqliteUsuarioRepository reaberto = repo();

        assertThat(reaberto.buscarPorEmail("ana@example.com")).contains(ana);
        assertThat(reaberto.buscarPorEmail("bruno@example.com")).contains(bruno);
        assertThat(reaberto.buscarPorEmail("ausente@example.com")).isEmpty();
    }
}
