const MOEDA = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
const UM_DIA = 86400000;

const tempo = (data) => Date.parse(`${data}T00:00:00Z`);

export const dinheiro = (valor) => MOEDA.format(valor);
export const dias = (inicio, fim) => Math.round((tempo(fim) - tempo(inicio)) / UM_DIA);
export const somarDias = (data, quantidade) => new Date(tempo(data) + quantidade * UM_DIA).toISOString().slice(0, 10);
export const dataBr = (data) => (data ? data.split('-').reverse().join('/') : '—');
export const periodoBr = (inicio, fim) => `${dataBr(inicio)} a ${dataBr(fim)}`;
export const plural = (quantidade, um, varios) => `${quantidade} ${quantidade === 1 ? um : varios}`;
export const idCurto = (id) => id.slice(0, 8);

export const dataHoraBr = (instante) => new Date(instante).toLocaleString('pt-BR', {
  day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit',
});

export function hoje() {
  const agora = new Date();
  return new Date(agora.getTime() - agora.getTimezoneOffset() * 60000).toISOString().slice(0, 10);
}

export function numero(texto) {
  const limpo = String(texto).trim();
  return Number(limpo.includes(',') ? limpo.replace(/\./g, '').replace(',', '.') : limpo);
}

export const ESTADOS = {
  ABERTA: { rotulo: 'Reserva aberta', classe: 'tag tag-aberta' },
  EM_ANDAMENTO: { rotulo: 'Em andamento', classe: 'tag tag-andamento' },
  PARCIALMENTE_DEVOLVIDA: { rotulo: 'Parcialmente devolvida', classe: 'tag tag-parcial' },
  FINALIZADA: { rotulo: 'Finalizada', classe: 'tag tag-finalizada' },
  CANCELADA: { rotulo: 'Cancelada', classe: 'tag tag-cancelada' },
};
