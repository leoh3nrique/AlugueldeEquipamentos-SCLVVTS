package br.ifsp.locacao.application;

import br.ifsp.locacao.domain.CodigoEquipamento;
import br.ifsp.locacao.domain.Locacao;
import br.ifsp.locacao.domain.LocacaoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@org.springframework.stereotype.Service
@org.springframework.transaction.annotation.Transactional
public class RegistrarDevolucaoService {
    private final LocacaoRepository repository;

    public RegistrarDevolucaoService(LocacaoRepository repository) {
        this.repository = repository;
    }

    public Locacao registrar(UUID id, List<CodigoEquipamento> codigos, LocalDate dataDevolucao) {
        Locacao locacao = repository.buscarPorId(id).orElseThrow(() -> new java.util.NoSuchElementException("Locação não encontrada"));
        locacao.registrarDevolucao(codigos, dataDevolucao);
        repository.salvar(locacao);
        return locacao;
    }
}
