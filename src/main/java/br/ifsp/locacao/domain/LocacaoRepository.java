package br.ifsp.locacao.domain;

import java.util.Optional;
import java.util.UUID;

public interface LocacaoRepository {
    void salvar(Locacao locacao);
    Optional<Locacao> buscarPorId(UUID id);
    boolean estaReservado(CodigoEquipamento codigo, PeriodoLocacao periodo);
    boolean estaReservado(CodigoEquipamento codigo, PeriodoLocacao periodo, UUID locacaoIgnorada);
}
