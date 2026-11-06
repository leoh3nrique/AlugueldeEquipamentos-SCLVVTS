package br.ifsp.locacao.domain;

import java.util.Optional;

public interface UsuarioRepository {
    void salvar(Usuario usuario);
    Optional<Usuario> buscarPorEmail(String email);
}
