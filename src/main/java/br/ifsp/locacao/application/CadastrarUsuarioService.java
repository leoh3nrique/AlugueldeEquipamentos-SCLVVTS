package br.ifsp.locacao.application;

import br.ifsp.locacao.domain.Usuario;
import br.ifsp.locacao.domain.UsuarioRepository;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Usuario usuario = new Usuario(UUID.randomUUID(), nome, email, encoder.encode(senha));
        repository.salvar(usuario);
        return usuario;
    }
}
