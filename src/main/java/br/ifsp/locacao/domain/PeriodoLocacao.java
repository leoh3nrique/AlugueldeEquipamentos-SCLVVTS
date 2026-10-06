package br.ifsp.locacao.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record PeriodoLocacao(LocalDate inicio, LocalDate fim) {
    public PeriodoLocacao {
        if (inicio == null || fim == null) {
            throw new IllegalArgumentException("Datas do período são obrigatórias");
        }
        long dias = ChronoUnit.DAYS.between(inicio, fim);
        if (dias < 1 || dias > 30) {
            throw new IllegalArgumentException("Período deve ter entre 1 e 30 dias");
        }
    }
}
