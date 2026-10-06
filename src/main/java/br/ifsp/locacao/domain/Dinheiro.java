package br.ifsp.locacao.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Dinheiro(BigDecimal valor) {
    public Dinheiro {
        if (valor == null || valor.signum() < 0) {
            throw new IllegalArgumentException("Valor monetário deve ser não negativo");
        }
        valor = valor.setScale(2, RoundingMode.UNNECESSARY);
    }
}
