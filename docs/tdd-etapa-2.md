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

## Cenário #22: retirada de locação cancelada — fase de falha

1. Adicionado teste que cancela uma locação aberta e tenta confirmar a retirada na data inicial.
2. O teste exige rejeição, estado CANCELADA e dados originais preservados, sem salvamento.
3. Executado `./mvnw -Dtest=ConfirmarRetiradaServiceTest#deveRejeitarRetiradaDeLocacaoCancelada test`: falha na compilação pela ausência de cancelar(). Nenhum teste foi executado nessa tentativa.
4. Teste registrado em commit separado, sem implementação de produção. No próximo passo será necessária a transição mínima para CANCELADA usada na preparação; os demais cenários de cancelamento da US06 continuam pendentes.

## Cenário #22: implementação da rejeição por estado

1. Adicionada a transição mínima cancelar() para preparar CANCELADA, sem antecipar as validações de cancelamento da US06.
2. Executado o teste do cenário #22: 1 falha, pois a retirada ainda não lançava exceção.
3. Adicionada verificação de estado ABERTA em Locacao.confirmarRetirada, antes de modificar o estado.
4. Executado `./mvnw test`: 20 execuções passaram, sem falhas.
5. Implementação registrada em commit separado, com referência de fechamento da issue #22.

O método cancelar() ainda não valida o estado; essas regras continuam pendentes dos cenários da US06.

## Cenário #23: segunda confirmação de retirada

1. Adicionado teste que confirma a retirada no agregado para preparar EM_ANDAMENTO e tenta confirmar novamente pelo serviço no dia seguinte.
2. O teste exige rejeição, preservação do estado e dados originais, sem salvamento.
3. Executado `./mvnw test`: 21 execuções passaram, sem falhas.
4. O teste passou na primeira execução: a verificação de estado ABERTA introduzida no cenário #22 já bloqueia uma segunda retirada. Não houve fase de falha nem alteração de produção neste cenário.
5. Teste registrado em commit próprio, com referência de fechamento da issue #23.

## Cenário #24: retirada de locação finalizada — fase de falha

1. Adicionado teste que confirma a retirada e devolve o único equipamento para preparar FINALIZADA, antes de tentar iniciar novamente a locação.
2. O teste exige rejeição, preservação do estado e dados originais, sem salvamento.
3. Executado `./mvnw -Dtest=ConfirmarRetiradaServiceTest#deveRejeitarRetiradaDeLocacaoFinalizada test`: falha na compilação pela ausência de registrarDevolucao(List<CodigoEquipamento>, LocalDate). Nenhum teste foi executado nessa tentativa.
4. Teste registrado em commit separado, sem implementação de produção. O bloqueio de retirada por estado já existe; falta a preparação de FINALIZADA pelo domínio. Os demais cenários e cálculos de devolução da US05 continuam pendentes.

## Cenário #24: preparação de FINALIZADA e validação da rejeição

1. Em um passo posterior ao commit do teste, adicionada transição mínima de devolução: ao informar todos os códigos dos equipamentos, a locação assume FINALIZADA.
2. O bloqueio de retirada por estado existente no cenário #22 rejeita a nova retirada da locação finalizada; não foi necessário alterar essa validação.
3. Executado `./mvnw test`: 22 execuções passaram, sem falhas.
4. Implementação registrada em commit separado, com referência de fechamento da issue #24.

A devolução ainda não registra datas ou situação por item, não trata devoluções parciais e não calcula valores ou multas. Essas regras e as validações de devolução continuam pendentes dos cenários da US05.

Os cinco cenários BDD de retirada da US03 (#20 a #24) estão cobertos.

## Cenário #26: renovação de locação em andamento — fase de falha

1. Adicionado teste solicitando três dias adicionais antes do término de uma locação EM_ANDAMENTO, com equipamento disponível conforme resposta simulada do repositório.
2. O teste exige extensão da data final, incremento de zero para uma renovação, preservação de identificador, cliente, início, itens e estado, e salvamento.
3. A consulta de disponibilidade cobre apenas o período adicional e exclui a própria locação.
4. Executado `./mvnw -Dtest=RenovarLocacaoServiceTest test`: falha na compilação pela ausência de RenovarLocacaoService e getQuantidadeRenovacoes(). Nenhum teste foi executado nessa tentativa.
5. Teste registrado em commit separado, sem implementação de produção.

## Cenário #26: implementação da renovação válida

1. Adicionados RenovarLocacaoService, extensão do período pelo agregado e contador de renovações iniciado em zero.
2. O serviço busca a locação, consulta disponibilidade no período adicional excluindo a própria locação, renova e salva o agregado.
3. Executado `./mvnw test`: 23 execuções passaram, sem falhas.
4. Implementação registrada em commit separado, com referência de fechamento da issue #26.

Esta implementação cobre apenas o caso válido do teste. As restrições por estado, data, número de renovações, dias adicionais e conflito de reserva continuam pendentes. A resposta de disponibilidade ainda não é tratada; sua rejeição será introduzida pelo cenário #32. A duração total continua sujeita ao limite atual de PeriodoLocacao, a ser revisado nos testes dos limites de renovação.

## Cenário #27: renovação após devolução parcial — fase de falha

1. Adicionado teste com câmera devolvida e projetor pendente. A renovação solicita três dias adicionais antes do término.
2. O teste exige manter PARCIALMENTE_DEVOLVIDA, incrementar o contador, preservar o prazo contratado da câmera e estender apenas o prazo do projetor.
3. A disponibilidade deve ser consultada apenas para o projetor pendente, excluindo a própria locação.
4. Executado `./mvnw -Dtest=RenovarLocacaoServiceTest#deveRenovarSomenteEquipamentosAindaNaoDevolvidos test`: falha na compilação pela ausência de estaDevolvido e getFimContratado por equipamento. Nenhum teste foi executado nessa tentativa.
5. Teste registrado em commit separado, sem implementação de produção. A preparação da devolução parcial e o controle de prazo por item também serão necessários para satisfazer o cenário.

## Cenário #27: implementação da renovação após devolução parcial

1. Adicionado controle interno de datas de devolução e prazos contratados dos equipamentos devolvidos na raiz Locacao, sem expor coleções mutáveis.
2. A devolução de parte dos equipamentos prepara PARCIALMENTE_DEVOLVIDA; a devolução de todos prepara FINALIZADA.
3. A renovação consulta disponibilidade apenas dos equipamentos pendentes. O prazo dos devolvidos permanece congelado no término contratado quando foram devolvidos; os pendentes acompanham o novo término da locação.
4. Executado `./mvnw test`: 24 execuções passaram, sem falhas.
5. Implementação registrada em commit separado, com referência de fechamento da issue #27.

As validações de devolução, os valores e as multas continuam pendentes da US05. Os limites e as rejeições de renovação continuam pendentes dos próximos cenários.

## Cenário #28: renovação após o término — fase de falha

1. Adicionado teste solicitando três dias adicionais um dia após o término de uma locação EM_ANDAMENTO.
2. O teste exige rejeição, preservação do período, contador, estado e itens, sem salvamento.
3. Executado `./mvnw test`: 25 execuções, 24 passaram e apenas o cenário #28 falhou porque nenhuma exceção foi lançada.
4. Teste registrado em commit separado, sem modificar a implementação.

## Cenário #28: implementação da validação da data de renovação

1. Em um passo posterior ao commit do teste, adicionada validação em Locacao.renovar exigindo solicitação antes da data final, antes de modificar prazo ou contador.
2. Executado `./mvnw test`: 25 execuções passaram, sem falhas.
3. Implementação registrada em commit separado, com referência de fechamento da issue #28.

A condição também rejeita solicitação na própria data final, conforme a regra de solicitar antes do término. Esse limite ainda será coberto pelos testes funcionais adicionais.

## Cenário #29: renovação de locação aberta — fase de falha

1. Adicionado teste solicitando três dias adicionais antes do término de uma locação ABERTA, sem confirmar sua retirada.
2. O teste exige rejeição, preservação do período, contador, estado e itens, sem salvamento.
3. Executado `./mvnw test`: 26 execuções, 25 passaram e apenas o cenário #29 falhou porque nenhuma exceção foi lançada.
4. Teste registrado em commit separado, sem modificar a implementação.

## Cenário #29: implementação da validação de estado na renovação

1. Em um passo posterior ao commit do teste, adicionada validação em Locacao.renovar permitindo apenas EM_ANDAMENTO ou PARCIALMENTE_DEVOLVIDA, antes de modificar prazo ou contador.
2. Executado `./mvnw test`: 26 execuções passaram, sem falhas. Os casos válidos dos cenários #26 e #27 continuam passando.
3. Implementação registrada em commit separado, com referência de fechamento da issue #29.

## Cenário #30: terceira renovação — fase de falha

1. Adicionado teste que prepara duas renovações válidas de um dia cada e tenta uma terceira antes do término atual.
2. O teste exige rejeição, preservação do prazo após as duas renovações e contador em dois, sem salvamento.
3. Executado `./mvnw test`: 27 execuções, 26 passaram e apenas o cenário #30 falhou porque nenhuma exceção foi lançada.
4. Teste registrado em commit separado, sem modificar a implementação.

## Cenário #30: implementação do limite de duas renovações

1. Em um passo posterior ao commit do teste, adicionada validação em Locacao.renovar rejeitando uma nova renovação quando o contador já é dois, antes de modificar prazo ou contador.
2. Executado `./mvnw test`: 27 execuções passaram, sem falhas. As duas renovações usadas na preparação continuam aceitas e a terceira é rejeitada.
3. Implementação registrada em commit separado, com referência de fechamento da issue #30.

Próximo cenário: #31, rejeitar renovação com acréscimo de oito dias.
