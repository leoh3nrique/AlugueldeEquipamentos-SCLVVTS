# Mutantes equivalentes

## `br.ifsp.locacao.domain.Locacao`

| Linha | ID do mutante | Justificativa |
| --- | --- | --- |
| 183 | `getMulta() / ConditionalsBoundaryMutator / índice 51` | Troca `diasAtraso > 0` por `diasAtraso >= 0`. A diferença ocorre apenas quando o atraso é zero: o cálculo acrescenta `0.00` à multa, sem alterar o resultado ou o estado da locação. Para atrasos positivos ou negativos, o comportamento é o mesmo. |
