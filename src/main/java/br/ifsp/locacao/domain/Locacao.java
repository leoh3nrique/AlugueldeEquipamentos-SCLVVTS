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
        this.id = UUID.randomUUID();
        this.clienteId = clienteId;
        this.periodo = periodo;
        this.itens = List.copyOf(itens);
        this.estado = EstadoLocacao.ABERTA;
    }

    public UUID getId() { return id; }
    public String getClienteId() { return clienteId; }
    public PeriodoLocacao getPeriodo() { return periodo; }
    public List<ItemLocacao> getItens() { return itens; }
    public EstadoLocacao getEstado() { return estado; }
}
