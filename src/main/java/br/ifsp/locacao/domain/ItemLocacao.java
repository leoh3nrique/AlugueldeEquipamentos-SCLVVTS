package br.ifsp.locacao.domain;

/** Entidade interna imutável, identificada pelo código dentro do agregado. */
public final class ItemLocacao {
    private final CodigoEquipamento codigo;
    private final String descricao;
    private final Dinheiro diaria;
    public ItemLocacao(CodigoEquipamento codigo, String descricao, Dinheiro diaria) {
        if (codigo == null || descricao == null || descricao.isBlank()) throw new IllegalArgumentException("Código e descrição são obrigatórios");
        if (diaria == null || diaria.valor().signum() <= 0) throw new IllegalArgumentException("Diária deve ser maior que zero");
        this.codigo = codigo; this.descricao = descricao.trim(); this.diaria = diaria;
    }
    public CodigoEquipamento codigo() { return codigo; }
    public String descricao() { return descricao; }
    public Dinheiro diaria() { return diaria; }
    @Override public boolean equals(Object o) { return o instanceof ItemLocacao i && codigo.equals(i.codigo); }
    @Override public int hashCode() { return codigo.hashCode(); }
}
