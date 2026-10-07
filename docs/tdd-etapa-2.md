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

## Cenário #9: implementação da rejeição de códigos repetidos

1. Em um passo posterior ao commit do teste, adicionada validação de códigos distintos no construtor de Locacao.
2. Executado `./mvnw test`: 8 testes passaram, sem falhas.
3. Implementação registrada em commit separado, com referência de fechamento da issue #9.

## Cenário #10: equipamento reservado — fase de falha

1. Adicionado teste com dois equipamentos: o primeiro disponível e o segundo reservado, conforme resposta simulada do repositório para o período solicitado.
2. O teste exige rejeição por indisponibilidade e que a locação não seja salva.
3. Executado `./mvnw test`: 9 testes executados, 8 passaram e apenas o cenário #10 falhou porque nenhuma exceção foi lançada.
4. Teste registrado em commit separado, sem implementar a consulta de disponibilidade no serviço. O cálculo de sobreposição será verificado ao implementar o repositório.

## Cenário #10: implementação da verificação de disponibilidade

1. Em um passo posterior ao commit do teste, adicionada consulta ao repositório para cada equipamento antes de salvar a locação.
2. Se algum equipamento estiver reservado, o serviço rejeita a operação sem salvar o agregado.
3. Executado `./mvnw test`: 9 testes passaram, sem falhas.
4. Implementação registrada em commit separado, com referência de fechamento da issue #10. A verificação usa a interface do repositório; persistência e cálculo de sobreposição continuam pendentes.

## Cenário #11: diária igual ou inferior a zero — fase de falha

1. Adicionado teste parametrizado para diárias de 0.00 e -1.00, esperando rejeição e nenhuma interação com o repositório.
2. Executado `./mvnw test`: 11 execuções, 9 passaram e as duas entradas do cenário #11 falharam porque nenhuma exceção foi lançada.
3. Teste registrado em commit separado, sem implementar a validação da diária.

## Cenário #11: implementação da validação da diária

1. Em um passo posterior ao commit do teste, adicionada validação no construtor de ItemLocacao para rejeitar diárias iguais ou inferiores a zero.
2. Executado `./mvnw test`: 11 execuções passaram, sem falhas.
3. Implementação registrada em commit separado, com referência de fechamento da issue #11.

Os dez cenários BDD de criação da US01 (#2 a #11) estão cobertos. Isso não encerra os requisitos adicionais de testes funcionais, persistência e API da etapa.

## Cenário #13: alteração de locação aberta — fase de falha

1. Adicionado teste de alteração de período e equipamentos, mantendo o identificador, o cliente e o estado ABERTA.
2. O teste mantém um equipamento original e acrescenta outro. A consulta de disponibilidade recebe o identificador da própria locação para que sua reserva não seja tratada como conflito.
3. Executado `./mvnw -Dtest=AlterarLocacaoServiceTest test`: falha na compilação, pois AlterarLocacaoService, buscarPorId e a consulta de disponibilidade com exclusão da própria locação ainda não existem. Nenhum teste foi executado nessa tentativa.
4. Teste registrado em commit separado, sem adicionar implementação de produção.

## Cenário #13: implementação da alteração

1. Adicionados AlterarLocacaoService, busca por identificador e contrato de consulta de disponibilidade excluindo a própria locação.
2. A alteração ocorre pela raiz Locacao, preservando identificador, cliente e estado. As validações de itens já existentes foram compartilhadas com o construtor, e a lista continua protegida contra alteração externa.
3. Executado `./mvnw test`: 12 execuções passaram, sem falhas.
4. Implementação registrada em commit separado, com referência de fechamento da issue #13.

Esta implementação cobre o caso válido. A rejeição por estado será introduzida pelo cenário #14; a consulta de disponibilidade já é chamada, mas sua resposta será tratada no cenário #15, após o teste de conflito. O repositório concreto permanece pendente.

## Cenário #14: alteração de locação em andamento — fase de falha

1. Adicionado teste que confirma a retirada para preparar uma locação EM_ANDAMENTO e tenta alterar período e equipamentos.
2. O teste exige rejeição, preservação do período, itens e estado originais, e ausência de salvamento.
3. Executado `./mvnw -Dtest=AlterarLocacaoServiceTest#deveRejeitarAlteracaoDeLocacaoEmAndamento test`: falha na compilação pela ausência de confirmarRetirada(LocalDate). Nenhum teste foi executado nessa tentativa.
4. Teste registrado em commit separado, sem implementação de produção. No próximo passo será necessária a transição mínima para EM_ANDAMENTO usada na preparação; os demais cenários de retirada da US03 continuam pendentes.

## Cenário #14: implementação da rejeição por estado

1. Adicionada a transição mínima confirmarRetirada(LocalDate) para preparar EM_ANDAMENTO, sem antecipar as validações de retirada da US03.
2. Executado o teste do cenário #14: 1 falha, pois a alteração ainda não lançava exceção.
3. Adicionada verificação de estado em Locacao.alterar, antes de modificar período e itens.
4. Executado `./mvnw test`: 13 execuções passaram, sem falhas.
5. Implementação registrada em commit separado, com referência de fechamento da issue #14.

O método de retirada ainda não valida data ou estado; essas regras continuam pendentes dos cenários da US03.

## Cenário #15: conflito de reserva no novo período — fase de falha

1. Adicionado teste de alteração apenas do período de uma locação aberta com dois equipamentos. O repositório simula conflito para o segundo equipamento, excluindo a própria locação da consulta.
2. O teste exige rejeição sem salvar ou modificar o período, os itens, o identificador e o estado originais.
3. Executado `./mvnw test`: 14 execuções, 13 passaram e apenas o cenário #15 falhou porque nenhuma exceção foi lançada.
4. Teste registrado em commit separado, sem modificar a implementação.

## Cenário #15: implementação da rejeição por indisponibilidade

1. Em um passo posterior ao commit do teste, o serviço passou a rejeitar a alteração quando a consulta de disponibilidade indicar conflito.
2. A rejeição ocorre antes de alterar o agregado ou salvá-lo, preservando os dados originais.
3. Executado `./mvnw test`: 14 execuções passaram, sem falhas.
4. Implementação registrada em commit separado, com referência de fechamento da issue #15.

## Cenário #16: alteração para período superior a 30 dias

1. Adicionado teste tentando alterar uma locação aberta para um período de 31 dias, esperando rejeição, dados originais preservados e nenhuma interação com o repositório.
2. Executado `./mvnw test`: 15 execuções passaram, sem falhas.
3. O teste passou na primeira execução: PeriodoLocacao já rejeita mais de 30 dias desde o cenário #6. A exceção ocorre ao construir o novo período, antes da chamada ao serviço; não houve fase de falha nem alteração de produção neste cenário.
4. Teste registrado em commit próprio, com referência de fechamento da issue #16.

## Cenário #17: adição de um sexto equipamento

1. Adicionado teste que cria uma locação aberta com cinco equipamentos e tenta adicionar um sexto, mantendo os itens originais na solicitação.
2. O teste exige rejeição, preservação dos dados originais e ausência de salvamento.
3. Executado `./mvnw test`: 16 execuções passaram, sem falhas.
4. O teste passou na primeira execução: a validação do limite de cinco equipamentos já existe em Locacao e é compartilhada entre criação e alteração. Não houve fase de falha nem alteração de produção neste cenário.
5. Teste registrado em commit próprio, com referência de fechamento da issue #17.

## Cenário #18: remoção do único equipamento

1. Adicionado teste que cria uma locação aberta com um equipamento e tenta alterar seus itens para uma lista vazia.
2. O teste exige rejeição, preservação dos dados originais e ausência de salvamento.
3. Executado `./mvnw test`: 17 execuções passaram, sem falhas.
4. O teste passou na primeira execução: a validação de pelo menos um equipamento já existe em Locacao e é compartilhada entre criação e alteração. Não houve fase de falha nem alteração de produção neste cenário.
5. Teste registrado em commit próprio, com referência de fechamento da issue #18.

Os seis cenários BDD de alteração da US02 (#13 a #18) estão cobertos. Persistência, testes funcionais adicionais e API continuam pendentes.

## Cenário #20: confirmar retirada na data inicial — fase de falha

1. Adicionado teste do serviço para confirmar a retirada de uma locação aberta com equipamento na data inicial.
2. O teste exige estado EM_ANDAMENTO, preservação de identificador, cliente, período e itens, e salvamento pelo repositório.
3. Executado `./mvnw -Dtest=ConfirmarRetiradaServiceTest test`: falha na compilação pela ausência de ConfirmarRetiradaService. Nenhum teste foi executado nessa tentativa.
4. Teste registrado em commit separado, sem implementação de produção. A transição mínima do agregado já existe desde o cenário #14; falta o serviço de aplicação.

## Cenário #20: implementação do serviço de retirada

1. Em um passo posterior ao commit do teste, adicionado ConfirmarRetiradaService: busca a locação, confirma a retirada pelo agregado e salva o resultado.
2. Executado `./mvnw test`: 18 execuções passaram, sem falhas.
3. Implementação registrada em commit separado, com referência de fechamento da issue #20.

As rejeições por data antecipada e estado continuam pendentes dos próximos cenários da US03.

## Cenário #21: retirada antecipada — fase de falha

1. Adicionado teste de confirmação de retirada um dia antes da data inicial.
2. O teste exige rejeição, estado ABERTA e dados originais preservados, sem salvamento.
3. Executado `./mvnw test`: 19 execuções, 18 passaram e apenas o cenário #21 falhou porque nenhuma exceção foi lançada.
4. Teste registrado em commit separado, sem modificar a implementação.

## Cenário #21: implementação da rejeição de retirada antecipada

1. Em um passo posterior ao commit do teste, adicionada validação da data em Locacao.confirmarRetirada, antes de modificar o estado.
2. Executado `./mvnw test`: 19 execuções passaram, sem falhas. A retirada na data inicial permanece aceita pelo cenário #20.
3. Implementação registrada em commit separado, com referência de fechamento da issue #21.

Próximo cenário: #22, rejeitar retirada de locação cancelada.
