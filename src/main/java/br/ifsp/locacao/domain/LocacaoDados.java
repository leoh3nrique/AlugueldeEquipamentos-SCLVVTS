package br.ifsp.locacao.domain;
import java.time.*;
import java.math.BigDecimal;
import java.util.*;
/** Representação de persistência do agregado; não oferece mutação do domínio. */
public record LocacaoDados(UUID id, String clienteId, Instant criadaEm, LocalDate inicio,
        LocalDate fim, EstadoLocacao estado, int renovacoes, LocalDate retirada, List<ItemDados> itens) {
    public record ItemDados(String codigo, String descricao, BigDecimal diaria, LocalDate devolucao, LocalDate fimContratado) {}
}
