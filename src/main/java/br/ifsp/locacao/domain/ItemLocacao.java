package br.ifsp.locacao.domain;

public record ItemLocacao(CodigoEquipamento codigo, String descricao, Dinheiro diaria) {
    public ItemLocacao {
        if (diaria.valor().signum() <= 0) {
            throw new IllegalArgumentException("Diária deve ser maior que zero");
        }
    }
}
