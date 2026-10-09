package br.ifsp.locacao.domain;
public record CodigoEquipamento(String valor) {
    public CodigoEquipamento {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException("Código é obrigatório");
        valor = valor.trim();
    }
}
