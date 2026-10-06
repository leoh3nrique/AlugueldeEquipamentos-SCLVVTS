package br.ifsp.locacao.domain;

import java.time.LocalDate;

public record PeriodoLocacao(LocalDate inicio, LocalDate fim) {
}
