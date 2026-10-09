# Módulo de Aluguel de Equipamentos

Projeto acadêmico da disciplina de Verificação, Validação e Teste de Software.

## Agregado proposto

- Entidade raiz: `Locacao`
- Entidade interna: `ItemLocacao`
- Objetos de valor: `PeriodoLocacao`, `Dinheiro` e `CodigoEquipamento`
- Repositório: `LocacaoRepository`

## Executar o projeto

Requisito: JDK 17 ou superior. O Maven Wrapper incluído baixa o Maven na primeira execução.

```bash
./mvnw spring-boot:run
```

A aplicação inicia na porta 8080 e cria a tabela de locações automaticamente no SQLite local `database.db`.
O agregado completo é persistido via JDBC como documento JSON: identidade, criação, estado, itens, prazos, retirada, devoluções e renovações. Não há JPA ou Lombok.

Para compilar e executar os testes disponíveis:

```bash
./mvnw verify
```

O build executa 81 casos: 46 execuções dos 45 cenários BDD, 31 testes funcionais e 4 testes de integração com SQLite real.

As três suítes podem ser executadas separadamente:

```bash
./mvnw -Dtest=TddSuite test
./mvnw -Dtest=FunctionalSuite test
./mvnw -Dtest=UnitTestSuite test
```

As suítes executam, respectivamente, 46, 31 e 77 casos. Os testes SQLite usam a tag `IntegrationTest` e ficam fora das suítes unitárias.

## API REST

| Método | Caminho | Operação |
|---|---|---|
| POST | `/api/locacoes` | Criar reserva (201 e cabeçalho Location) |
| GET | `/api/locacoes/{id}` | Buscar por identificador |
| GET | `/api/locacoes?clienteId=cliente-1&estado=EM_ANDAMENTO` | Listar por cliente; estado opcional |
| PUT | `/api/locacoes/{id}` | Substituir período e equipamentos da reserva aberta |
| POST | `/api/locacoes/{id}/retirada` | Confirmar retirada |
| POST | `/api/locacoes/{id}/renovacoes` | Renovar prazo |
| POST | `/api/locacoes/{id}/devolucoes` | Devolver um ou mais equipamentos |
| POST | `/api/locacoes/{id}/cancelamento` | Cancelar reserva aberta |

Exemplo de criação:

```bash
curl -i http://localhost:8080/api/locacoes \
  -H 'Content-Type: application/json' \
  -d '{"clienteId":"cliente-1","inicio":"2026-10-09","fim":"2026-10-12","itens":[{"codigo":"CAM01","descricao":"Câmera","diaria":100.00}]}'
```

Use o identificador retornado nas outras operações. Os corpos JSON são:

```json
{"data":"2026-10-09"}
```

Para retirada; para renovação:

```json
{"dias":3,"data":"2026-10-10"}
```

Para devolução:

```json
{"codigos":["CAM01"],"data":"2026-10-15"}
```

O cancelamento dispensa corpo. A alteração recebe `inicio`, `fim` e `itens` como na criação e preserva o cliente original.
Erros retornam JSON com `mensagem`: 400 para entradas inválidas ou equipamento indisponível, 404 para locação inexistente, 409 para estado incompatível e 503 para indisponibilidade do banco.

## Regras e decisões

- Períodos de reserva usam o intervalo `[início, fim)`: a quantidade de diárias é a diferença entre as datas. Reservas adjacentes são permitidas.
- A reserva inicial tem de 1 a 30 dias; até duas renovações podem acrescentar de 1 a 7 dias cada, chegando a 44 dias totais.
- A renovação deve ser solicitada antes da data final. Itens devolvidos preservam seu prazo contratado e não participam das novas consultas de disponibilidade.
- Dinheiro usa BigDecimal e centavos com HALF_UP. A multa é calculada por equipamento como 20% da diária vezes os dias de atraso e arredondada por item.
- Consultas são ordenadas pela data de criação, da mais recente para a mais antiga.
- Cada equipamento possui identidade pelo código dentro da locação. Os itens são imutáveis; devoluções e prazos são controlados exclusivamente pela raiz do agregado.
- Os serviços usam transações JDBC e uma conexão no pool SQLite para serializar as operações desta instância da aplicação.

## Documentação da entrega

- [Especificação da Etapa 1](proposta-etapa-1.md)
- [Registro do processo e dos cenários](docs/tdd-etapa-2.md)
- [Critérios funcionais e validação final](docs/entrega-etapa-2.md)
- [Auditoria histórica do processo](docs/auditoria-etapa-2.md)
- [Plano inicial de execução](plano-etapa-2.md)

Os registros preservam os casos que passaram de imediato e a antecipação inicial de validações. Este projeto é realizado individualmente por Leonardo, conforme esclarecido pelo responsável. O PDF orienta grupos de três; essa orientação não descreve a composição deste projeto.

## Origem da base

A estrutura Spring Boot e o Maven Wrapper foram adaptados do [projeto base da disciplina](https://github.com/lucas-ifsp/demo-auth-app), licenciado sob GPL-3.0 (ver [LICENSE](LICENSE)).
Foram mantidas as versões de Spring Boot e SQLite da base. O projeto usa Java 17, disponível no ambiente de desenvolvimento, e JDBC para acesso ao banco, sem JPA ou Lombok, conforme o enunciado.
O exemplo de autenticação da base não foi incorporado porque está fora do escopo aprovado.
