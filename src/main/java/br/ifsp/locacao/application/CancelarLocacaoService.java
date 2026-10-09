package br.ifsp.locacao.application;

import br.ifsp.locacao.domain.Locacao;
import br.ifsp.locacao.domain.LocacaoRepository;

import java.util.UUID;

@org.springframework.stereotype.Service
@org.springframework.transaction.annotation.Transactional
public class CancelarLocacaoService {
    private final LocacaoRepository repository;

    public CancelarLocacaoService(LocacaoRepository repository) {
        this.repository = repository;
    }

    public Locacao cancelar(UUID id) {
        Locacao locacao = repository.buscarPorId(id).orElseThrow(() -> new java.util.NoSuchElementException("Locação não encontrada"));
        locacao.cancelar();
        repository.salvar(locacao);
        return locacao;
    }
}
