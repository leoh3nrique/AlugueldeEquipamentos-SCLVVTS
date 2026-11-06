// Provisório até o contrato de autenticação existir: o clienteId definitivo
// será o ID do usuário autenticado. Toda a troca fica concentrada neste arquivo.
const CHAVE = 'locacao.clienteId';
const CLIENTE_DESENVOLVIMENTO = import.meta.env.VITE_CLIENTE_ID || 'cliente-1';

export const clienteAtual = () => sessionStorage.getItem(CHAVE);
export const iniciarSessao = () => sessionStorage.setItem(CHAVE, CLIENTE_DESENVOLVIMENTO);
export const encerrarSessao = () => sessionStorage.removeItem(CHAVE);
