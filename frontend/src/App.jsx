import { useEffect, useState } from 'react';
import { Navigate, Outlet, Route, Routes } from 'react-router-dom';
import Sistema from './componentes/Sistema.jsx';
import Cadastro from './paginas/Cadastro.jsx';
import Detalhes from './paginas/Detalhes.jsx';
import FormularioReserva from './paginas/FormularioReserva.jsx';
import Lista from './paginas/Lista.jsx';
import Login from './paginas/Login.jsx';
import { recuperarSessao, usuarioAtual } from './sessao.js';

function Visitante() {
  return usuarioAtual() ? <Navigate to="/locacoes" replace /> : <Outlet />;
}

export default function App() {
  const [sessao, definirSessao] = useState({ situacao: 'verificando' });
  const [tentativa, definirTentativa] = useState(0);

  useEffect(() => {
    let ativo = true;
    definirSessao({ situacao: 'verificando' });
    recuperarSessao()
      .then(() => ativo && definirSessao({ situacao: 'ok' }))
      .catch((erro) => ativo && definirSessao({ situacao: 'erro', mensagem: erro.message }));
    return () => { ativo = false; };
  }, [tentativa]);

  if (sessao.situacao === 'verificando') {
    return <div className="app"><main className="estado-tela" role="status"><p className="suave">Carregando…</p></main></div>;
  }

  if (sessao.situacao === 'erro') {
    return (
      <div className="app">
        <main className="estado-tela" role="alert">
          <h1>Não foi possível verificar sua sessão</h1>
          <p className="suave">{sessao.mensagem}</p>
          <button type="button" className="btn btn-secundario" onClick={() => definirTentativa((n) => n + 1)}>Tentar novamente</button>
        </main>
      </div>
    );
  }

  return (
    <div className="app">
      <Routes>
        <Route element={<Visitante />}>
          <Route path="/login" element={<Login />} />
          <Route path="/cadastro" element={<Cadastro />} />
        </Route>
        <Route element={<Sistema />}>
          <Route path="/locacoes" element={<Lista />} />
          <Route path="/locacoes/nova" element={<FormularioReserva />} />
          <Route path="/locacoes/:id" element={<Detalhes />} />
          <Route path="/locacoes/:id/alterar" element={<FormularioReserva />} />
        </Route>
        <Route path="*" element={<Navigate to="/locacoes" replace />} />
      </Routes>
    </div>
  );
}
