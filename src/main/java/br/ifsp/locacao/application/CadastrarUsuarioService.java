package br.ifsp.locacao.application;

import br.ifsp.locacao.domain.Usuario;
import br.ifsp.locacao.domain.UsuarioRepository;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class CadastrarUsuarioService {
    private final UsuarioRepository repository;
    private final PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    public CadastrarUsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public Usuario cadastrar(String nome, String email, String senha) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        if (email == null || !email.strip().matches("[^\\s@]+@[^\\s@.]+(?:\\.[^\\s@.]+)+")) {
            throw new IllegalArgumentException("E-mail inválido");
        }
        if (senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("Senha é obrigatória");
        }
        String emailNormalizado = email.strip().toLowerCase(Locale.ROOT);
        if (repository.buscarPorEmail(emailNormalizado).isPresent()) {
            throw new IllegalStateException("E-mail já cadastrado");
        }
        Usuario usuario = new Usuario(UUID.randomUUID(), nome, emailNormalizado, encoder.encode(senha));
        repository.salvar(usuario);
        return usuario;
    }
}
