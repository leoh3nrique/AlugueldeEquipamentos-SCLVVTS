package br.ifsp.locacao.application;

import br.ifsp.locacao.domain.Locacao;
import br.ifsp.locacao.domain.LocacaoRepository;
import br.ifsp.locacao.domain.EstadoLocacao;

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

    public List<Locacao> consultarPorCliente(String clienteId, EstadoLocacao estado) {
        return consultarPorCliente(clienteId).stream()
                .filter(locacao -> locacao.getEstado() == estado)
                .toList();
    }
}
