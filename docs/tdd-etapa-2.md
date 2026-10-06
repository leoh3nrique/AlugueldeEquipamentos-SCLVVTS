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

Próximo cenário: #4, rejeitar data final anterior à inicial.
