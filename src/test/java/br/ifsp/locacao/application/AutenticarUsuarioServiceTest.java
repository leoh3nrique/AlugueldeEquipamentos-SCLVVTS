package br.ifsp.locacao.application;

import br.ifsp.locacao.domain.Usuario;
import br.ifsp.locacao.domain.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("UnitTest")
class AutenticarUsuarioServiceTest {
    @ParameterizedTest
    @ValueSource(strings = {"ana@example.com", "ANA@EXAMPLE.COM", " ana@example.com "})
    @DisplayName("Senha correta autentica o usuário salvo mesmo com variações no e-mail")
    void autenticaUsuarioComSenhaCorreta(String email) {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        AutenticarUsuarioService service = new AutenticarUsuarioService(repository);
        String hash = PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("SenhaDeTeste123!");
        Usuario usuario = new Usuario(UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "Ana Silva", "ana@example.com", hash);
        when(repository.buscarPorEmail("ana@example.com")).thenReturn(Optional.of(usuario));

        Optional<Usuario> autenticado = service.autenticar(email, "SenhaDeTeste123!");

        assertThat(autenticado).contains(usuario);
        verify(repository).buscarPorEmail("ana@example.com");
        verify(repository, never()).salvar(any(Usuario.class));
    }

    @ParameterizedTest
    @MethodSource("credenciaisInvalidas")
    @DisplayName("Credenciais inválidas não autenticam nem alteram o usuário")
    void rejeitaCredenciaisInvalidas(String email, String senha) {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        AutenticarUsuarioService service = new AutenticarUsuarioService(repository);
        String hash = PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("SenhaDeTeste123!");
        Usuario usuario = new Usuario(UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "Ana Silva", "ana@example.com", hash);
        when(repository.buscarPorEmail("ana@example.com")).thenReturn(Optional.of(usuario));

        Optional<Usuario> autenticado = service.autenticar(email, senha);

        assertThat(autenticado).isEmpty();
        verify(repository, never()).salvar(any(Usuario.class));
    }

    private static Stream<Arguments> credenciaisInvalidas() {
        return Stream.of(
                Arguments.of("ana@example.com", "SenhaErrada123!"),
                Arguments.of("outro@example.com", "SenhaDeTeste123!"),
                Arguments.of(null, "SenhaDeTeste123!"),
                Arguments.of("", "SenhaDeTeste123!"),
                Arguments.of("   ", "SenhaDeTeste123!"),
                Arguments.of("ana@example.com", null),
                Arguments.of("ana@example.com", ""),
                Arguments.of("ana@example.com", "   "),
                Arguments.of("ana@example.com", " SenhaDeTeste123! ")
        );
    }
}
