# Front-end de Locação de Equipamentos

Interface em React e Vite para a API de locações do projeto.

## Requisitos

- Node.js 20.19 ou superior
- API em execução em `http://localhost:8080` (veja o README da raiz)

## Executar

Na pasta `frontend`:

```bash
npm install
npm run dev
```

A aplicação abre em `http://localhost:5173`. As chamadas usam caminhos relativos a `/api`, e o Vite as encaminha para `http://localhost:8080`, então o back-end não precisa de CORS em desenvolvimento.

## Gerar a versão de produção

```bash
npm run build
```

Os arquivos ficam em `dist/`. O endereço de publicação e o encaminhamento de `/api` ainda serão definidos.

## Configuração

| Variável | Padrão | Uso |
|---|---|---|
| `VITE_CLIENTE_ID` | `cliente-1` | Cliente usado enquanto não há autenticação |

Para mudar o valor, crie `frontend/.env.local` com `VITE_CLIENTE_ID=outro-cliente`.

## Autenticação

Login e cadastro estão desenhados, mas não conectados, porque o back-end ainda não tem autenticação:

- O login só confere se os campos foram preenchidos e entra com o cliente de `VITE_CLIENTE_ID`.
- O cadastro valida o formulário e não cria conta.

Na integração, o `clienteId` passará a ser o ID do usuário autenticado. Essa troca fica em `src/sessao.js`.
