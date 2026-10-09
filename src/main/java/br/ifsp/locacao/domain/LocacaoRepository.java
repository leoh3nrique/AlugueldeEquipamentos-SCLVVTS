package br.ifsp.locacao.domain;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface LocacaoRepository {
    void salvar(Locacao locacao);
    Optional<Locacao> buscarPorId(UUID id);
    List<Locacao> listar();
    boolean estaReservado(CodigoEquipamento codigo, PeriodoLocacao periodo);
    boolean estaReservado(CodigoEquipamento codigo, PeriodoLocacao periodo, UUID locacaoIgnorada);
}
