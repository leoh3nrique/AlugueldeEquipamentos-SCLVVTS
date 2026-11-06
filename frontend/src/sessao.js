import { ErroApi } from './api.js';

// O clienteId é o ID do usuário devolvido pelo back-end. Enquanto as rotas de
// autenticação não existem, entrar e cadastrar são provisórios: troque o corpo
// deles pelas chamadas reais, sem mudar as telas.
const CHAVE = 'locacao.clienteId';
const CLIENTE_DESENVOLVIMENTO = import.meta.env.VITE_CLIENTE_ID || 'cliente-1';

const iniciarSessao = (usuario) => sessionStorage.setItem(CHAVE, usuario.id);

export const clienteAtual = () => sessionStorage.getItem(CHAVE);
export const encerrarSessao = () => sessionStorage.removeItem(CHAVE);

export async function entrar() {
  iniciarSessao({ id: CLIENTE_DESENVOLVIMENTO });
}

export async function cadastrar() {
  throw new ErroApi('Cadastro ainda não conectado ao servidor. Nenhuma conta foi criada.', 0);
}
