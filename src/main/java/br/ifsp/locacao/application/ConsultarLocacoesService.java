package br.ifsp.locacao.application;

import br.ifsp.locacao.domain.Locacao;
import br.ifsp.locacao.domain.LocacaoRepository;
import br.ifsp.locacao.domain.EstadoLocacao;

import java.util.List;
import java.util.Comparator;

@org.springframework.stereotype.Service
@org.springframework.transaction.annotation.Transactional
public class ConsultarLocacoesService {
    private final LocacaoRepository repository;

    public ConsultarLocacoesService(LocacaoRepository repository) {
        this.repository = repository;
    }

    public Locacao buscarPorId(java.util.UUID id) {
        return repository.buscarPorId(id).orElseThrow(() -> new java.util.NoSuchElementException("Locação não encontrada"));
    }

    public List<Locacao> consultarPorCliente(String clienteId) {
        if (clienteId == null || clienteId.isBlank()) throw new IllegalArgumentException("Cliente é obrigatório");
        return repository.listar().stream()
                .filter(locacao -> locacao.getClienteId().equals(clienteId))
                .sorted(Comparator.comparing(Locacao::getCriadaEm).reversed())
                .toList();
    }

    public List<Locacao> consultarPorCliente(String clienteId, EstadoLocacao estado) {
        return consultarPorCliente(clienteId).stream()
                .filter(locacao -> locacao.getEstado() == estado)
                .toList();
    }
}
