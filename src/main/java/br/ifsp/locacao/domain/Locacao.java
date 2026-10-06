package br.ifsp.locacao.domain;

import java.util.List;
import java.util.UUID;

public class Locacao {
    private final UUID id;
    private final String clienteId;
    private final PeriodoLocacao periodo;
    private final List<ItemLocacao> itens;
    private final EstadoLocacao estado;

    public Locacao(String clienteId, PeriodoLocacao periodo, List<ItemLocacao> itens) {
        if (clienteId == null || clienteId.isBlank() || periodo == null) {
            throw new IllegalArgumentException("Cliente e período são obrigatórios");
        }
        if (itens == null || itens.isEmpty() || itens.size() > 5) {
            throw new IllegalArgumentException("Locação deve ter entre 1 e 5 equipamentos");
        }
        List<ItemLocacao> copia = List.copyOf(itens);
        if (copia.stream().map(ItemLocacao::codigo).distinct().count() != copia.size()) {
            throw new IllegalArgumentException("Equipamento repetido na locação");
        }
        this.id = UUID.randomUUID();
        this.clienteId = clienteId;
        this.periodo = periodo;
        this.itens = copia;
        this.estado = EstadoLocacao.ABERTA;
    }

    public UUID getId() { return id; }
    public String getClienteId() { return clienteId; }
    public PeriodoLocacao getPeriodo() { return periodo; }
    public List<ItemLocacao> getItens() { return itens; }
    public EstadoLocacao getEstado() { return estado; }
}
