# Testes estruturais - Etapa 3

Arquivo: `src/main/java/br/ifsp/locacao/domain/Locacao.java`.

| Classe | Linha | Por que o caminho não é alcançado |
| --- | --- | --- |
| `Locacao` | 39 | O período nunca chega nulo ao construtor privado. Os construtores públicos passam por `validarPeriodoInicial`, que já rejeita esse valor na linha 34. Na reconstituição, `PeriodoLocacao.reconstituir` devolve um período válido ou lança uma exceção. Por isso, o resultado verdadeiro de `periodo == null` não é alcançado nessa linha. |
| `Locacao` | 142 | O mapa de devoluções não pode estar vazio nesse ponto. Antes de chegar aqui, o método exige pelo menos um código válido e registra sua devolução. Assim, `!datasDevolucao.isEmpty()` sempre é verdadeiro quando ainda há equipamentos pendentes. O resultado falso não é alcançado. |
