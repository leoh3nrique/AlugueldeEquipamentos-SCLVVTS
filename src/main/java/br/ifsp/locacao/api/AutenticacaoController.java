package br.ifsp.locacao.api;

import br.ifsp.locacao.application.AutenticarUsuarioService;
import br.ifsp.locacao.application.CadastrarUsuarioService;
import br.ifsp.locacao.domain.Usuario;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AutenticacaoController {
    private final CadastrarUsuarioService cadastro;
    private final AutenticarUsuarioService autenticacao;
    private final SessionAuthenticationStrategy sessoes;
    private final SecurityContextRepository contextos;

    public AutenticacaoController(CadastrarUsuarioService cadastro, AutenticarUsuarioService autenticacao,
                                  SessionAuthenticationStrategy sessoes, SecurityContextRepository contextos) {
        this.cadastro = cadastro;
        this.autenticacao = autenticacao;
        this.sessoes = sessoes;
        this.contextos = contextos;
    }

    public record CadastroEntrada(String nome, String email, String senha) {}
    public record CsrfSaida(String token, String headerName) {}
    public record LoginEntrada(String email, String senha) {}
    public record UsuarioSaida(UUID id, String nome, String email) {}

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioSaida cadastrar(@RequestBody CadastroEntrada entrada) {
        Usuario usuario = cadastro.cadastrar(entrada.nome(), entrada.email(), entrada.senha());
        return new UsuarioSaida(usuario.id(), usuario.nome(), usuario.email());
    }

    @GetMapping("/csrf")
    public CsrfSaida csrf(CsrfToken token) {
        return new CsrfSaida(token.getToken(), token.getHeaderName());
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioSaida.class))),
            @ApiResponse(responseCode = "401", description = "E-mail ou senha inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiExceptionHandler.Erro.class)))
    })
    @PostMapping("/login")
    public UsuarioSaida login(@RequestBody LoginEntrada entrada, HttpServletRequest requisicao,
                              HttpServletResponse resposta) {
        Usuario usuario = autenticacao.autenticar(entrada.email(), entrada.senha())
                .orElseThrow(() -> new BadCredentialsException("E-mail ou senha inválidos"));
        UsuarioSaida saida = new UsuarioSaida(usuario.id(), usuario.nome(), usuario.email());
        var autenticado = UsernamePasswordAuthenticationToken.authenticated(saida, null, List.of());
        sessoes.onAuthentication(autenticado, requisicao, resposta);
        var contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(autenticado);
        SecurityContextHolder.setContext(contexto);
        contextos.saveContext(contexto, requisicao, resposta);
        return saida;
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário da sessão",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioSaida.class))),
            @ApiResponse(responseCode = "401", description = "Sessão não autenticada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiExceptionHandler.Erro.class)))
    })
    @GetMapping("/sessao")
    public ResponseEntity<?> consultarSessao(@AuthenticationPrincipal UsuarioSaida usuario) {
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiExceptionHandler.Erro("Sessão não autenticada"));
        }
        return ResponseEntity.ok(usuario);
    }
}
