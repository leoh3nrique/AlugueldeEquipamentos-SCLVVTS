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

A aplicação inicia na porta 8080. Os endpoints serão adicionados durante a Etapa 2.
O SQLite está configurado para usar o arquivo local `database.db`; as tabelas e a persistência serão implementadas junto ao domínio.

Para compilar e executar os testes disponíveis:

```bash
./mvnw verify
```

## Origem da base

A estrutura Spring Boot e o Maven Wrapper foram adaptados do [projeto base da disciplina](https://github.com/lucas-ifsp/demo-auth-app), licenciado sob GPL-3.0 (ver [LICENSE](LICENSE)).
Foram mantidas as versões de Spring Boot e SQLite da base. O projeto usa Java 17, disponível no ambiente de desenvolvimento, e JDBC para acesso ao banco, sem JPA ou Lombok, conforme o enunciado.
O exemplo de autenticação da base não foi incorporado porque está fora do escopo aprovado.
