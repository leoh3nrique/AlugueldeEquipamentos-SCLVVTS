# Registro do processo TDD — Etapa 2

## Cenário #2: criação de locação válida

1. O teste foi escrito antes das classes de domínio e do serviço. A execução falhou na compilação pela ausência dessas classes.
2. A implementação foi adicionada e o teste passou.
3. Na revisão, foram identificadas validações antecipadas sem seus respectivos testes: período, quantidade de itens, duplicidade, diária, campos obrigatórios e indisponibilidade.
4. Essas validações foram retiradas em um novo commit, preservando o histórico. O teste mantém a verificação do identificador único, cliente, período, itens, estado ABERTA e envio ao repositório. A exigência de chamadas à consulta de disponibilidade foi retirada deste cenário; a rejeição por indisponibilidade será tratada no cenário #10.

A versão atual é incremental: ainda não garante todas as regras da proposta. Cada regra será acrescentada após escrever e executar seu teste correspondente. A correção não altera o fato de que a primeira implementação antecipou validações.

## Cenário #3: datas inicial e final iguais

1. Adicionado teste do serviço esperando rejeição e nenhuma interação com o repositório.
2. Executado somente esse teste: 1 falha, pois nenhuma exceção foi lançada.
3. Criado commit do teste antes da implementação.
4. Adicionada validação de datas iguais no construtor de PeriodoLocacao.
5. Executados todos os testes existentes: 2 testes passaram, sem falhas.

## Cenário #4: data final anterior à inicial

1. Adicionado teste esperando rejeição e nenhuma interação com o repositório.
2. Executado somente esse teste: 1 falha, pois nenhuma exceção foi lançada.
3. Criado commit do teste antes da implementação.
4. Adicionada validação de data final anterior à inicial em PeriodoLocacao.
5. Executados todos os testes existentes: 3 testes passaram, sem falhas.

## Cenário #5: período de exatamente 30 dias

1. Adicionado teste de criação com data final igual à inicial mais 30 dias.
2. Executados todos os testes existentes: 4 testes passaram, sem falhas.
3. O novo teste passou na primeira execução, pois a implementação já aceitava esse período. Não houve fase de falha nem alteração de produção neste cenário; o teste registra o comportamento e protege o limite válido para a próxima regra.

## Cenário #6: período superior a 30 dias

1. Adicionado teste de criação com período de 31 dias, esperando rejeição e nenhuma interação com o repositório.
2. Executado somente esse teste: 1 falha, pois nenhuma exceção foi lançada.
3. Criado commit do teste antes da implementação.
4. Adicionada validação da duração máxima em PeriodoLocacao.
5. Executado `./mvnw verify`: 5 testes passaram, sem falhas, e o pacote da aplicação foi gerado. O cenário #5 continua garantindo a aceitação de exatamente 30 dias.

## Cenário #7: locação sem equipamentos

1. Adicionado teste de criação com lista vazia, esperando rejeição e nenhuma interação com o repositório.
2. Executado somente esse teste: 1 falha, pois nenhuma exceção foi lançada.
3. Criado commit do teste antes da implementação.
4. Adicionada validação de pelo menos um equipamento no construtor de Locacao.
5. Executados todos os testes existentes: 6 testes passaram, sem falhas.

## Cenário #8: locação com seis equipamentos — fase de falha

1. Adicionado teste de criação com seis equipamentos diferentes, esperando rejeição e nenhuma interação com o repositório.
2. Executado `./mvnw test`: 7 testes executados, 6 passaram e apenas o cenário #8 falhou porque nenhuma exceção foi lançada.
3. Registrado o teste em commit separado. A implementação do limite máximo ainda não foi adicionada; a issue #8 permanece pendente.

## Cenário #8: implementação do limite máximo

1. Em um passo posterior ao commit do teste, adicionada validação para rejeitar mais de cinco equipamentos no construtor de Locacao.
2. Executado `./mvnw test`: 7 testes passaram, sem falhas.
3. Implementação registrada em commit separado, com referência de fechamento da issue #8.

## Cenário #9: códigos de equipamento repetidos — fase de falha

1. Adicionado teste com dois itens de mesmo código, mas descrições e diárias diferentes, para verificar a duplicidade pela identidade do equipamento.
2. Executado `./mvnw test`: 8 testes executados, 7 passaram e apenas o cenário #9 falhou porque nenhuma exceção foi lançada.
3. Teste registrado em commit separado, sem implementar a regra de duplicidade.

Próximo passo: implementar a rejeição de códigos repetidos do cenário #9.
