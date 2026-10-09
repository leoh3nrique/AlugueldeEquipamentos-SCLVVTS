# Entrega técnica da Etapa 2

Validação em 9 de outubro de 2026.

## Testes e critérios funcionais

| Critério | Partições ou limites | Cobertura |
|---|---|---|
| Duração inicial | -1, 0, 1, 30, 31 dias | RegrasFuncionaisTest |
| Quantidade de equipamentos | 0, 1, 5, 6 | ServicosFuncionaisTest |
| Dias de renovação | -1, 0, 1, 7, 8 | Ambas as classes funcionais; serviço não consulta disponibilidade nem salva entradas inválidas |
| Número de renovações | 0, 1, 2, tentativa de 3 | Cenários BDD e renovação funcional de 30 até 44 dias |
| Data da renovação | Antes, na data final, após | Cenários BDD e teste funcional na data final |
| Dados obrigatórios | Válidos, vazios, espaços, nulos | RegrasFuncionaisTest |
| Devolução | Completa, parcial, repetida, vazia, código desconhecido, códigos duplicados, antes da retirada | BDD e RegrasFuncionaisTest |
| Atomicidade da devolução | Lote com um item pendente e outro já devolvido | Validação de todo o lote antes de modificar qualquer item |
| Valores | No prazo, atraso, equipamentos com datas diferentes, fração de centavo | BDD e arredondamento funcional por item |
| Renovação parcial | Item devolvido conserva prazo e valor; pendente é estendido | BDD e ServicosFuncionaisTest |
| Identificador | Existente, inexistente | BDD e serviços funcionais |
| Disponibilidade | Sobreposição, períodos adjacentes, própria reserva, cancelamento, item devolvido | SqliteLocacaoRepositoryTest em banco real |
| Persistência | Fechar/reabrir, renovação parcial, período de 44 dias | SqliteLocacaoRepositoryTest |

Esses casos usam particionamento de equivalência, análise de valores limite e combinações de estados e operações. Testes funcionais recebem UnitTest e Functional; cenários do backlog recebem UnitTest e TDD. Os casos do backlog que passaram de imediato estão explicitamente registrados; a tag não representa uma fase de falha fabricada.

## Defeitos encontrados e corrigidos

- [Issue #53 — regras de domínio expostas pelos testes funcionais](https://github.com/leoh3nrique/AlugueldeEquipamentos-SCLVVTS/issues/53): os primeiros testes funcionais tiveram nove falhas e um erro. Correções incluem entradas obrigatórias, datas e lotes de devolução, renovação com zero ou valor negativo, renovação após reserva de 30 dias, liberação de equipamentos devolvidos e arredondamento de multa. Commit de correção `410531e`.
- [Issue #54 — reutilização de período estendido na criação](https://github.com/leoh3nrique/AlugueldeEquipamentos-SCLVVTS/issues/54): teste falhou antes da correção; a criação pública agora valida novamente o limite inicial de 30 dias. Commit de correção `a352c6b`.

Ambas foram abertas com label bug; os commits de correção incluem `- closes #53` e `- closes #54`. Após o push, as issues ainda apareciam abertas na verificação, então seu fechamento foi realizado explicitamente e confirmado no GitHub.

## Resultado final

- `./mvnw verify`: 81 execuções, nenhuma falha ou erro.
- TddSuite: 46 execuções dos 45 cenários BDD; #11 é parametrizado com duas diárias.
- FunctionalSuite: 31 execuções adicionais.
- UnitTestSuite: 77 execuções, selecionando apenas UnitTest.
- Integração SQLite: 4 casos, fora das suítes unitárias.
- Build gera um JAR executável Spring Boot com Java 17.

As suítes são executadas individualmente por `-Dtest`, evitando repetir os mesmos casos no build padrão.

## Verificação da API

A aplicação foi iniciada com um SQLite temporário e verificada por HTTP real. Foram exercitados criação (201), leitura e listagem (200), alteração, retirada, renovação, devolução parcial e completa, cancelamento e liberação de equipamento. Também foram verificados 400 para entradas inválidas, 404 para identificador inexistente e 409 para operação incompatível com o estado.

Uma locação renovada com devoluções em datas distintas retornou aluguel de 900.00, multa de 20.00 e total de 920.00. Após reiniciar a aplicação com o mesmo banco, identificador, criação, estado FINALIZADA, uma renovação, itens devolvidos e valores foram recuperados corretamente.

Não foram criados testes unitários de controladores, conforme a dispensa do enunciado. A verificação HTTP complementa os testes unitários de serviços e os testes com SQLite.

## Persistência e DDD

Locacao é a raiz e controla todas as operações. ItemLocacao é uma entidade interna imutável identificada pelo código; não existe repositório próprio de itens. Objetos de valor são imutáveis. Datas de devolução e prazos por item ficam encapsulados no agregado, e as coleções públicas são imutáveis.

O SQLite guarda um documento JSON do agregado completo por identificador, usando JDBC e ObjectMapper. Essa opção mantém a unidade de persistência igual à unidade do domínio. Consultas de disponibilidade leem os agregados e aplicam a regra de estado, item e sobreposição; é adequado ao pequeno módulo acadêmico, sem pretensão de otimização para grande volume. Serviços Spring transacionais e pool de uma conexão serializam as operações desta instância.

## Pendência acadêmica e histórico

A implementação técnica não comprova participação equilibrada dos três integrantes. O histórico atual não evidencia esse requisito; a equipe precisa apresentar sua participação real. Não foram alteradas autorias, datas ou commits para simular contribuições.

A antecipação inicial e as dependências entre cenários descritas na auditoria histórica continuam visíveis. As correções posteriores não transformam retrospectivamente o histórico em um processo perfeito de TDD. A avaliação acadêmica desse processo pertence ao professor.
