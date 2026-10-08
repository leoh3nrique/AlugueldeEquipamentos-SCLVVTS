package br.ifsp.locacao.application;

import br.ifsp.locacao.domain.ItemLocacao;
import br.ifsp.locacao.domain.Locacao;
import br.ifsp.locacao.domain.LocacaoRepository;
import br.ifsp.locacao.domain.PeriodoLocacao;

import java.time.LocalDate;
import java.util.UUID;

public class RenovarLocacaoService {
    private final LocacaoRepository repository;

    public RenovarLocacaoService(LocacaoRepository repository) {
        this.repository = repository;
    }

    public Locacao renovar(UUID id, int diasAdicionais, LocalDate dataSolicitacao) {
        Locacao locacao = repository.buscarPorId(id).orElseThrow();
        PeriodoLocacao periodoAdicional = new PeriodoLocacao(
                locacao.getPeriodo().fim(), locacao.getPeriodo().fim().plusDays(diasAdicionais));
        for (ItemLocacao item : locacao.getItens()) {
            if (!locacao.estaDevolvido(item.codigo())) {
                repository.estaReservado(item.codigo(), periodoAdicional, id);
            }
        }
        locacao.renovar(diasAdicionais, dataSolicitacao);
        repository.salvar(locacao);
        return locacao;
    }
}
