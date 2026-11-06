import { useEffect } from 'react';
import { Navigate, Outlet, useNavigate } from 'react-router-dom';
import { definirAoExpirarSessao } from '../api.js';
import { clienteAtual, encerrarSessao } from '../sessao.js';

export default function Sistema() {
  const navegar = useNavigate();
  const cliente = clienteAtual();

  useEffect(() => {
    definirAoExpirarSessao(() => {
      encerrarSessao();
      navegar('/login', { replace: true, state: { erro: 'Sua sessão expirou. Entre novamente.' } });
    });
    return () => definirAoExpirarSessao(null);
  }, [navegar]);

  if (!cliente) return <Navigate to="/login" replace />;

  function sair() {
    encerrarSessao();
    navegar('/login');
  }

  return (
    <>
      <header className="topo">
        <div className="topo-dentro">
          <p className="logo"><span className="logo-marca" aria-hidden="true" />Locação de Equipamentos</p>
          <div className="topo-conta">
            <span>Cliente: <span className="mono">{cliente}</span></span>
            <button type="button" className="topo-sair" onClick={sair}>Sair</button>
          </div>
        </div>
      </header>
      <main className="pagina">
        <Outlet />
      </main>
    </>
  );
}
