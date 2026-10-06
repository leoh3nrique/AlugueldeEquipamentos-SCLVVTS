package br.ifsp.locacao.domain;

public interface LocacaoRepository {
    void salvar(Locacao locacao);
    boolean estaReservado(CodigoEquipamento codigo, PeriodoLocacao periodo);
}
