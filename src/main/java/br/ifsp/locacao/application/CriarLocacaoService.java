package br.ifsp.locacao.application;

import br.ifsp.locacao.domain.ItemLocacao;
import br.ifsp.locacao.domain.Locacao;
import br.ifsp.locacao.domain.LocacaoRepository;
import br.ifsp.locacao.domain.PeriodoLocacao;

import java.util.List;

public class CriarLocacaoService {
    private final LocacaoRepository repository;

    public CriarLocacaoService(LocacaoRepository repository) {
        this.repository = repository;
    }

    public Locacao criar(String clienteId, PeriodoLocacao periodo, List<ItemLocacao> itens) {
        Locacao locacao = new Locacao(clienteId, periodo, itens);
        for (ItemLocacao item : locacao.getItens()) {
            if (repository.estaReservado(item.codigo(), periodo)) {
                throw new IllegalArgumentException("Equipamento indisponível no período");
            }
        }
        repository.salvar(locacao);
        return locacao;
    }
}
