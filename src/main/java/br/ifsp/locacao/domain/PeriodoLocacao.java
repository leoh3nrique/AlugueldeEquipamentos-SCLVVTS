package br.ifsp.locacao.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record PeriodoLocacao(LocalDate inicio, LocalDate fim) {
    public PeriodoLocacao {
        if (inicio.equals(fim)) {
            throw new IllegalArgumentException("Datas inicial e final devem ser diferentes");
        }
        if (fim.isBefore(inicio)) {
            throw new IllegalArgumentException("Data final deve ser posterior à inicial");
        }
        if (ChronoUnit.DAYS.between(inicio, fim) > 30) {
            throw new IllegalArgumentException("Período não pode ultrapassar 30 dias");
        }
    }
}
