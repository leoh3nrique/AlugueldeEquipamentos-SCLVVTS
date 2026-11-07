import { useEffect, useState } from 'react';
import { Navigate, Outlet, useNavigate } from 'react-router-dom';
import { definirAoExpirarSessao } from '../api.js';
import { encerrarSessao, sair, usuarioAtual } from '../sessao.js';

export default function Sistema() {
  const navegar = useNavigate();
  const usuario = usuarioAtual();
  const [saindo, definirSaindo] = useState(false);
  const [erroSair, definirErroSair] = useState('');

  useEffect(() => {
    definirAoExpirarSessao(() => {
      encerrarSessao();
      navegar('/login', { replace: true, state: { erro: 'Sua sessão expirou. Entre novamente.' } });
    });
    return () => definirAoExpirarSessao(null);
  }, [navegar]);

  if (!usuario) return <Navigate to="/login" replace />;

  async function aoSair() {
    definirSaindo(true);
    definirErroSair('');
    try {
      await sair();
      navegar('/login');
    } catch (erro) {
      definirErroSair(erro.message);
      definirSaindo(false);
    }
  }

  return (
    <>
      <header className="topo">
        <div className="topo-dentro">
          <p className="logo"><span className="logo-marca" aria-hidden="true" />Locação de Equipamentos</p>
          <div className="topo-conta">
            <span>{usuario.nome}</span>
            <button type="button" className="topo-sair" disabled={saindo} onClick={aoSair}>{saindo ? 'Saindo…' : 'Sair'}</button>
          </div>
        </div>
      </header>
      <main className="pagina">
        {erroSair && <div className="aviso aviso-erro" role="alert"><p>{erroSair}</p></div>}
        <Outlet />
      </main>
    </>
  );
}
