package br.ifsp.locacao.api;

import br.ifsp.locacao.LocacaoApplication;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;

import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("IntegrationTest")
class AutenticacaoIntegracaoTest {
    @TempDir Path temp;
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Aplicação recém-iniciada cadastra, autentica e encerra a sessão por HTTP")
    void autenticaComConfiguracaoDeSegurancaRecemIniciada() throws Exception {
        try (var contexto = new SpringApplicationBuilder(LocacaoApplication.class).run(
                "--server.port=0", "--spring.main.banner-mode=off",
                "--spring.datasource.url=jdbc:sqlite:" + temp.resolve("autenticacao.db"))) {
            int porta = ((ServletWebServerApplicationContext) contexto).getWebServer().getPort();
            URI base = URI.create("http://localhost:" + porta);
            CookieManager cookies = new CookieManager();
            HttpClient cliente = HttpClient.newBuilder().cookieHandler(cookies)
                    .connectTimeout(Duration.ofSeconds(10)).build();
            JsonNode semSessao = mapper.readTree("""
                    {"mensagem":"Sessão não autenticada"}
                    """);
            assertThat(json(enviar(cliente, base, "GET", "/api/auth/sessao", null, null), 401))
                    .isEqualTo(semSessao);
            assertThat(json(enviar(cliente, base, "GET", "/api/locacoes?clienteId=cliente-1", null, null), 401))
                    .isEqualTo(semSessao);
            String csrf = csrf(cliente, base);
            var cookieInicial = cookies.getCookieStore().getCookies().stream()
                    .filter(cookie -> cookie.getName().equals("JSESSIONID")).findFirst().orElseThrow();
            assertThat(cookieInicial.isHttpOnly()).isTrue();
            String idSessaoInicial = cookieInicial.getValue();

            JsonNode cadastro = json(enviar(cliente, base, "POST", "/api/auth/cadastro", """
                    {"nome":"Ana Silva","email":"ana@example.com","senha":"SenhaDeTeste123!"}
                    """, csrf), 201);
            String id = cadastro.path("id").textValue();
            assertThat(UUID.fromString(id).toString()).isEqualTo(id);
            JsonNode usuarioEsperado = mapper.createObjectNode().put("id", id)
                    .put("nome", "Ana Silva").put("email", "ana@example.com");
            assertThat(cadastro).isEqualTo(usuarioEsperado);
            assertThat(json(enviar(cliente, base, "GET", "/api/auth/sessao", null, null), 401))
                    .isEqualTo(semSessao);

            assertThat(json(enviar(cliente, base, "POST", "/api/auth/login", """
                    {"email":"ana@example.com","senha":"SenhaDeTeste123!"}
                    """, csrf), 200)).isEqualTo(usuarioEsperado);
            var cookieAutenticado = cookies.getCookieStore().getCookies().stream()
                    .filter(cookie -> cookie.getName().equals("JSESSIONID")).findFirst().orElseThrow();
            assertThat(cookieAutenticado.getValue()).isNotEqualTo(idSessaoInicial);
            assertThat(cookieAutenticado.isHttpOnly()).isTrue();
            assertThat(json(enviar(cliente, base, "GET", "/api/auth/sessao", null, null), 200))
                    .isEqualTo(usuarioEsperado);

            var logout = enviar(cliente, base, "POST", "/api/auth/logout", "", csrf(cliente, base));
            assertThat(logout.statusCode()).isEqualTo(204);
            assertThat(logout.body()).isEmpty();
            assertThat(logout.headers().allValues("Set-Cookie"))
                    .anySatisfy(cookie -> assertThat(cookie).contains("JSESSIONID=", "Max-Age=0"));
            assertThat(json(enviar(cliente, base, "GET", "/api/auth/sessao", null, null), 401))
                    .isEqualTo(semSessao);
        }
    }

    private String csrf(HttpClient cliente, URI base) throws Exception {
        JsonNode dados = json(enviar(cliente, base, "GET", "/api/auth/csrf", null, null), 200);
        assertThat(dados.size()).isEqualTo(2);
        assertThat(dados.path("headerName").textValue()).isEqualTo("X-CSRF-TOKEN");
        assertThat(dados.path("token").isTextual()).isTrue();
        assertThat(dados.path("token").textValue()).isNotBlank();
        return dados.path("token").textValue();
    }

    private JsonNode json(HttpResponse<String> resposta, int status) throws Exception {
        assertThat(resposta.statusCode()).isEqualTo(status);
        assertThat(resposta.headers().firstValue("Content-Type").orElseThrow()).startsWith("application/json");
        return mapper.readTree(resposta.body());
    }

    private HttpResponse<String> enviar(HttpClient cliente, URI base, String metodo,
                                         String caminho, String corpo, String csrf) throws Exception {
        var requisicao = HttpRequest.newBuilder(base.resolve(caminho)).timeout(Duration.ofSeconds(10));
        if (csrf != null) requisicao.header("X-CSRF-TOKEN", csrf);
        if (corpo != null) requisicao.header("Content-Type", "application/json");
        requisicao.method(metodo, corpo == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(corpo));
        return cliente.send(requisicao.build(), HttpResponse.BodyHandlers.ofString());
    }
}
