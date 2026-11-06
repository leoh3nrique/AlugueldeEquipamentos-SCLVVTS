import { useCallback, useEffect, useState } from 'react';
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom';
import { buscarLocacao } from '../api.js';
import AcoesLocacao from '../componentes/AcoesLocacao.jsx';
import { IconeConfirmar, IconeFechar, IconeVoltar } from '../componentes/Icones.jsx';
import { ESTADOS, dataBr, dataHoraBr, dias, dinheiro, idCurto, periodoBr, plural } from '../formato.js';

const TITULOS_ERRO = { 403: 'Acesso não permitido', 404: 'Locação não encontrada' };

function situacaoDoItem(item, estado) {
  if (item.devolvido) return { texto: `Devolvido em ${dataBr(item.dataDevolucao)}`, classe: 'tag tag-finalizada' };
  if (estado === 'ABERTA') return { texto: 'Aguardando retirada', classe: 'tag tag-aberta' };
  if (estado === 'CANCELADA') return { texto: 'Não retirado', classe: 'tag tag-cancelada' };
  return { texto: 'Aguardando devolução', classe: 'tag tag-andamento' };
}

function Equipamentos({ locacao }) {
  return (
    <section className="cartao" aria-labelledby="titulo-itens">
      <div className="cartao-corpo cartao-titulo-tabela"><h2 id="titulo-itens">Equipamentos</h2></div>
      <table className="tabela">
        <thead>
          <tr>
            <th scope="col">Equipamento</th>
            <th scope="col" className="direita">Diária</th>
            <th scope="col">Prazo contratado</th>
            <th scope="col">Devolução</th>
          </tr>
        </thead>
        <tbody>
          {locacao.itens.map((item) => {
            const situacao = situacaoDoItem(item, locacao.estado);
            const atraso = item.devolvido ? dias(item.fimContratado, item.dataDevolucao) : 0;
            return (
              <tr key={item.codigo}>
                <td><span className="codigo">{item.codigo}</span><br /><span className="suave pequeno">{item.descricao}</span></td>
                <td className="valor direita" data-rotulo="Diária">{dinheiro(item.diaria)}</td>
                <td data-rotulo="Prazo contratado">{dataBr(item.fimContratado)}</td>
                <td data-rotulo="Devolução">
                  <span className={situacao.classe}>{situacao.texto}</span>
                  {atraso > 0 && <><br /><span className="pequeno atraso">{plural(atraso, 'dia de atraso', 'dias de atraso')}</span></>}
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </section>
  );
}

function Carregando() {
  return (
    <>
      <p className="suave" role="status">Carregando locação…</p>
      <div className="duas-colunas" aria-hidden="true">
        <div className="cartao"><div className="cartao-corpo"><div className="osso" /><div className="osso" /><div className="osso" /></div></div>
        <div className="cartao"><div className="cartao-corpo"><div className="osso" /><div className="osso" /></div></div>
      </div>
    </>
  );
}

export default function Detalhes() {
  const { id } = useParams();
  const { state, pathname } = useLocation();
  const navegar = useNavigate();
  const [carga, definirCarga] = useState({ situacao: 'carregando' });
  const [aviso, definirAviso] = useState(state?.aviso || '');
  const [tentativa, definirTentativa] = useState(0);

  useEffect(() => {
    if (state?.aviso) navegar(pathname, { replace: true, state: null });
  }, [state, pathname, navegar]);

  const recarregar = useCallback(() => buscarLocacao(id).then((locacao) => definirCarga({ situacao: 'ok', locacao })), [id]);

  useEffect(() => {
    let ativo = true;
    definirCarga({ situacao: 'carregando' });
    buscarLocacao(id)
      .then((locacao) => ativo && definirCarga({ situacao: 'ok', locacao }))
      .catch((erro) => ativo && definirCarga({ situacao: 'erro', status: erro.status, mensagem: erro.message }));
    return () => { ativo = false; };
  }, [id, tentativa]);

  const { locacao } = carga;

  return (
    <>
      {aviso && (
        <div className="aviso aviso-sucesso" role="status">
          <IconeConfirmar />
          <p>{aviso}</p>
          <button type="button" className="aviso-fechar" aria-label="Fechar aviso" onClick={() => definirAviso('')}><IconeFechar tamanho={16} /></button>
        </div>
      )}
      <Link className="voltar" to="/locacoes"><IconeVoltar />Locações</Link>
      {carga.situacao === 'carregando' && <Carregando />}
      {carga.situacao === 'erro' && (
        <section className="cartao">
          <div className="estado-tela" role="alert">
            <h2>{TITULOS_ERRO[carga.status] || 'Não foi possível carregar a locação'}</h2>
            {carga.status !== 404 && <p className="suave">{carga.mensagem}</p>}
            {TITULOS_ERRO[carga.status]
              ? <Link className="btn btn-secundario" to="/locacoes">Voltar para locações</Link>
              : <button type="button" className="btn btn-secundario" onClick={() => definirTentativa((n) => n + 1)}>Tentar novamente</button>}
          </div>
        </section>
      )}
      {locacao && (
        <>
          <div className="cabeca">
            <div className="cabeca-titulo">
              <h1>Locação <span className="mono">{idCurto(locacao.id)}</span></h1>
              <span className={ESTADOS[locacao.estado].classe}>{ESTADOS[locacao.estado].rotulo}</span>
            </div>
          </div>
          <div className="duas-colunas">
            <div className="coluna">
              <section className="cartao" aria-labelledby="titulo-dados">
                <div className="cartao-corpo">
                  <h2 id="titulo-dados">Dados da locação</h2>
                  <dl className="dados">
                    <div><dt>Cliente</dt><dd className="mono">{locacao.clienteId}</dd></div>
                    <div><dt>Criada em</dt><dd>{dataHoraBr(locacao.criadaEm)}</dd></div>
                    <div className="dados-largo"><dt>ID</dt><dd className="mono quebra">{locacao.id}</dd></div>
                    <div><dt>Período</dt><dd>{periodoBr(locacao.inicio, locacao.fim)}</dd></div>
                    <div><dt>Diárias</dt><dd>{dias(locacao.inicio, locacao.fim)}</dd></div>
                    <div><dt>Renovações</dt><dd>{locacao.renovacoes} de 2</dd></div>
                  </dl>
                </div>
              </section>
              <Equipamentos locacao={locacao} />
            </div>
            <aside className="coluna coluna-acoes">
              <AcoesLocacao
                locacao={locacao}
                aoAbrir={() => definirAviso('')}
                aoRecusar={() => recarregar().catch(() => {})}
                aoConcluir={(atualizada, mensagem) => {
                  definirCarga({ situacao: 'ok', locacao: atualizada });
                  definirAviso(mensagem);
                }}
              />
              <section className="cartao" aria-labelledby="titulo-valores">
                <div className="cartao-corpo">
                  <h2 id="titulo-valores">Valores</h2>
                  <div className="resumo-linha"><span className="suave">Valor normal</span><span className="valor">{dinheiro(locacao.valorNormal)}</span></div>
                  <div className="resumo-linha"><span className="suave">Multa</span><span className="valor">{dinheiro(locacao.multa)}</span></div>
                  <div className="resumo-linha resumo-total"><span>Valor total</span><span className="valor">{dinheiro(locacao.valorTotal)}</span></div>
                </div>
              </section>
            </aside>
          </div>
        </>
      )}
    </>
  );
}
