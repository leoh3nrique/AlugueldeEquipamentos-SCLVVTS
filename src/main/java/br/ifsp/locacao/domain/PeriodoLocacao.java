package br.ifsp.locacao.domain;

import java.time.LocalDate;

public record PeriodoLocacao(LocalDate inicio, LocalDate fim) {
    public PeriodoLocacao {
        if (inicio.equals(fim)) {
            throw new IllegalArgumentException("Datas inicial e final devem ser diferentes");
        }
    }
}
