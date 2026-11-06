const BASE = '/api';
const LOCACOES = '/locacoes';
const MENSAGENS = {
  401: 'Sua sessão expirou. Entre novamente.',
  403: 'Você não tem permissão para esta operação.',
};

let aoExpirarSessao = null;
export const definirAoExpirarSessao = (acao) => { aoExpirarSessao = acao; };

export class ErroApi extends Error {
  constructor(mensagem, status) {
    super(mensagem);
    this.status = status;
  }
}

let cabecalhoCsrf = null;
export const descartarCsrf = () => { cabecalhoCsrf = null; };

async function obterCsrf() {
  if (!cabecalhoCsrf) {
    const { headerName, token } = await chamar('/auth/csrf');
    cabecalhoCsrf = { [headerName]: token };
  }
  return cabecalhoCsrf;
}

async function chamar(caminho, { metodo = 'GET', corpo } = {}, podeRepetir = true) {
  const alteraDados = metodo !== 'GET';
  const headers = {
    ...(corpo === undefined ? {} : { 'Content-Type': 'application/json' }),
    ...(alteraDados ? await obterCsrf() : {}),
  };
  let resposta;
  try {
    resposta = await fetch(BASE + caminho, {
      method: metodo,
      credentials: 'include',
      headers,
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
    // O 403 pode ser só um token CSRF vencido, como quando a sessão expira. A operação
    // recusada não foi executada, então repetir uma vez com token novo é seguro.
    if (resposta.status === 403 && alteraDados && podeRepetir) {
      descartarCsrf();
      return chamar(caminho, { metodo, corpo }, false);
    }
    if (resposta.status === 401) aoExpirarSessao?.();
    throw new ErroApi(dados?.mensagem || MENSAGENS[resposta.status] || 'Não foi possível concluir a operação. Tente novamente.', resposta.status);
  }
  return dados;
}

export const cadastrarUsuario = (dados) => chamar('/auth/cadastro', { metodo: 'POST', corpo: dados });
export const autenticar = (credenciais) => chamar('/auth/login', { metodo: 'POST', corpo: credenciais });
export const buscarSessao = () => chamar('/auth/sessao');
export const encerrarAutenticacao = () => chamar('/auth/logout', { metodo: 'POST' });

export function listarLocacoes(clienteId, estado) {
  const parametros = new URLSearchParams({ clienteId });
  if (estado) parametros.set('estado', estado);
  return chamar(`${LOCACOES}?${parametros}`);
}

export const buscarLocacao = (id) => chamar(`${LOCACOES}/${id}`);
export const criarLocacao = (reserva) => chamar(LOCACOES, { metodo: 'POST', corpo: reserva });
export const alterarLocacao = (id, reserva) => chamar(`${LOCACOES}/${id}`, { metodo: 'PUT', corpo: reserva });
export const confirmarRetirada = (id, data) => chamar(`${LOCACOES}/${id}/retirada`, { metodo: 'POST', corpo: { data } });
export const renovarLocacao = (id, diasAdicionais, data) => chamar(`${LOCACOES}/${id}/renovacoes`, { metodo: 'POST', corpo: { dias: diasAdicionais, data } });
export const devolverEquipamentos = (id, codigos, data) => chamar(`${LOCACOES}/${id}/devolucoes`, { metodo: 'POST', corpo: { codigos, data } });
export const cancelarLocacao = (id) => chamar(`${LOCACOES}/${id}/cancelamento`, { metodo: 'POST' });
