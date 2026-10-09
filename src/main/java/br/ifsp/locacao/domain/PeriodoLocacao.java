package br.ifsp.locacao.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public final class PeriodoLocacao {
    private final LocalDate inicio;
    private final LocalDate fim;
    public PeriodoLocacao(LocalDate inicio, LocalDate fim) { this(inicio, fim, 30); }
    private PeriodoLocacao(LocalDate inicio, LocalDate fim, int limite) {
        if (inicio == null || fim == null) throw new IllegalArgumentException("Datas são obrigatórias");
        if (inicio.equals(fim)) throw new IllegalArgumentException("Datas inicial e final devem ser diferentes");
        if (fim.isBefore(inicio)) throw new IllegalArgumentException("Data final deve ser posterior à inicial");
        if (ChronoUnit.DAYS.between(inicio, fim) > limite) throw new IllegalArgumentException("Período não pode ultrapassar " + limite + " dias");
        this.inicio = inicio; this.fim = fim;
    }
    public PeriodoLocacao estender(int dias) { return new PeriodoLocacao(inicio, fim.plusDays(dias), 44); }
    public static PeriodoLocacao reconstituir(LocalDate inicio, LocalDate fim) { return new PeriodoLocacao(inicio, fim, 44); }
    public LocalDate inicio() { return inicio; }
    public LocalDate fim() { return fim; }
    public boolean sobrepoe(PeriodoLocacao outro) { return inicio.isBefore(outro.fim) && outro.inicio.isBefore(fim); }
    @Override public boolean equals(Object o) { return o instanceof PeriodoLocacao p && inicio.equals(p.inicio) && fim.equals(p.fim); }
    @Override public int hashCode() { return Objects.hash(inicio, fim); }
}
