package br.ifsp.locacao.domain;

import java.util.List;
import java.util.UUID;

public class Locacao {
    private final UUID id;
    private final String clienteId;
    private PeriodoLocacao periodo;
    private List<ItemLocacao> itens;
    private final EstadoLocacao estado;

    public Locacao(String clienteId, PeriodoLocacao periodo, List<ItemLocacao> itens) {
        validarItens(itens);
        this.id = UUID.randomUUID();
        this.clienteId = clienteId;
        this.periodo = periodo;
        this.itens = List.copyOf(itens);
        this.estado = EstadoLocacao.ABERTA;
    }

    private static void validarItens(List<ItemLocacao> itens) {
        if (itens.isEmpty()) {
            throw new IllegalArgumentException("Locação deve possuir pelo menos um equipamento");
        }
        if (itens.size() > 5) {
            throw new IllegalArgumentException("Locação não pode possuir mais de cinco equipamentos");
        }
        long quantidadeCodigos = itens.stream()
                .map(ItemLocacao::codigo)
                .distinct()
                .count();
        if (quantidadeCodigos != itens.size()) {
            throw new IllegalArgumentException("Equipamento não pode aparecer mais de uma vez na locação");
        }
    }

    public void alterar(PeriodoLocacao novoPeriodo, List<ItemLocacao> novosItens) {
        validarItens(novosItens);
        List<ItemLocacao> copia = List.copyOf(novosItens);
        this.periodo = novoPeriodo;
        this.itens = copia;
    }

    public UUID getId() { return id; }
    public String getClienteId() { return clienteId; }
    public PeriodoLocacao getPeriodo() { return periodo; }
    public List<ItemLocacao> getItens() { return itens; }
    public EstadoLocacao getEstado() { return estado; }
}
