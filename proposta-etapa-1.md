# Proposta para aprovação - Etapa 1

## Módulo de Aluguel de Equipamentos

Professor, propomos desenvolver um módulo para gerenciamento do aluguel de equipamentos, como ferramentas, câmeras, projetores e equipamentos de áudio. O cliente poderá criar uma locação, selecionar equipamentos e o período de uso, confirmar a retirada, renovar o prazo, registrar devoluções, cancelar a locação e consultar seu histórico.

O módulo será limitado ao agregado de locação. Não serão implementados autenticação, pagamento real, cadastro completo de clientes, manutenção dos equipamentos ou integração com serviços externos.

## Estrutura do agregado DDD

| Elemento | Classe proposta | Responsabilidade |
|---|---|---|
| Entidade raiz | `Locacao` | Controlar os equipamentos alugados, o período, os valores e as mudanças de estado da locação. |
| Entidade interna | `ItemLocacao` | Representar um equipamento incluído na locação, com código, descrição, valor da diária e situação de devolução. Somente poderá ser manipulado por meio de `Locacao`. |
| Objeto de valor | `PeriodoLocacao` | Representar, de forma imutável, as datas de início e término previstas e garantir um período válido. |
| Objeto de valor | `Dinheiro` | Representar valores monetários e realizar cálculos sem perda de precisão. |
| Objeto de valor adicional | `CodigoEquipamento` | Representar e validar o código que identifica cada equipamento. |
| Repositório | `LocacaoRepository` | Salvar, buscar, listar e excluir agregados `Locacao`, além de verificar se um equipamento já está reservado em determinado período. Não haverá repositório separado para `ItemLocacao`. |

A locação poderá assumir os estados `ABERTA`, `EM_ANDAMENTO`, `PARCIALMENTE_DEVOLVIDA`, `FINALIZADA` e `CANCELADA`.

## User stories previstas e serviços de aplicação

| ID | User story | Serviço de aplicação |
|---|---|---|
| US01 | Como atendente, eu quero criar uma locação escolhendo cliente, período e equipamentos, para que o aluguel seja registrado. | `CriarLocacaoService` |
| US02 | Como atendente, eu quero alterar o período ou os equipamentos de uma locação aberta, para que eu possa corrigir a reserva antes da retirada. | `AlterarLocacaoService` |
| US03 | Como atendente, eu quero confirmar a retirada dos equipamentos, para que a locação seja iniciada. | `ConfirmarRetiradaService` |
| US04 | Como cliente, eu quero renovar uma locação em andamento, para que eu possa permanecer com os equipamentos por mais tempo. | `RenovarLocacaoService` |
| US05 | Como atendente, eu quero registrar a devolução dos equipamentos, para que o valor final e eventuais multas sejam calculados. | `RegistrarDevolucaoService` |
| US06 | Como cliente, eu quero cancelar uma locação ainda não iniciada, para que os equipamentos voltem a ficar disponíveis. | `CancelarLocacaoService` |
| US07 | Como cliente, eu quero consultar minhas locações, para que eu possa acompanhar períodos, estados e valores. | `ConsultarLocacoesService` |

As histórias US01, US02, US06 e US07 representam as operações CRUD do agregado. As demais histórias contêm as principais mudanças de estado e regras de negócio.

## Regras de negócio previstas

### Criação e alteração

- Toda locação deve possuir identificador, identificação do cliente, período e estado válidos.
- O período deve possuir data inicial anterior à data final e duração entre 1 e 30 dias.
- Uma locação pode conter de 1 a 5 equipamentos diferentes.
- Cada item deve possuir código, descrição não vazia e valor de diária maior que zero.
- O mesmo equipamento não pode aparecer duas vezes na mesma locação.
- Um equipamento não poderá ser incluído quando já estiver reservado em outra locação que se sobreponha ao período solicitado.
- Somente uma locação no estado `ABERTA` poderá ter período ou equipamentos alterados.
- Uma alteração não poderá tornar indisponível algum equipamento já reservado por outra locação.

### Retirada

- Somente uma locação `ABERTA`, com pelo menos um equipamento, poderá ter a retirada confirmada.
- A retirada não poderá ser confirmada antes da data inicial da locação.
- Ao confirmar a retirada, o estado mudará para `EM_ANDAMENTO`.
- Uma locação cancelada, finalizada ou já iniciada não poderá ter nova retirada confirmada.

### Renovação

- Somente uma locação `EM_ANDAMENTO` ou `PARCIALMENTE_DEVOLVIDA` poderá ser renovada.
- A renovação deverá ser solicitada antes do término do período atual.
- Cada locação poderá ser renovada no máximo duas vezes.
- Cada renovação poderá acrescentar de 1 a 7 dias ao período.
- A renovação será recusada se algum equipamento ainda não devolvido já estiver reservado por outro cliente durante o período adicional.

### Devolução e cálculo do valor

- A devolução poderá incluir todos os equipamentos ou apenas parte deles.
- Cada equipamento poderá ser devolvido apenas uma vez.
- Após uma devolução parcial, a locação assumirá o estado `PARCIALMENTE_DEVOLVIDA`.
- Quando todos os equipamentos forem devolvidos, a locação assumirá o estado `FINALIZADA`.
- O valor normal de cada item será calculado por `valor da diária x quantidade de dias contratados`.
- Quando a devolução ocorrer após a data final, será cobrada, por dia de atraso, uma multa correspondente a 20% do valor da diária de cada equipamento devolvido com atraso.
- O valor final será a soma dos valores normais e das multas de todos os itens.
- Uma locação finalizada ou cancelada não poderá receber novas devoluções.

### Cancelamento e consulta

- Somente uma locação `ABERTA` poderá ser cancelada.
- Uma locação em andamento deverá ser encerrada por meio da devolução dos equipamentos, e não por cancelamento.
- Uma locação cancelada não poderá ser alterada, iniciada, renovada ou cancelada novamente.
- A consulta permitirá buscar uma locação por identificador, listar as locações de um cliente, filtrar por estado e ordenar da mais recente para a mais antiga.

## Validade dos estados

As classes não terão métodos `set` públicos. Construtores e fábricas validarão os dados iniciais, e cada método de domínio verificará todas as regras antes de modificar o estado. Os objetos de valor serão imutáveis e a coleção de itens não será exposta para alteração externa. Assim, todas as mudanças passarão pela entidade raiz `Locacao`, mantendo o agregado sempre válido.

Após a aprovação desta proposta, cada user story será especificada em cenários BDD no formato solicitado e registrada no backlog Kanban do GitHub.
