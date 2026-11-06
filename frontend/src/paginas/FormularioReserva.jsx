import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { alterarLocacao, buscarLocacao, criarLocacao } from '../api.js';
import Campo from '../componentes/Campo.jsx';
import { IconeFechar, IconeMais, IconeVoltar } from '../componentes/Icones.jsx';
import { dias, dinheiro, numero } from '../formato.js';
import { clienteAtual } from '../sessao.js';

const MAXIMO_ITENS = 5;
const MAXIMO_DIAS = 30;
const itemVazio = () => ({ codigo: '', descricao: '', diaria: '' });
const SEM_ERROS = { itens: [] };

function validar({ inicio, fim, itens }) {
  const erros = { itens: itens.map(() => ({})) };
  if (!inicio) erros.inicio = 'Informe a data de início';
  if (!fim) erros.fim = 'Informe a data de fim';
  if (inicio && fim) {
    const diarias = dias(inicio, fim);
    if (diarias <= 0) erros.fim = 'A data final deve ser posterior à inicial';
    else if (diarias > MAXIMO_DIAS) erros.fim = `O período não pode passar de ${MAXIMO_DIAS} dias`;
  }
  const vistos = new Set();
  itens.forEach((item, indice) => {
    const codigo = item.codigo.trim();
    if (!codigo) erros.itens[indice].codigo = 'Informe o código';
    else if (vistos.has(codigo)) erros.itens[indice].codigo = 'Código repetido nesta reserva';
    vistos.add(codigo);
    if (!item.descricao.trim()) erros.itens[indice].descricao = 'Informe a descrição';
    if (!(numero(item.diaria) > 0)) erros.itens[indice].diaria = 'A diária deve ser maior que zero';
  });
  const valido = !erros.inicio && !erros.fim && erros.itens.every((item) => Object.keys(item).length === 0);
  return { erros, valido };
}

function Formulario({ id, inicial }) {
  const navegar = useNavigate();
  const [inicio, definirInicio] = useState(inicial.inicio);
  const [fim, definirFim] = useState(inicial.fim);
  const [itens, definirItens] = useState(inicial.itens);
  const [tentou, definirTentou] = useState(false);
  const [erroApi, definirErroApi] = useState('');
  const [salvando, definirSalvando] = useState(false);

  const validacao = validar({ inicio, fim, itens });
  const erros = tentou ? validacao.erros : SEM_ERROS;
  const erroGeral = erroApi || (tentou && !validacao.valido ? 'Corrija os campos destacados para continuar.' : '');
  const diarias = inicio && fim ? Math.max(dias(inicio, fim), 0) : 0;
  const subtotal = (item) => { const valor = numero(item.diaria); return valor > 0 ? valor * diarias : 0; };
  const destino = id ? `/locacoes/${id}` : '/locacoes';

  const mudarItem = (indice, campo, valor) => definirItens(itens.map((item, i) => (i === indice ? { ...item, [campo]: valor } : item)));

  const remover = (indice) => definirItens(itens.filter((_, i) => i !== indice));

  async function salvar(evento) {
    evento.preventDefault();
    definirTentou(true);
    definirErroApi('');
    if (!validacao.valido) return;
    definirSalvando(true);
    const reserva = {
      inicio,
      fim,
      itens: itens.map((item) => ({ codigo: item.codigo.trim(), descricao: item.descricao.trim(), diaria: numero(item.diaria) })),
    };
    try {
      const salva = id ? await alterarLocacao(id, reserva) : await criarLocacao({ ...reserva, clienteId: clienteAtual() });
      navegar(`/locacoes/${salva.id}`, { state: { aviso: id ? 'Reserva alterada.' : 'Reserva criada.' } });
    } catch (erro) {
      definirErroApi(erro.message);
      definirSalvando(false);
    }
  }

  const rotuloSalvar = id ? 'Salvar alterações' : 'Criar reserva';

  return (
    <form onSubmit={salvar} noValidate className="coluna">
      {erroGeral && <div className="aviso aviso-erro" role="alert"><p>{erroGeral}</p></div>}
      <div className="duas-colunas">
        <div className="coluna">
          <section className="cartao" aria-labelledby="titulo-periodo">
            <div className="cartao-corpo">
              <h2 id="titulo-periodo">Período</h2>
              <div className="grade-datas">
                <Campo id="inicio" rotulo="Início" type="date" value={inicio} erro={erros.inicio} onChange={(e) => definirInicio(e.target.value)} />
                <Campo id="fim" rotulo="Fim" type="date" value={fim} erro={erros.fim} onChange={(e) => definirFim(e.target.value)} />
              </div>
              <p className="ajuda">De 1 a 30 diárias. O dia final não é cobrado: de 10/11 a 13/11 são 3 diárias.</p>
            </div>
          </section>
          <section className="cartao" aria-labelledby="titulo-equipamentos">
            <div className="cartao-corpo">
              <div className="cabeca">
                <h2 id="titulo-equipamentos">Equipamentos</h2>
                <span className="suave pequeno">{itens.length} de {MAXIMO_ITENS}</span>
              </div>
              {itens.map((item, indice) => {
                const erro = erros.itens[indice] || {};
                return (
                  // O índice identifica a linha porque o código é editável e pode estar vazio ou repetido.
                  <div className="item-linha" key={indice}>
                    <div className="item-titulo">
                      <span>Equipamento {indice + 1}</span>
                      {itens.length > 1 && <button type="button" className="btn-link" onClick={() => remover(indice)}>Remover</button>}
                    </div>
                    <Campo id={`codigo-${indice}`} rotulo="Código" mono type="text" value={item.codigo} erro={erro.codigo} onChange={(e) => mudarItem(indice, 'codigo', e.target.value)} />
                    <Campo id={`descricao-${indice}`} rotulo="Descrição" type="text" value={item.descricao} erro={erro.descricao} onChange={(e) => mudarItem(indice, 'descricao', e.target.value)} />
                    <Campo id={`diaria-${indice}`} rotulo="Diária (R$)" type="text" inputMode="decimal" value={item.diaria} erro={erro.diaria} onChange={(e) => mudarItem(indice, 'diaria', e.target.value)} />
                    <div className="item-remover">
                      {itens.length > 1 && (
                        <button type="button" className="btn btn-secundario btn-icone" aria-label={`Remover equipamento ${indice + 1}`} onClick={() => remover(indice)}><IconeFechar /></button>
                      )}
                    </div>
                  </div>
                );
              })}
              {itens.length < MAXIMO_ITENS
                ? <button type="button" className="btn btn-secundario alinhar-inicio" onClick={() => definirItens([...itens, itemVazio()])}><IconeMais />Adicionar equipamento</button>
                : <p className="ajuda">Limite de {MAXIMO_ITENS} equipamentos atingido.</p>}
            </div>
          </section>
        </div>
        <aside className="coluna">
          <section className="cartao" aria-labelledby="titulo-resumo">
            <div className="cartao-corpo">
              <h2 id="titulo-resumo">Resumo</h2>
              <div className="resumo-linha"><span className="suave">Diárias</span><span className="valor">{diarias || '—'}</span></div>
              {itens.filter((item) => item.codigo.trim()).map((item, indice) => (
                <div className="resumo-linha" key={indice}><span className="codigo pequeno quebra">{item.codigo}</span><span className="valor">{dinheiro(subtotal(item))}</span></div>
              ))}
              <div className="resumo-linha resumo-total"><span>Valor estimado</span><span className="valor">{dinheiro(itens.reduce((total, item) => total + subtotal(item), 0))}</span></div>
              <button type="submit" className="btn btn-primario btn-bloco" disabled={salvando}>{salvando ? 'Salvando…' : rotuloSalvar}</button>
              <Link className="btn btn-secundario btn-bloco" to={destino}>Voltar sem salvar</Link>
            </div>
          </section>
        </aside>
      </div>
    </form>
  );
}

const paraFormulario = (locacao) => ({
  inicio: locacao.inicio,
  fim: locacao.fim,
  itens: locacao.itens.map((item) => ({ codigo: item.codigo, descricao: item.descricao, diaria: Number(item.diaria).toFixed(2).replace('.', ',') })),
});

export default function FormularioReserva() {
  const { id } = useParams();
  const [carga, definirCarga] = useState(id ? { situacao: 'carregando' } : { situacao: 'ok', inicial: { inicio: '', fim: '', itens: [itemVazio()] } });

  useEffect(() => {
    if (!id) return undefined;
    let ativo = true;
    buscarLocacao(id)
      .then((locacao) => {
        if (!ativo) return;
        if (locacao.estado === 'ABERTA') definirCarga({ situacao: 'ok', inicial: paraFormulario(locacao) });
        else definirCarga({ situacao: 'erro', mensagem: 'Somente locações abertas podem ser alteradas' });
      })
      .catch((erro) => ativo && definirCarga({ situacao: 'erro', mensagem: erro.message }));
    return () => { ativo = false; };
  }, [id]);

  return (
    <>
      <Link className="voltar" to={id ? `/locacoes/${id}` : '/locacoes'}><IconeVoltar />{id ? 'Detalhes da locação' : 'Locações'}</Link>
      <h1>{id ? 'Alterar reserva' : 'Nova reserva'}</h1>
      {carga.situacao === 'carregando' && <p className="suave" role="status">Carregando reserva…</p>}
      {carga.situacao === 'erro' && <div className="aviso aviso-erro" role="alert"><p>{carga.mensagem}</p></div>}
      {carga.situacao === 'ok' && <Formulario id={id} inicial={carga.inicial} />}
    </>
  );
}
