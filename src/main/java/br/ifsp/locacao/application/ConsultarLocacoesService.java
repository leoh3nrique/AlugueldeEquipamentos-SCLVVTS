package br.ifsp.locacao.application;

import br.ifsp.locacao.domain.Locacao;
import br.ifsp.locacao.domain.LocacaoRepository;

import java.util.List;

public class ConsultarLocacoesService {
    private final LocacaoRepository repository;

    public ConsultarLocacoesService(LocacaoRepository repository) {
        this.repository = repository;
    }

    public List<Locacao> consultarPorCliente(String clienteId) {
        return repository.listar().stream()
                .filter(locacao -> locacao.getClienteId().equals(clienteId))
                .toList();
    }
}
