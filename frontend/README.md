# Front-end de Locação de Equipamentos

Interface em React e Vite para a API de locações do projeto.

## Requisitos

- Node.js 20.19 ou superior
- API em execução em `http://localhost:8080`, com as rotas de `/api/auth` (veja o README da raiz)

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

## Autenticação

A sessão usa o cookie `JSESSIONID`, definido pelo back-end como `HttpOnly`. Ao carregar, a aplicação consulta `/api/auth/sessao` para recuperar o usuário, e o `id` dele é o `clienteId` das locações. Essa integração fica em `src/sessao.js`.

Todo POST e PUT envia o token de `/api/auth/csrf`, que é buscado de novo após login e logout (`src/api.js`).

- 401 no login informa credenciais incorretas. Nas demais chamadas, encerra a sessão e leva ao login.
- 403 informa a falta de permissão. Em POST e PUT, a chamada é repetida uma vez com token novo, porque o token pode ter vencido.

O cookie exige que a aplicação e a API fiquem no mesmo endereço, com `/api` encaminhado ao back-end.
