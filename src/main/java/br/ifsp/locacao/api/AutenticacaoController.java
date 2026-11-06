package br.ifsp.locacao.api;

import br.ifsp.locacao.application.CadastrarUsuarioService;
import br.ifsp.locacao.domain.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AutenticacaoController {
    private final CadastrarUsuarioService cadastro;

    public AutenticacaoController(CadastrarUsuarioService cadastro) {
        this.cadastro = cadastro;
    }

    public record CadastroEntrada(String nome, String email, String senha) {}
    public record UsuarioSaida(UUID id, String nome, String email) {}

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioSaida cadastrar(@RequestBody CadastroEntrada entrada) {
        Usuario usuario = cadastro.cadastrar(entrada.nome(), entrada.email(), entrada.senha());
        return new UsuarioSaida(usuario.id(), usuario.nome(), usuario.email());
    }
}
