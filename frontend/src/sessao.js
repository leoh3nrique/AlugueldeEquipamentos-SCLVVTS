import { autenticar, buscarSessao, cadastrarUsuario, descartarCsrf, encerrarAutenticacao } from './api.js';

// A sessão vale pelo cookie HttpOnly do back-end. Este usuário é só a cópia local
// para a interface e é recuperado de /api/auth/sessao ao carregar a aplicação.
let usuario = null;

export const usuarioAtual = () => usuario;
export const clienteAtual = () => usuario?.id;
export const cadastrar = cadastrarUsuario;

export function encerrarSessao() {
  usuario = null;
  descartarCsrf();
}

export async function recuperarSessao() {
  try {
    usuario = await buscarSessao();
  } catch (erro) {
    if (erro.status !== 401) throw erro;
    encerrarSessao();
  }
}

export async function entrar(credenciais) {
  usuario = await autenticar(credenciais);
  descartarCsrf();
}

export async function sair() {
  await encerrarAutenticacao();
  encerrarSessao();
}
