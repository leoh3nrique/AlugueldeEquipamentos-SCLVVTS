const BASE = '/api/locacoes';

export class ErroApi extends Error {
  constructor(mensagem, status) {
    super(mensagem);
    this.status = status;
  }
}

async function chamar(caminho, { metodo = 'GET', corpo } = {}) {
  let resposta;
  try {
    resposta = await fetch(BASE + caminho, {
      method: metodo,
      headers: corpo === undefined ? undefined : { 'Content-Type': 'application/json' },
      body: corpo === undefined ? undefined : JSON.stringify(corpo),
    });
  } catch {
    throw new ErroApi('Não foi possível conectar ao servidor. Tente novamente.', 0);
  }
  const texto = await resposta.text();
  let dados = null;
  try {
    dados = texto ? JSON.parse(texto) : null;
  } catch {
    dados = null;
  }
  if (!resposta.ok) {
    throw new ErroApi(dados?.mensagem || 'Não foi possível concluir a operação. Tente novamente.', resposta.status);
  }
  return dados;
}

export function listarLocacoes(clienteId, estado) {
  const parametros = new URLSearchParams({ clienteId });
  if (estado) parametros.set('estado', estado);
  return chamar(`?${parametros}`);
}

export const buscarLocacao = (id) => chamar(`/${id}`);
export const criarLocacao = (reserva) => chamar('', { metodo: 'POST', corpo: reserva });
export const alterarLocacao = (id, reserva) => chamar(`/${id}`, { metodo: 'PUT', corpo: reserva });
export const confirmarRetirada = (id, data) => chamar(`/${id}/retirada`, { metodo: 'POST', corpo: { data } });
export const renovarLocacao = (id, diasAdicionais, data) => chamar(`/${id}/renovacoes`, { metodo: 'POST', corpo: { dias: diasAdicionais, data } });
export const devolverEquipamentos = (id, codigos, data) => chamar(`/${id}/devolucoes`, { metodo: 'POST', corpo: { codigos, data } });
export const cancelarLocacao = (id) => chamar(`/${id}/cancelamento`, { metodo: 'POST' });
