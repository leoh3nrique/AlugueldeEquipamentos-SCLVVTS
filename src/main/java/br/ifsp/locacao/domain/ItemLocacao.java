package br.ifsp.locacao.domain;

public record ItemLocacao(CodigoEquipamento codigo, String descricao, Dinheiro diaria) {
    public ItemLocacao {
        if (codigo == null || descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("Código e descrição são obrigatórios");
        }
        if (diaria == null || diaria.valor().signum() <= 0) {
            throw new IllegalArgumentException("Diária deve ser maior que zero");
        }
    }
}
