package br.ifsp.locacao.application;

import br.ifsp.locacao.domain.Usuario;
import br.ifsp.locacao.domain.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
}
