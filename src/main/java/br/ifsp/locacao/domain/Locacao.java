package br.ifsp.locacao.domain;

import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;

public class Locacao {
    private final UUID id;
    private final String clienteId;
    private final Instant criadaEm;
    private PeriodoLocacao periodo;
    private List<ItemLocacao> itens;
    private EstadoLocacao estado;
    private int quantidadeRenovacoes;
    private LocalDate dataRetirada;
    private final Map<CodigoEquipamento, LocalDate> datasDevolucao = new HashMap<>();
    private final Map<CodigoEquipamento, LocalDate> prazosDosDevolvidos = new HashMap<>();

    public Locacao(String clienteId, PeriodoLocacao periodo, List<ItemLocacao> itens) {
        this(clienteId, periodo, itens, Clock.systemUTC());
    }

    public Locacao(String clienteId, PeriodoLocacao periodo, List<ItemLocacao> itens, Clock clock) {
        this(clienteId, validarPeriodoInicial(periodo), itens, clock, UUID.randomUUID());
    }

    private static PeriodoLocacao validarPeriodoInicial(PeriodoLocacao periodo) {
        if (periodo == null) throw new IllegalArgumentException("Período é obrigatório");
        return new PeriodoLocacao(periodo.inicio(), periodo.fim());
    }

    private Locacao(String clienteId, PeriodoLocacao periodo, List<ItemLocacao> itens, Clock clock, UUID id) {
        if (clienteId == null || clienteId.isBlank() || periodo == null || clock == null) throw new IllegalArgumentException("Cliente, período e relógio são obrigatórios");
        validarItens(itens);
        this.id = id;
        this.clienteId = clienteId;
        this.criadaEm = clock.instant();
        this.periodo = periodo;
        this.itens = List.copyOf(itens);
        this.estado = EstadoLocacao.ABERTA;
    }

    private static void validarItens(List<ItemLocacao> itens) {
        if (itens == null || itens.stream().anyMatch(java.util.Objects::isNull)) throw new IllegalArgumentException("Itens são obrigatórios");
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
        if (estado != EstadoLocacao.ABERTA) {
            throw new IllegalStateException("Somente locações abertas podem ser canceladas");
        }
        this.estado = EstadoLocacao.CANCELADA;
    }

    public boolean bloqueiaReserva(CodigoEquipamento codigo) {
        return estado != EstadoLocacao.CANCELADA && estado != EstadoLocacao.FINALIZADA
                && !estaDevolvido(codigo)
                && itens.stream().anyMatch(item -> item.codigo().equals(codigo));
    }

    public void confirmarRetirada(LocalDate dataRetirada) {
        if (estado != EstadoLocacao.ABERTA) {
            throw new IllegalStateException("Somente locações abertas podem ter a retirada confirmada");
        }
        if (dataRetirada == null) throw new IllegalArgumentException("Data de retirada é obrigatória");
        if (dataRetirada.isBefore(periodo.inicio())) {
            throw new IllegalArgumentException("Retirada não pode ocorrer antes da data inicial");
        }
        this.dataRetirada = dataRetirada;
        this.estado = EstadoLocacao.EM_ANDAMENTO;
    }

    public void validarRenovacao(int diasAdicionais, LocalDate dataSolicitacao) {
        if (estado != EstadoLocacao.EM_ANDAMENTO
                && estado != EstadoLocacao.PARCIALMENTE_DEVOLVIDA) {
            throw new IllegalStateException(
                    "Somente locações em andamento ou parcialmente devolvidas podem ser renovadas");
        }
        if (dataSolicitacao == null) throw new IllegalArgumentException("Data da solicitação é obrigatória");
        if (dataSolicitacao.isBefore(dataRetirada)) throw new IllegalArgumentException("Solicitação não pode anteceder a retirada");
        if (!dataSolicitacao.isBefore(periodo.fim())) {
            throw new IllegalArgumentException("Renovação deve ser solicitada antes da data final");
        }
        if (quantidadeRenovacoes >= 2) {
            throw new IllegalStateException("Locação não pode ser renovada mais de duas vezes");
        }
        if (diasAdicionais < 1) throw new IllegalArgumentException("Renovação deve acrescentar pelo menos um dia");
        if (diasAdicionais > 7) {
            throw new IllegalArgumentException("Renovação não pode acrescentar mais de sete dias");
        }
    }

    public void renovar(int diasAdicionais, LocalDate dataSolicitacao) {
        validarRenovacao(diasAdicionais, dataSolicitacao);
        PeriodoLocacao novoPeriodo = periodo.estender(diasAdicionais);
        this.periodo = novoPeriodo;
        this.quantidadeRenovacoes++;
    }

    public void registrarDevolucao(List<CodigoEquipamento> codigos, LocalDate dataDevolucao) {
        if (estado != EstadoLocacao.EM_ANDAMENTO
                && estado != EstadoLocacao.PARCIALMENTE_DEVOLVIDA) {
            throw new IllegalStateException(
                    "Somente locações em andamento ou parcialmente devolvidas podem receber devoluções");
        }
        if (dataDevolucao == null || dataDevolucao.isBefore(dataRetirada)) throw new IllegalArgumentException("Devolução não pode anteceder a retirada");
        if (codigos == null || codigos.isEmpty() || codigos.stream().anyMatch(java.util.Objects::isNull) || codigos.stream().distinct().count() != codigos.size()) throw new IllegalArgumentException("Informe equipamentos distintos para devolver");
        for (CodigoEquipamento codigo : codigos) {
            if (itens.stream().noneMatch(i -> i.codigo().equals(codigo))) throw new IllegalArgumentException("Equipamento não pertence à locação");
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
        if (novoPeriodo == null) throw new IllegalArgumentException("Período é obrigatório");
        new PeriodoLocacao(novoPeriodo.inicio(), novoPeriodo.fim());
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
                    total = total.add(new Dinheiro(multaItem).valor());
                }
            }
        }
        return new Dinheiro(total);
    }

    public Dinheiro getValorTotal() {
        return new Dinheiro(getValorNormal().valor().add(getMulta().valor()));
    }

    public LocacaoDados dados() {
        return new LocacaoDados(id, clienteId, criadaEm, periodo.inicio(), periodo.fim(), estado,
                quantidadeRenovacoes, dataRetirada, itens.stream().map(i -> new LocacaoDados.ItemDados(
                        i.codigo().valor(), i.descricao(), i.diaria().valor(), datasDevolucao.get(i.codigo()),
                        getFimContratado(i.codigo()))).toList());
    }

    public static Locacao reconstituir(LocacaoDados dados) {
        List<ItemLocacao> itens = dados.itens().stream().map(i -> new ItemLocacao(
                new CodigoEquipamento(i.codigo()), i.descricao(), new Dinheiro(i.diaria()))).toList();
        Locacao l = new Locacao(dados.clienteId(), PeriodoLocacao.reconstituir(dados.inicio(), dados.fim()),
                itens, Clock.fixed(dados.criadaEm(), java.time.ZoneOffset.UTC), dados.id());
        l.estado = dados.estado(); l.quantidadeRenovacoes = dados.renovacoes(); l.dataRetirada = dados.retirada();
        for (LocacaoDados.ItemDados i : dados.itens()) {
            if (i.devolucao() != null) {
                CodigoEquipamento c = new CodigoEquipamento(i.codigo());
                l.datasDevolucao.put(c, i.devolucao()); l.prazosDosDevolvidos.put(c, i.fimContratado());
            }
        }
        return l;
    }

    public UUID getId() { return id; }
    public Instant getCriadaEm() { return criadaEm; }
    public String getClienteId() { return clienteId; }
    public PeriodoLocacao getPeriodo() { return periodo; }
    public List<ItemLocacao> getItens() { return itens; }
    public EstadoLocacao getEstado() { return estado; }
    public int getQuantidadeRenovacoes() { return quantidadeRenovacoes; }
}
