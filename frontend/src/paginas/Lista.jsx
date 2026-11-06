import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { listarLocacoes } from '../api.js';
import { IconeMais } from '../componentes/Icones.jsx';
import { ESTADOS, dias, dinheiro, idCurto, periodoBr, plural } from '../formato.js';
import { clienteAtual } from '../sessao.js';

const FILTROS = [['', 'Todas'], ...Object.entries(ESTADOS).map(([estado, { rotulo }]) => [estado, rotulo])];

function Carregando() {
  return (
    <>
      <p className="suave pequeno lista-carregando" role="status">Carregando locações…</p>
      {[1, 2, 3, 4].map((linha) => (
        <div className="osso-linha" aria-hidden="true" key={linha}>
          <div className="osso" /><div className="osso" /><div className="osso" /><div className="osso" />
        </div>
      ))}
    </>
  );
}

function Tabela({ locacoes }) {
  return (
    <table className="tabela tabela-locacoes">
      <thead>
        <tr>
          <th scope="col">Locação</th>
          <th scope="col">Período</th>
          <th scope="col">Estado</th>
          <th scope="col" className="direita">Valor total</th>
          <th scope="col"><span className="visualmente-oculto">Ação</span></th>
        </tr>
      </thead>
      <tbody>
        {locacoes.map((locacao) => (
          <tr key={locacao.id}>
            <td className="c-id codigo">{idCurto(locacao.id)}</td>
            <td className="c-periodo" data-rotulo="Período">
              {periodoBr(locacao.inicio, locacao.fim)}{' '}
              <span className="suave pequeno">· {plural(dias(locacao.inicio, locacao.fim), 'diária', 'diárias')}</span>
            </td>
            <td className="c-estado"><span className={ESTADOS[locacao.estado].classe}>{ESTADOS[locacao.estado].rotulo}</span></td>
            <td className="c-valor valor direita" data-rotulo="Valor total">{dinheiro(locacao.valorTotal)}</td>
            <td className="c-acao direita">
              <Link className="btn btn-secundario" to={`/locacoes/${locacao.id}`} aria-label={`Ver detalhes da locação ${idCurto(locacao.id)}`}>Detalhes</Link>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

export default function Lista() {
  const [parametros, definirParametros] = useSearchParams();
  const estado = ESTADOS[parametros.get('estado')] ? parametros.get('estado') : '';
  const [resultado, definirResultado] = useState({ situacao: 'carregando' });
  const [tentativa, definirTentativa] = useState(0);

  useEffect(() => {
    let ativo = true;
    definirResultado({ situacao: 'carregando' });
    listarLocacoes(clienteAtual(), estado)
      .then((locacoes) => ativo && definirResultado({ situacao: 'ok', locacoes }))
      .catch((erro) => ativo && definirResultado({ situacao: 'erro', mensagem: erro.message }));
    return () => { ativo = false; };
  }, [estado, tentativa]);

  const filtrar = (valor) => definirParametros(valor ? { estado: valor } : {});
  const vazia = resultado.situacao === 'ok' && resultado.locacoes.length === 0;

  return (
    <>
      <div className="cabeca">
        <h1>Locações</h1>
        <Link className="btn btn-primario" to="/locacoes/nova"><IconeMais />Nova reserva</Link>
      </div>
      <div className="filtros" role="group" aria-label="Filtrar por estado">
        {FILTROS.map(([valor, rotulo]) => (
          <button type="button" key={valor} className={estado === valor ? 'chip chip-ativo' : 'chip'} aria-pressed={estado === valor} onClick={() => filtrar(valor)}>
            {rotulo}
          </button>
        ))}
      </div>
      <section className="cartao" aria-label="Lista de locações">
        {resultado.situacao === 'carregando' && <Carregando />}
        {resultado.situacao === 'erro' && (
          <div className="estado-tela" role="alert">
            <h2>Não foi possível carregar as locações</h2>
            <p className="suave">{resultado.mensagem}</p>
            <button type="button" className="btn btn-secundario" onClick={() => definirTentativa((n) => n + 1)}>Tentar novamente</button>
          </div>
        )}
        {vazia && !estado && (
          <div className="estado-tela">
            <h2>Você ainda não tem locações</h2>
            <p className="suave">Crie uma reserva para começar. Ela aparecerá aqui com o período, o estado e o valor total.</p>
            <Link className="btn btn-primario" to="/locacoes/nova">Criar reserva</Link>
          </div>
        )}
        {vazia && estado && (
          <div className="estado-tela">
            <h2>Nenhuma locação neste estado</h2>
            <p className="suave">Escolha outro estado ou mostre todas as locações.</p>
            <button type="button" className="btn btn-secundario" onClick={() => filtrar('')}>Mostrar todas</button>
          </div>
        )}
        {resultado.situacao === 'ok' && !vazia && <Tabela locacoes={resultado.locacoes} />}
      </section>
    </>
  );
}
