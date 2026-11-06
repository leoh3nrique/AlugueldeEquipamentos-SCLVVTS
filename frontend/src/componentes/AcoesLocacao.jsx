import { useState } from 'react';
import { Link } from 'react-router-dom';
import { cancelarLocacao, confirmarRetirada, devolverEquipamentos, renovarLocacao } from '../api.js';
import { dataBr, dias, dinheiro, hoje, plural, somarDias } from '../formato.js';
import Campo from './Campo.jsx';

const LIMITE_RENOVACOES = 2;
const TITULOS = { retirada: 'Confirmar retirada', renovar: 'Renovar', devolucao: 'Registrar devolução', cancelar: 'Cancelar reserva' };

function Botoes({ rotulo, rotuloEnviando, enviando, bloqueado, classe = 'btn-primario', rotuloVoltar = 'Voltar', aoVoltar }) {
  return (
    <div className="painel-botoes">
      <button type="button" className="btn btn-secundario" onClick={aoVoltar}>{rotuloVoltar}</button>
      <button type="submit" className={`btn ${classe}`} disabled={enviando || bloqueado}>{enviando ? rotuloEnviando : rotulo}</button>
    </div>
  );
}

function PainelRetirada({ locacao, enviar, ...botoes }) {
  const [data, definirData] = useState(hoje() > locacao.inicio ? hoje() : locacao.inicio);
  return (
    <form className="painel-form" noValidate onSubmit={enviar(() => confirmarRetirada(locacao.id, data), `Retirada confirmada em ${dataBr(data)}.`)}>
      <Campo id="data-retirada" rotulo="Data da retirada" type="date" value={data} ajuda={`A partir de ${dataBr(locacao.inicio)}, início do período.`} onChange={(e) => definirData(e.target.value)} />
      <Botoes rotulo="Confirmar" rotuloEnviando="Confirmando…" {...botoes} />
    </form>
  );
}

function PainelRenovar({ locacao, enviar, ...botoes }) {
  const [quantidade, definirQuantidade] = useState('1');
  const [data, definirData] = useState(hoje());
  const adicionais = Number(quantidade);
  const previsto = Number.isInteger(adicionais) && adicionais >= 1 && adicionais <= 7;
  const novoFim = previsto ? somarDias(locacao.fim, adicionais) : null;
  return (
    <form className="painel-form" noValidate onSubmit={enviar(() => renovarLocacao(locacao.id, adicionais, data), `Locação renovada até ${dataBr(novoFim)}.`)}>
      <Campo id="dias-adicionais" rotulo="Dias adicionais" type="number" min="1" max="7" value={quantidade} ajuda="De 1 a 7 dias." onChange={(e) => definirQuantidade(e.target.value)} />
      <Campo id="data-solicitacao" rotulo="Data da solicitação" type="date" value={data} ajuda={`Antes de ${dataBr(locacao.fim)}, fim atual.`} onChange={(e) => definirData(e.target.value)} />
      <p className="previa">{previsto ? `Novo fim: ${dataBr(novoFim)}. Esta será a renovação ${locacao.renovacoes + 1} de ${LIMITE_RENOVACOES}.` : 'Informe de 1 a 7 dias.'}</p>
      <Botoes rotulo="Renovar" rotuloEnviando="Renovando…" bloqueado={!previsto} {...botoes} />
    </form>
  );
}

function PainelDevolucao({ locacao, enviar, ...botoes }) {
  const pendentes = locacao.itens.filter((item) => !item.devolvido);
  const [codigos, definirCodigos] = useState([]);
  const [data, definirData] = useState(hoje());
  const alternar = (codigo) => definirCodigos(codigos.includes(codigo) ? codigos.filter((c) => c !== codigo) : [...codigos, codigo]);

  const atraso = data ? Math.max(dias(locacao.fim, data), 0) : 0;
  const multa = pendentes
    .filter((item) => codigos.includes(item.codigo))
    .reduce((total, item) => total + Math.round(item.diaria * 20 * atraso) / 100, 0);
  let previa = 'Selecione ao menos um equipamento.';
  if (codigos.length > 0) previa = atraso > 0 ? `${plural(atraso, 'dia de atraso', 'dias de atraso')}. Multa estimada: ${dinheiro(multa)}.` : 'Sem multa nesta data.';

  return (
    <form className="painel-form" noValidate onSubmit={enviar(() => devolverEquipamentos(locacao.id, codigos, data), `${plural(codigos.length, 'equipamento devolvido', 'equipamentos devolvidos')} em ${dataBr(data)}.`)}>
      <fieldset className="campo sem-borda">
        <legend className="rotulo">Equipamentos devolvidos</legend>
        {pendentes.map((item) => (
          <label className="marcar" key={item.codigo}>
            <input type="checkbox" checked={codigos.includes(item.codigo)} onChange={() => alternar(item.codigo)} />
            <span className="marcar-texto"><span className="codigo">{item.codigo}</span><span className="suave pequeno">{item.descricao}</span></span>
          </label>
        ))}
      </fieldset>
      <Campo id="data-devolucao" rotulo="Data da devolução" type="date" value={data} onChange={(e) => definirData(e.target.value)} />
      <p className="previa">{previa}</p>
      <Botoes rotulo="Registrar" rotuloEnviando="Registrando…" bloqueado={codigos.length === 0} {...botoes} />
    </form>
  );
}

function PainelCancelar({ locacao, enviar, ...botoes }) {
  return (
    <form className="painel-form" onSubmit={enviar(() => cancelarLocacao(locacao.id), 'Reserva cancelada.')}>
      <p>A reserva será cancelada e os equipamentos ficarão livres para outras reservas. Esta ação não pode ser desfeita.</p>
      <Botoes rotulo="Cancelar reserva" rotuloEnviando="Cancelando…" classe="btn-perigo-cheio" rotuloVoltar="Manter reserva" {...botoes} />
    </form>
  );
}

const PAINEIS = { retirada: PainelRetirada, renovar: PainelRenovar, devolucao: PainelDevolucao, cancelar: PainelCancelar };

export default function AcoesLocacao({ locacao, aoConcluir, aoRecusar, aoAbrir }) {
  const [painel, definirPainel] = useState('');
  const [enviando, definirEnviando] = useState(false);
  const [erro, definirErro] = useState('');

  const aberta = locacao.estado === 'ABERTA';
  const ativa = locacao.estado === 'EM_ANDAMENTO' || locacao.estado === 'PARCIALMENTE_DEVOLVIDA';
  const limiteAtingido = locacao.renovacoes >= LIMITE_RENOVACOES;

  function abrir(nome) {
    definirErro('');
    definirPainel(nome);
    aoAbrir();
  }

  const enviar = (operacao, mensagem) => async (evento) => {
    evento.preventDefault();
    definirErro('');
    definirEnviando(true);
    try {
      const atualizada = await operacao();
      definirPainel('');
      aoConcluir(atualizada, mensagem);
    } catch (falha) {
      definirErro(falha.message);
      if (falha.status === 409) aoRecusar();
    } finally {
      definirEnviando(false);
    }
  };

  const Painel = PAINEIS[painel];

  return (
    <section className="cartao" aria-labelledby="titulo-acoes">
      <div className="cartao-corpo">
        <h2 id="titulo-acoes">{TITULOS[painel] || 'Ações'}</h2>
        {erro && <div className="aviso aviso-erro" role="alert"><p>{erro}</p></div>}
        {Painel && <Painel locacao={locacao} enviar={enviar} enviando={enviando} aoVoltar={() => abrir('')} />}
        {!Painel && (
          <div className="acoes">
            {aberta && <button type="button" className="btn btn-primario btn-bloco" onClick={() => abrir('retirada')}>Confirmar retirada</button>}
            {ativa && <button type="button" className="btn btn-primario btn-bloco" onClick={() => abrir('devolucao')}>Registrar devolução</button>}
            {ativa && <button type="button" className="btn btn-secundario btn-bloco" disabled={limiteAtingido} onClick={() => abrir('renovar')}>Renovar</button>}
            {ativa && limiteAtingido && <p className="ajuda">Limite de {LIMITE_RENOVACOES} renovações atingido.</p>}
            {aberta && <Link className="btn btn-secundario btn-bloco" to={`/locacoes/${locacao.id}/alterar`}>Alterar reserva</Link>}
            {aberta && <button type="button" className="btn btn-perigo btn-bloco" onClick={() => abrir('cancelar')}>Cancelar reserva</button>}
            {!aberta && !ativa && (
              <p className="suave">{locacao.estado === 'CANCELADA' ? 'Reserva cancelada.' : 'Locação finalizada.'} Não há ações disponíveis.</p>
            )}
          </div>
        )}
      </div>
    </section>
  );
}
