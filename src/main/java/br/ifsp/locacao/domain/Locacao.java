package br.ifsp.locacao.domain;

import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;

public class Locacao {
    private final UUID id;
    private final String clienteId;
    private PeriodoLocacao periodo;
    private List<ItemLocacao> itens;
    private EstadoLocacao estado;
    private int quantidadeRenovacoes;
    private final Map<CodigoEquipamento, LocalDate> datasDevolucao = new HashMap<>();
    private final Map<CodigoEquipamento, LocalDate> prazosDosDevolvidos = new HashMap<>();

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

    public void cancelar() {
        this.estado = EstadoLocacao.CANCELADA;
    }

    public boolean bloqueiaReserva(CodigoEquipamento codigo) {
        return estado != EstadoLocacao.CANCELADA
                && itens.stream().anyMatch(item -> item.codigo().equals(codigo));
    }

    public void confirmarRetirada(LocalDate dataRetirada) {
        if (estado != EstadoLocacao.ABERTA) {
            throw new IllegalStateException("Somente locações abertas podem ter a retirada confirmada");
        }
        if (dataRetirada.isBefore(periodo.inicio())) {
            throw new IllegalArgumentException("Retirada não pode ocorrer antes da data inicial");
        }
        this.estado = EstadoLocacao.EM_ANDAMENTO;
    }

    public void renovar(int diasAdicionais, LocalDate dataSolicitacao) {
        if (estado != EstadoLocacao.EM_ANDAMENTO
                && estado != EstadoLocacao.PARCIALMENTE_DEVOLVIDA) {
            throw new IllegalStateException(
                    "Somente locações em andamento ou parcialmente devolvidas podem ser renovadas");
        }
        if (!dataSolicitacao.isBefore(periodo.fim())) {
            throw new IllegalArgumentException("Renovação deve ser solicitada antes da data final");
        }
        if (quantidadeRenovacoes >= 2) {
            throw new IllegalStateException("Locação não pode ser renovada mais de duas vezes");
        }
        if (diasAdicionais > 7) {
            throw new IllegalArgumentException("Renovação não pode acrescentar mais de sete dias");
        }
        PeriodoLocacao novoPeriodo = new PeriodoLocacao(
                periodo.inicio(), periodo.fim().plusDays(diasAdicionais));
        this.periodo = novoPeriodo;
        this.quantidadeRenovacoes++;
    }

    public void registrarDevolucao(List<CodigoEquipamento> codigos, LocalDate dataDevolucao) {
        if (estado != EstadoLocacao.EM_ANDAMENTO
                && estado != EstadoLocacao.PARCIALMENTE_DEVOLVIDA) {
            throw new IllegalStateException(
                    "Somente locações em andamento ou parcialmente devolvidas podem receber devoluções");
        }
        for (CodigoEquipamento codigo : codigos) {
            if (estaDevolvido(codigo)) {
                throw new IllegalArgumentException("Equipamento já foi devolvido");
            }
        }
        for (ItemLocacao item : itens) {
            if (codigos.contains(item.codigo())) {
                datasDevolucao.put(item.codigo(), dataDevolucao);
                prazosDosDevolvidos.put(item.codigo(), periodo.fim());
            }
        }
        boolean todosEquipamentosDevolvidos = itens.stream()
                .allMatch(item -> estaDevolvido(item.codigo()));
        if (todosEquipamentosDevolvidos) {
            this.estado = EstadoLocacao.FINALIZADA;
        } else if (!datasDevolucao.isEmpty()) {
            this.estado = EstadoLocacao.PARCIALMENTE_DEVOLVIDA;
        }
    }

    public boolean estaDevolvido(CodigoEquipamento codigo) {
        return datasDevolucao.containsKey(codigo);
    }

    public LocalDate getFimContratado(CodigoEquipamento codigo) {
        return prazosDosDevolvidos.getOrDefault(codigo, periodo.fim());
    }

    public void alterar(PeriodoLocacao novoPeriodo, List<ItemLocacao> novosItens) {
        if (estado != EstadoLocacao.ABERTA) {
            throw new IllegalStateException("Somente locações abertas podem ser alteradas");
        }
        validarItens(novosItens);
        List<ItemLocacao> copia = List.copyOf(novosItens);
        this.periodo = novoPeriodo;
        this.itens = copia;
    }

    public Dinheiro getValorNormal() {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemLocacao item : itens) {
            long diasContratados = ChronoUnit.DAYS.between(
                    periodo.inicio(), getFimContratado(item.codigo()));
            total = total.add(item.diaria().valor().multiply(BigDecimal.valueOf(diasContratados)));
        }
        return new Dinheiro(total);
    }

    public Dinheiro getMulta() {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemLocacao item : itens) {
            if (estaDevolvido(item.codigo())) {
                long diasAtraso = ChronoUnit.DAYS.between(
                        getFimContratado(item.codigo()), datasDevolucao.get(item.codigo()));
                if (diasAtraso > 0) {
                    BigDecimal multaItem = item.diaria().valor()
                            .multiply(new BigDecimal("0.20"))
                            .multiply(BigDecimal.valueOf(diasAtraso));
                    total = total.add(multaItem);
                }
            }
        }
        return new Dinheiro(total);
    }

    public Dinheiro getValorTotal() {
        return new Dinheiro(getValorNormal().valor().add(getMulta().valor()));
    }

    public UUID getId() { return id; }
    public String getClienteId() { return clienteId; }
    public PeriodoLocacao getPeriodo() { return periodo; }
    public List<ItemLocacao> getItens() { return itens; }
    public EstadoLocacao getEstado() { return estado; }
    public int getQuantidadeRenovacoes() { return quantidadeRenovacoes; }
}
