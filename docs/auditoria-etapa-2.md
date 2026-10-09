# Auditoria da Etapa 2

Estado auditado: commit `c2d9e0a`. Auditoria realizada em 8 de outubro de 2026, sem alterar o código ou o histórico.

**Registro histórico:** este documento retrata aquele commit, não o estado final. Em 9 de outubro, os cenários restantes, testes funcionais, correções das issues #53 e #54, revisão de ItemLocacao, SQLite, suítes, serviços Spring e REST foram concluídos e verificados. Consulte entrega-etapa-2.md para o resultado atual. Os desvios históricos e a falta de evidência de participação equilibrada permanecem.

## Conclusão

Existe evidência real de desenvolvimento orientado por testes, mas a entrega está incompleta e o histórico contém antecipação de regras. Não se pode afirmar conformidade integral com o enunciado nem que todos os cenários tiveram um ciclo individual de falha e sucesso.

## Verificação do processo

O histórico e o registro de desenvolvimento mostram 38 cenários implementados, mais o teste pendente do cenário #45. Desses 38, dez passaram na primeira execução: #5, #16, #17, #18, #23, #35, #37, #38, #41 e #42. Os demais possuem registros de teste antes da implementação, incluindo falhas de compilação por APIs ausentes.

Foram reexecutados dois pares de commits em diretórios temporários, sem modificar a cópia de trabalho:

| Cenário | Commit do teste | Resultado reproduzido | Commit da implementação | Resultado reproduzido |
|---|---|---|---|---|
| #3 — datas iguais | `4da3294` | 1 teste, 1 falha: nenhuma exceção | `32a6420` | 2 testes passaram |
| #36 — multa | `78e860d` | 1 teste, 1 falha: esperava 40.00, recebeu zero | `c8f3682` | 3 testes de devolução passaram |

Essa reprodução é uma amostra, não uma reexecução de todos os commits históricos. Logs da auditoria: `/tmp/etapa2-auditoria-jmggivkg/`; são temporários e não integram a entrega.

A execução atual de `./mvnw test` apresentou 40 execuções: 39 passaram e o cenário #45 falhou, pois o cancelamento em andamento ainda é permitido. Há uma entrada adicional de teste porque #11 usa duas diárias inválidas.

### Antecipação inicial confirmada

O commit `8e8109c`, feito para #2, acrescentou validações de período, quantidade, duplicidade, diária e campos obrigatórios antes de seus testes específicos. O commit `24c5284` retirou essas validações para retomar a evolução incremental. Isso corrigiu o código daquela fase, mas não apaga a antecipação do histórico.

### Dependências entre cenários

- `cff5869`, para #14, introduziu a transição mínima de retirada para preparar EM_ANDAMENTO.
- `a3c09df`, para #22, introduziu cancelamento para preparar CANCELADA.
- `70617a3`, para #24, introduziu devolução completa para preparar FINALIZADA.
- `b979987`, para #27, introduziu controle de devolução parcial e prazos por equipamento antes dos testes próprios da US05.

Essas transições foram solicitadas por testes anteriores e necessárias à sua preparação. Isso não é, por si só, prova de violação de TDD. Porém, o processo fica mais difícil de revisar porque funcionalidades de histórias diferentes aparecem no mesmo commit. O enunciado não exige ordem numérica das histórias; exige teste antes da respectiva implementação e commits distintos para testes, funcionalidades e refatorações.

### Testes que passaram imediatamente

Reutilizar uma regra já existente não requer inventar falha ou modificar produção sem necessidade. Os dez casos estão registrados como tendo passado de imediato. Eles comprovam o comportamento coberto, mas não uma nova fase de falha para cada cenário. Como todos estão em classes com tag TDD, a entrega precisa explicar essa distinção, sem alegar que cada teste seguiu um ciclo independente completo.

## Requisitos da entrega

| Requisito | Evidência e situação |
|---|---|
| Base Spring Boot | Integrada, origem documentada |
| Sem JPA ou Lombok | Dependências atuais não incluem esses recursos |
| Domínio com TDD | Evidência parcial conforme análise acima |
| Cada cenário do backlog | 38 concluídos; #45 com teste falhando; #46, #47 e #49–#52 sem testes |
| DDD | Raiz, serviços e objetos de valor existem; desenho de ItemLocacao precisa ser revisto contra a proposta |
| SQLite | Configurado e driver presente; repositório concreto e persistência ausentes |
| Testes funcionais | Ausentes como conjunto identificado por critérios e tag Functional |
| Issues de defeito com bug | Não verificadas remotamente nesta auditoria; não presumir atendimento |
| Tags | UnitTest e TDD presentes; Functional ausente |
| Três suítes JUnit | Ausentes; dependência de suíte sozinha não atende ao requisito |
| Conventional commits | Commits criados nesta sessão seguem o padrão; há atualização remota anterior do README fora do padrão |
| Commits separados | Pares de teste e implementação presentes; alguns commits incluem refatoração ou transições auxiliares junto da funcionalidade |
| Serviços com @Service | Ausente |
| Controladores REST | Ausentes |
| Participação equilibrada de três integrantes | Não evidenciada: o intervalo da Etapa 2 contém 70 commits atribuídos a Leonardo Henrique e 1 a Leonardo Tiburcio; os nomes não comprovam pessoas distintas |

## Pontos de código a corrigir ou testar

- `Locacao.cancelar` altera o estado sem validação; o teste #45 reproduz esse problema.
- Cliente, código e descrição obrigatórios ainda não têm validações completas. A proposta promete que o agregado sempre estará válido.
- `ItemLocacao` é um record com igualdade por todos os campos, sem identidade de entidade explícita; devolução e prazo estão em mapas da raiz. Records não são proibidos pelo enunciado, mas esse desenho não realiza claramente a entidade interna prometida na proposta.
- A renovação reconstrói PeriodoLocacao com o limite de 30 dias da criação. É necessário esclarecer/testar a renovação de uma reserva inicial de 30 dias, pois a proposta separa duração inicial e extensão.
- A renovação direta no agregado ainda não rejeita zero ou dias negativos explicitamente; o serviço e o agregado podem produzir comportamentos diferentes.
- Devoluções com códigos inexistentes, repetidos na mesma solicitação, lista vazia ou datas inválidas precisam de testes funcionais e tratamento definido.
- Multas com frações de centavo não têm arredondamento definido.
- `bloqueiaReserva` atualmente só exclui CANCELADA; não libera itens devolvidos nem FINALIZADA. Sua semântica deve ser consolidada antes de implementá-la nas consultas SQLite.
- Os mocks verificam a reação à resposta do repositório; não comprovam cálculo de sobreposição nem liberação efetiva de reservas no banco.

Esses pontos decorrem da leitura do código, salvo o cancelamento em andamento, reproduzido pelo teste atual. Não foram apresentados como testes executados de comportamentos ainda sem casos específicos.

## Continuidade recomendada

1. Preservar datas e commits existentes; não tentar reconstruir retrospectivamente um histórico ideal.
2. Concluir #45 a partir do teste já falhando e seguir os cenários restantes com etapas reais e separadas.
3. Cobrir as lacunas por testes funcionais, registrar os defeitos encontrados e corrigir em commits próprios.
4. Revisar o desenho de ItemLocacao com testes que protejam os comportamentos e commit explícito de refatoração.
5. Implementar SQLite, suítes, serviços Spring e REST, com verificações correspondentes.
6. Documentar quais casos passaram imediatamente e quais tiveram falha antes da implementação. Garantir participação real da equipe.

Refazer tudo não é justificado pelos resultados desta auditoria. As falhas reais de processo devem ser reconhecidas, e as pendências devem ser concluídas sem fabricar evidências históricas.
