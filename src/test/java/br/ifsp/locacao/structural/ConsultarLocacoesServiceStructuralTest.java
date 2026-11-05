package br.ifsp.locacao.structural;

import br.ifsp.locacao.application.ConsultarLocacoesService;
import br.ifsp.locacao.domain.LocacaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

@Tag("Structural")
@Tag("UnitTest")
class ConsultarLocacoesServiceStructuralTest {

    @Test
    @DisplayName("Rejeitar cliente nulo antes de consultar o repositório")
    void rejeitaClienteNuloSemConsultarRepositorio() {
        LocacaoRepository repository = mock(LocacaoRepository.class);
        ConsultarLocacoesService service = new ConsultarLocacoesService(repository);

        assertThatThrownBy(() -> service.consultarPorCliente(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cliente é obrigatório");
        verifyNoInteractions(repository);
    }
}
