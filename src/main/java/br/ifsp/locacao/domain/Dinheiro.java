package br.ifsp.locacao.domain;
import java.math.BigDecimal;
import java.math.RoundingMode;
public record Dinheiro(BigDecimal valor) {
    public Dinheiro {
        if (valor == null) throw new IllegalArgumentException("Valor monetário é obrigatório");
        valor = valor.setScale(2, RoundingMode.HALF_UP);
    }
}
