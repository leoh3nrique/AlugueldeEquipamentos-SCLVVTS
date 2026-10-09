package br.ifsp.locacao.application;

import br.ifsp.locacao.domain.ItemLocacao;
import br.ifsp.locacao.domain.Locacao;
import br.ifsp.locacao.domain.LocacaoRepository;
import br.ifsp.locacao.domain.PeriodoLocacao;

import java.util.List;
import java.util.UUID;

@org.springframework.stereotype.Service
@org.springframework.transaction.annotation.Transactional
public class AlterarLocacaoService {
    private final LocacaoRepository repository;

    public AlterarLocacaoService(LocacaoRepository repository) {
        this.repository = repository;
    }

    public Locacao alterar(UUID id, PeriodoLocacao periodo, List<ItemLocacao> itens) {
        Locacao locacao = repository.buscarPorId(id).orElseThrow(() -> new java.util.NoSuchElementException("Locação não encontrada"));
        for (ItemLocacao item : itens) {
            if (repository.estaReservado(item.codigo(), periodo, id)) {
                throw new IllegalArgumentException("Equipamento indisponível no período");
            }
        }
        locacao.alterar(periodo, itens);
        repository.salvar(locacao);
        return locacao;
    }
}
