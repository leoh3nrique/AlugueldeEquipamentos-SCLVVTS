# Testes estruturais - Etapa 3

A suíte unitária passou com 105 testes. A cobertura foi conferida no IntelliJ e no JaCoCo. Nos dois relatórios, ficaram dois caminhos sem cobertura em `Locacao`. Os motivos estão na tabela abaixo.

Arquivo: `src/main/java/br/ifsp/locacao/domain/Locacao.java`.

| Classe | Linha | Por que o caminho não é alcançado |
| --- | --- | --- |
| `Locacao` | 39 | O período nunca chega nulo ao construtor privado. Os construtores públicos passam por `validarPeriodoInicial`, que já rejeita esse valor na linha 34. Na reconstituição, `PeriodoLocacao.reconstituir` devolve um período válido ou lança uma exceção. Por isso, o resultado verdadeiro de `periodo == null` não é alcançado nessa linha. |
| `Locacao` | 142 | O mapa de devoluções não pode estar vazio nesse ponto. Antes de chegar aqui, o método exige pelo menos um código válido e registra sua devolução. Assim, `!datasDevolucao.isEmpty()` sempre é verdadeiro quando ainda há equipamentos pendentes. O resultado falso não é alcançado. |

## Conferência no IntelliJ

Em 6 de novembro de 2026, a `UnitTestSuite` rodou com **Run with Coverage** no IntelliJ. Os 105 testes passaram.

| Pacote | Branches cobertos |
| --- | --- |
| `application` | 20/20 (100%) |
| `domain` | 128/130 (98%) |

Os contadores de `Locacao` confirmaram os dois casos da tabela: na linha 39, `periodo == null` teve zero ocorrências verdadeiras; na linha 142, `!datasDevolucao.isEmpty()` teve zero ocorrências falsas.

## Conferência com JaCoCo

Comando usado em 6 de novembro de 2026 para rodar a suíte e gerar uma nova medição:

```bash
./mvnw org.jacoco:jacoco-maven-plugin:0.8.12:prepare-agent \
  -Djacoco.append=false -Dtest=UnitTestSuite test \
  org.jacoco:jacoco-maven-plugin:0.8.12:report
```

Resultado: 105 testes passaram, sem falhas ou erros. Foram cobertos 156 dos 158 branches do domínio e dos serviços. Os dois restantes são os casos da tabela. O relatório fica em `target/site/jacoco/index.html`.
