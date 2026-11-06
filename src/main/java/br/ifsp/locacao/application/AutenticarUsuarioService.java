package br.ifsp.locacao.application;

import br.ifsp.locacao.domain.Usuario;
import br.ifsp.locacao.domain.UsuarioRepository;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class AutenticarUsuarioService {
    private final UsuarioRepository repository;
    private final PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    public AutenticarUsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public Optional<Usuario> autenticar(String email, String senha) {
        String emailNormalizado = email.strip().toLowerCase(Locale.ROOT);
        return repository.buscarPorEmail(emailNormalizado)
                .filter(usuario -> encoder.matches(senha, usuario.senhaHash()));
    }
}
