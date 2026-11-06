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
npm run preview
```

Os arquivos ficam em `dist/`. O `preview` serve esse build em `http://localhost:4173`, com o mesmo encaminhamento de `/api`.

O endereço de publicação ainda será definido. O servidor que publicar `dist/` precisa encaminhar `/api` para o back-end e devolver `index.html` nas rotas da aplicação, como `/locacoes`.

## Configuração

| Variável | Padrão | Uso |
|---|---|---|
| `VITE_CLIENTE_ID` | `cliente-1` | Cliente usado enquanto não há autenticação |

Para mudar o valor, crie `frontend/.env.local` com `VITE_CLIENTE_ID=outro-cliente`.

## Autenticação

Login e cadastro ainda não estão conectados, porque o back-end não tem rotas de autenticação:

- O login só confere se os campos foram preenchidos e entra com o cliente de `VITE_CLIENTE_ID`.
- O cadastro valida o formulário e informa que nenhuma conta foi criada.

Na integração, troque o corpo de `entrar` e `cadastrar` em `src/sessao.js` pelas chamadas reais. `entrar` deve guardar o ID devolvido pelo back-end, que passa a ser o `clienteId`. As telas já tratam carregamento, envio repetido e erros da API.

Nas chamadas de locação, 401 encerra a sessão e leva ao login, e 403 informa a falta de permissão.
