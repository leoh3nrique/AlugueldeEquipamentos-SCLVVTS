package br.ifsp.locacao.api;

import br.ifsp.locacao.application.CadastrarUsuarioService;
import br.ifsp.locacao.domain.Usuario;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("UnitTest")
class AutenticacaoControllerTest {
    @Test
    @DisplayName("Cadastro retorna 201 com ID, nome e e-mail, sem expor senha ou hash")
    void cadastroRetornaUsuarioSemExporSenha() throws Exception {
        CadastrarUsuarioService cadastro = mock(CadastrarUsuarioService.class);
        Usuario usuario = new Usuario(UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "Ana Silva", "ana@example.com", "hash-que-nao-deve-sair");
        when(cadastro.cadastrar("Ana Silva", "ana@example.com", "SenhaDeTeste123!"))
                .thenReturn(usuario);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new AutenticacaoController(cadastro))
                .setControllerAdvice(new ApiExceptionHandler()).build();
        ObjectMapper mapper = new ObjectMapper();

        var resposta = mvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Ana Silva","email":"ana@example.com","senha":"SenhaDeTeste123!"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertThat(mapper.readTree(resposta.getContentAsByteArray())).isEqualTo(mapper.readTree("""
                {"id":"00000000-0000-0000-0000-000000000001","nome":"Ana Silva","email":"ana@example.com"}
                """));
        verify(cadastro).cadastrar("Ana Silva", "ana@example.com", "SenhaDeTeste123!");
    }
}
