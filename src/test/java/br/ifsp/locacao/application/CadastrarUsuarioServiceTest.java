package br.ifsp.locacao.application;

import br.ifsp.locacao.domain.Usuario;
import br.ifsp.locacao.domain.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@Tag("UnitTest")
class CadastrarUsuarioServiceTest {
    @Test
    @DisplayName("Cadastro salva o usuário com ID e hash da senha informada")
    void cadastraUsuarioComSenhaProtegida() {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        CadastrarUsuarioService service = new CadastrarUsuarioService(repository);
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        String senha = "SenhaDeTeste123!";

        Usuario cadastrado = service.cadastrar("Ana Silva", "ana@example.com", senha);

        assertThat(cadastrado.id()).isNotNull();
        assertThat(cadastrado.nome()).isEqualTo("Ana Silva");
        assertThat(cadastrado.email()).isEqualTo("ana@example.com");
        assertThat(cadastrado.senhaHash()).isNotEqualTo(senha);
        assertThat(encoder.matches(senha, cadastrado.senhaHash())).isTrue();
        verify(repository).salvar(cadastrado);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("Cadastro rejeita nome ausente ou em branco sem salvar o usuário")
    void rejeitaCadastroSemNome(String nome) {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        CadastrarUsuarioService service = new CadastrarUsuarioService(repository);

        assertThatThrownBy(() -> service.cadastrar(nome, "ana@example.com", "SenhaDeTeste123!"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nome é obrigatório");
        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "ana.example.com", "@example.com", "ana@",
            "ana@@example.com", "ana silva@example.com", "ana@example..com"})
    @DisplayName("Cadastro rejeita e-mail ausente ou inválido sem salvar o usuário")
    void rejeitaCadastroComEmailInvalido(String email) {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        CadastrarUsuarioService service = new CadastrarUsuarioService(repository);

        assertThatThrownBy(() -> service.cadastrar("Ana Silva", email, "SenhaDeTeste123!"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("E-mail inválido");
        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("Cadastro rejeita senha ausente ou em branco sem salvar o usuário")
    void rejeitaCadastroSemSenha(String senha) {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        CadastrarUsuarioService service = new CadastrarUsuarioService(repository);

        assertThatThrownBy(() -> service.cadastrar("Ana Silva", "ana@example.com", senha))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Senha é obrigatória");
        verifyNoInteractions(repository);
    }
}
