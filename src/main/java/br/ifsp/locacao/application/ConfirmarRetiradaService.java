package br.ifsp.locacao.application;

import br.ifsp.locacao.domain.Locacao;
import br.ifsp.locacao.domain.LocacaoRepository;

import java.time.LocalDate;
import java.util.UUID;

public class ConfirmarRetiradaService {
    private final LocacaoRepository repository;

    public ConfirmarRetiradaService(LocacaoRepository repository) {
        this.repository = repository;
    }

    public Locacao confirmar(UUID id, LocalDate dataRetirada) {
        Locacao locacao = repository.buscarPorId(id).orElseThrow();
        locacao.confirmarRetirada(dataRetirada);
        repository.salvar(locacao);
        return locacao;
    }
}
