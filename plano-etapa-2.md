# Plano de execução — Etapa 2

Entrega: **9 de outubro de 2026**.

Este é o plano inicial, preservado como registro do início do trabalho. A situação final e os comandos de validação estão em [docs/entrega-etapa-2.md](docs/entrega-etapa-2.md).

## Situação inicial

- Proposta: `proposta-etapa-1.md`.
- Backlog: https://github.com/leoh3nrique/equipment-rental-module/issues
- São 7 histórias e 45 cenários BDD, nas issues #1 a #52.
- Base indicada no PDF: https://github.com/lucas-ifsp/demo-auth-app
- Ainda não há implementação Java neste repositório.
- Ambiente inspecionado: Java 17 disponível; Maven não instalado.

## 1. Preparar a base

- Incorporar e adaptar a base da disciplina, registrando a origem.
- A base configura Java 21 e inclui Maven Wrapper; escolher o JDK antes de executar.
- Remover dependências, configuração e código de JPA, Hibernate e Lombok: a base contém esses recursos, mas o enunciado os proíbe.
- Usar SQLite com JDBC para persistir o agregado.
- Manter o escopo aprovado, sem autenticação e pagamento real.
- Organizar pacotes de domínio, aplicação, infraestrutura e API.
- Configurar JUnit e suítes TDD, Functional e UnitTest.

## 2. Implementar cenário por cenário com TDD

Para cada cenário: escrever o teste, executá-lo e observar a falha; implementar o mínimo para passar; executar novamente; refatorar quando necessário.

| Ordem | História | Cenários | Serviço |
|---|---|---|---|
| 1 | US01 — Criar | #2–#11 | CriarLocacaoService |
| 2 | US02 — Alterar | #13–#18 | AlterarLocacaoService |
| 3 | US03 — Retirar | #20–#24 | ConfirmarRetiradaService |
| 4 | US04 — Renovar | #26–#32 | RenovarLocacaoService |
| 5 | US05 — Devolver | #34–#42 | RegistrarDevolucaoService |
| 6 | US06 — Cancelar | #44–#47 | CancelarLocacaoService |
| 7 | US07 — Consultar | #49–#52 | ConsultarLocacoesService |

Começar pelo cenário #2: cliente identificado, período válido e dois equipamentos disponíveis devem gerar uma locação com identificador único e estado ABERTA. Testar o serviço com um repositório em memória, para isolar as regras do banco e do contexto Spring.

Os testes TDD precisam de `@Tag("UnitTest")` e `@Tag("TDD")`. Cada teste deve identificar o cenário correspondente.

Preservar o agregado proposto: Locacao, ItemLocacao, PeriodoLocacao, Dinheiro, CodigoEquipamento e LocacaoRepository. Validar antes de modificar o estado; evitar setters públicos e coleções mutáveis expostas.

## 3. Ampliar os testes pela técnica funcional

Registrar os critérios e resultados esperados, com particionamento de equivalência e análise de valores limite. Exemplos:

- Duração inicial: 0, 1, 30 e 31 dias.
- Quantidade de equipamentos: 0, 1, 5 e 6.
- Renovação: 0, 1, 7 e 8 dias; 0, 1, 2 e 3 renovações.
- Devolução: no prazo, um dia atrasada e vários dias atrasada.
- Transições permitidas e proibidas para cada estado.
- Reservas com períodos separados, sobrepostos e adjacentes.

Usar `@Tag("UnitTest")` e `@Tag("Functional")`. Criar issues com label `bug` apenas para defeitos realmente encontrados; registrar reprodução e correção.

## 4. Persistência e REST

- Implementar LocacaoRepository com SQLite/JDBC, sem repositório independente para ItemLocacao.
- Persistir e recuperar o agregado completo, incluindo devoluções e renovações.
- Anotar os serviços com `@Service`.
- Expor criação, alteração, retirada, renovação, devolução, cancelamento e consulta em controladores REST.
- Tratar erros de validação, indisponibilidade e identificadores inexistentes.
- O enunciado dispensa testes dos controladores nesta etapa; priorizar os testes unitários dos serviços.

## Histórico e entrega

- Fazer commits separados para testes, funcionalidades e refatorações, usando conventional commits.
- Exemplos: `test: adiciona cenário de criação de locação`, `feat: implementa criação de locação`, `refactor: extrai validação de período`.
- Ao resolver uma issue, incluir `- closes #<numero>` na mensagem do commit correspondente.
- Distribuir histórias e revisões entre os três integrantes; o histórico deve evidenciar participação equilibrada com autoria real.
- Documentar como iniciar a API, configurar o SQLite e executar cada suíte.
- Antes da entrega, conferir todos os 45 cenários, os testes funcionais adicionais e as três suítes.

## Decisões a alinhar antes dos cenários afetados

- Se reservas adjacentes compartilham a data final ou podem usar o mesmo equipamento.
- Como calcular e arredondar centavos de multa.
- Como preservar o prazo e o valor de itens já devolvidos quando a locação é renovada parcialmente (cenário #27).
- Qual campo define a ordenação de locações da mais recente para a mais antiga.
